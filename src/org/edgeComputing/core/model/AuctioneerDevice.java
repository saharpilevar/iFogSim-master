package org.edgeComputing.core.model;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.edgeComputing.Env;
import org.edgeComputing.model.DeviceInfo;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;

import java.util.*;
import java.lang.Math;

public class AuctioneerDevice extends Device {
        private List<DeviceInfo> edgeServerInfoList = new ArrayList<>();
        private List<Tuple> tupleList = new ArrayList<>();
        private boolean endProcess1 = false;
        private double heartBeatDelay = 10;

    public AuctioneerDevice(String name, FogDeviceCharacteristics characteristics,
                            VmAllocationPolicy vmAllocationPolicy,
                            List<Storage> storageList, double schedulingInterval,
                            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency,
                            double ratePerMips) throws Exception {
            super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth,
                    downlinkBandwidth, uplinkLatency, ratePerMips);
        }

        public AuctioneerDevice(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth,
                            double ratePerMips,
                            PowerModel powerModel) throws Exception {
            super(name, mips, ram, uplinkBandwidth, downlinkBandwidth, ratePerMips, powerModel);
        }


    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {

            case FogEvents.RECEIVE_DEVICE_INFO:
                handleEdgeServerInfo(ev);
                break;
            case FogEvents.FIND_MATCHES_PERIODICALLY:
                findMatchesPeriodically();
                break;
            case FogEvents.LAUNCH_MODULE:
                this.send(this.getId(), 10, FogEvents.FIND_MATCHES_PERIODICALLY);
                processModuleArrival(ev);
                break;

            default:
                super.processOtherEvent(ev);
                break;
        }
    }
    protected void handleTaskInfo(SimEvent ev){

        Tuple tuple = (Tuple) ev.getData();
        tupleList.add(tuple);
    }

    protected void handleEdgeServerInfo(SimEvent ev){
            DeviceInfo edgeServerInfo = (DeviceInfo) ev.getData();
            edgeServerInfoList.add(edgeServerInfo);
    }

    protected void handleTasks(SimEvent ev) { }

    protected void handleResponse(SimEvent ev) {   }

    protected void handleAuctioneerResponse(SimEvent ev) {

    }

    private  void findMatchesPeriodically(){
        List<Tuple> tuples = tupleList;
        List<DeviceInfo> deviceInfos = edgeServerInfoList;
        double[][] tuplesPrefrences = new double[tuples.size()][deviceInfos.size()];
        double[][] edgeServerPriorities = new double[deviceInfos.size()][tuples.size()];

//        if (!endProcess1) {
            // we want to obtain score of each mobile and edge server in this loop
            for (int i = 0; i < tuples.size(); i++) {
                for (int j = 0; j < deviceInfos.size() ; j++) {
                    double inputSize = tuples.get(i).getCloudletFileSize();

                    double cpuLength = tuples.get(i).getCloudletLength();
                    long mipsOfSourceDevice= tuples.get(i).getMipsOfSourceDevice();
                    //compute time and energy when tuple execute locally
                    double t_local =  cpuLength/mipsOfSourceDevice;
                    double e_local = cpuLength * tuples.get(i).getBusyPowerOfSourceDevice();

                    double cost_local= t_local + e_local;

                    double distance_ij= calculateDistance(tuples.get(i).getSourcexCoordinate(),
                            tuples.get(i).getSourceyCoordinate(),
                            deviceInfos.get(j).getxCoordinate(),
                            deviceInfos.get(j).getyCoordinate());
                    //compute time and energy when tuple offload
                    //1) first phase: time and energy for transmit tuple to edge server j
//                    long r_ij= B_ij log(1+ P_i*H_ij/sigma^2)
                    double H_ij = 127 + 30 * log2(distance_ij); //the channel power gain
                    double noisePower = dbm2watt(-100); //-100 in dBm to watt //The power of White Gaussian Noise

                    double r_ij = tuples.get(i).getUpLinkBandwidth() *
                            log2(1+ tuples.get(i).getTransmissionPowerOfSourceDevice()* H_ij / noisePower);
                    double t_ij_transmit = inputSize / r_ij;
                    double e_ij_transmit = t_ij_transmit * tuples.get(i).getTransmissionPowerOfSourceDevice();
                    //2) second phase: time and energy for process tuple in edge server j
                    double t_ij_process = cpuLength / deviceInfos.get(j).getMips();
                    double e_ij_process = t_ij_process * tuples.get(i).getIdlePowerOfSourceDevice();

                    double t_ij_offload = t_ij_transmit + t_ij_process;
                    double e_ij_offload = e_ij_transmit + e_ij_process;

                    double edgeServer_bid = deviceInfos.get(j).getBidPrice();

                    double cost_offload_ij_for_tuple = t_ij_offload + e_ij_offload + edgeServer_bid;

                    tuplesPrefrences[i][j] = 1 / cost_offload_ij_for_tuple;

                    double tuple_bid = tuples.get(i).getTupleBidPrice();

                    double cost_for_edgeServer = 0.5 * tuple_bid + 0.5 * distance_ij ;

                    edgeServerPriorities[j][i] = 1 / cost_for_edgeServer ;

                }

            }
        int[][] tuplesPrefrences1 = sortRowWise(tuplesPrefrences);
        int[][] edgeServerPriorities1 = sortRowWise(edgeServerPriorities);
        int[] matches = Ttcmmech(tuplesPrefrences1.length,edgeServerPriorities1.length, tuplesPrefrences1,edgeServerPriorities1);
        //in this phase auctioneer must response to mobiles that witch edge server is the best match for them
        for (int i = 0; i < matches.length; i++) {
            Tuple tuple= tupleList.get(i);
            if(matches[i]==-1){
                tuple.setDestinationId(tuple.getSourceDeviceId());
                tuple.setDestModuleName(Env.DEVICE_USER_EQUIPMENT);
            }
            else{
                tuple.setDestinationId(edgeServerInfoList.get(matches[i]-1).getId());
                tuple.setDestModuleName(Env.DEVICE_EDGE_SERVER);

            }
            tuple.setTupleType(Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE);

            for (int childId : getChildrenIds())
                //6.send match response to router
                sendDown(tuple, childId);
        }
//            send(this.getId(), 10, FogEvents.FIND_MATCHES_PERIODICALLY);
        }

//    }

    static int[][] sortRowWise(double m[][]) {
        int[][] x= new int[m.length][m[0].length];
        fillDown(x);
        // loop for rows of matrix
        for (int i = 0; i < m.length; i++) {
            // loop for column of matrix
            for (int j = 0; j < m[i].length; j++) {

                // loop for comparison and swapping
                for (int k = 0; k < m[i].length - j - 1; k++) {
                    if (m[i][k] > m[i][k + 1]) {
                        //swapping of elements
                        double t = m[i][k];
                        m[i][k] = m[i][k + 1];
                        m[i][k + 1] = t;
                        /////////////////
                        int y = x[i][k];
                        x[i][k] = x[i][k + 1];
                        x[i][k + 1] = y;
                    }
                }
            }
        }
                return x;
    }

    static void fillDown(int[][] grid) {
        for (int i = 0 ; i < grid.length ; i++){
            for (int j = 0 ; j < grid[i].length ; j++) {
                grid[i][j] = j+1;
            }
        }
    }

    public int[] Ttcmmech(int n, int m, int[][] tuplePrefs, int[][] serverPrefs) {
        int[] choice = new int[n];
        int num = 0;     //keep track the num of tuples who have been allocated
        Map<Integer, ArrayList<Integer>> tupleRemain = new HashMap<Integer, ArrayList<Integer>>();    //for storing the tuples who are not allocated
        Map<Integer, ArrayList<Integer>> serverRemain = new HashMap<Integer, ArrayList<Integer>>();  //for storing servers who have seats remains
        for(int i=0;i<n;i++){                    //initialization
            choice[i] = -1;
            ArrayList<Integer> ls = new ArrayList<>();
            for(int j=0; j<m; j++){
                ls.add(tuplePrefs[i][j]);
            }
            tupleRemain.put(i+1,ls);
        }
        for(int i=0;i<m;i++){             //initialization
            ArrayList<Integer> ls = new ArrayList<>();
            for(int j=0;j<n;j++){
                ls.add(serverPrefs[i][j]);
            }
            serverRemain.put(i+1,ls);
        }
        while (!tupleRemain.isEmpty() && !serverRemain.isEmpty()){            //if there are still tuples who are not allocated
            HashMap<Integer,Integer> tupleChoice = new HashMap<>();        //for storing the new matching pairs in this step
            findAllCircles(tupleRemain,serverRemain,tupleChoice);              //find all the circles in this step and find the ner matching pairs
            num += tupleChoice.keySet().size();
            Iterator<Integer> keyTpl = tupleChoice.keySet().iterator();    //use the tupleChoice for updating the final choice table and the remaining tuples and servers
            while (keyTpl.hasNext()){
                int tplId = keyTpl.next();
                int srvId = tupleChoice.get(tplId);
                choice[tplId-1] = srvId;       //update the tuple choice table
                if(num!=n){
                    updateRemain(tplId,srvId,tupleRemain,serverRemain);   //updating the remaining tuples and servers
                }
            }
        }
        return choice;
    }

    public static void findAllCircles(Map<Integer, ArrayList<Integer>> tupleRemain,Map<Integer, ArrayList<Integer>>serverRemain,HashMap<Integer, Integer> tupleChoice){
        Map<Integer,Boolean>  srvRead = new HashMap<>();                 //keep track if a certain server node has been read
        Iterator<Integer> srvKey = serverRemain.keySet().iterator();        //initialization
        while(srvKey.hasNext()){
            int srvId = srvKey.next();
            srvRead.put(srvId,false);
        }
        Iterator<Integer> keyIter = serverRemain.keySet().iterator();
        while(keyIter.hasNext()){                                     //ensure that every server node will be considered
            int srvId = keyIter.next();
            Stack<Integer> srvStack = new Stack<>();               //store the non-read nodes which can be reached from this server node
            strongConnected(srvId,srvRead,tupleRemain,serverRemain,tupleChoice,srvStack);      // find the circle which include this server node if exist
            while(!srvStack.isEmpty()){                //update
                int term = srvStack.pop();
                srvRead.replace(term,true);
            }
        }
    }

    public static void strongConnected(int srvId, Map<Integer,Boolean>srvRead, Map<Integer, ArrayList<Integer>> tplRemain,Map<Integer, ArrayList<Integer>>srvRemain,
                                       Map<Integer,Integer> tplChoice, Stack<Integer> srvStack){
        if(!srvRead.get(srvId)){
            if(!srvStack.contains(srvId)){     //if it's not read
                srvStack.push(srvId);
                //srvRead[srvId-1] = true;
                int tplId = srvRemain.get(srvId).get(0);
                int srvNext = tplRemain.get(tplId).get(0);
                strongConnected(srvNext,srvRead,tplRemain,srvRemain,tplChoice,srvStack);
            }
            else{       //if it is already in the stack, we can find a circle
                int srv = srvStack.pop();
                int w = srvId;
                while(srv!=srvId){
                    int tpl = srvRemain.get(srv).get(0);
                    tplChoice.put(tpl,w);
                    srvRead.replace(srv,true);
                    w = srv;
                    srv = srvStack.pop();
                }
                srvRead.replace(srvId,true);
                tplChoice.put(srvRemain.get(srvId).get(0),w);
            }
        }
    }

    public  static void updateRemain(int tplId, int srvId, Map<Integer, ArrayList<Integer>> tplRemain, Map<Integer, ArrayList<Integer>>srvRemain){
        tplRemain.remove(tplId);
        //remove this server from the list
        srvRemain.remove(srvId);
        Iterator<Integer> keyTpl = tplRemain.keySet().iterator();
        while(keyTpl.hasNext()){
            int tplKey = keyTpl.next();
            ArrayList<Integer> srvList = tplRemain.get(tplKey);
            int index = srvList.indexOf(srvId);
            srvList.remove(index);
        }
        //remove this tuple from the list
        Iterator<Integer> keySrv = srvRemain.keySet().iterator();
        while(keySrv.hasNext()){
            int srvKey = keySrv.next();
            ArrayList<Integer> tplList = srvRemain.get(srvKey);
            int index = tplList.indexOf(tplId);
            tplList.remove(index);
        }
    }

    private double calculateDistance(double sourcexCoordinate,double sourceyCoordinate, double destinationxCoordinate,double destinationyCoordinate) {
        return Math.sqrt(Math.pow(sourcexCoordinate-destinationxCoordinate, 2.00)+
                Math.pow(sourceyCoordinate -destinationyCoordinate, 2.00));}
    // Function to calculate the log base 2 of an integer
    public static double log2(double N)
    {

        // calculate log2 N indirectly
        // using log() method
        double result = (Math.log(N) / Math.log(2));

        return result;
    }

    private double dbm2watt(double input) {
        double pow = input / 10;
        return Math.pow(10.0, pow)/1000;
    }

}
