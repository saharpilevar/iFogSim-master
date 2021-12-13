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
        private static List<String> graph = new ArrayList<String>();


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
                this.send(this.getId(), 20, FogEvents.FIND_MATCHES_PERIODICALLY);
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
        long edgeServerSQuota[] = new long[deviceInfos.size()];
        double[][] tuplesRequiredMipsOnEachEdgeServer= new double[tuples.size()][deviceInfos.size()];
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
                    //Mahla Noise Power
                    //double noisePower = 2 * Math.pow(10, -13);
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

                    double cost_offload_ij_for_tuple = -(1 / 3.0) * t_ij_offload -(1 / 3.0) * e_ij_offload -(1 / 3.0) * edgeServer_bid;


                    tuplesPrefrences[i][j] = cost_offload_ij_for_tuple;

                    double deadline = tuples.get(i).getParentDeadline();
                    double timeForExecution = deadline - t_ij_transmit;
                    double numberOfMipsRequired;
                    if (timeForExecution > 0) {
                        numberOfMipsRequired = cpuLength/ timeForExecution;

                    }else {
                        numberOfMipsRequired =0;
                        timeForExecution =0;

                    }
                    tuplesRequiredMipsOnEachEdgeServer[i][j]= numberOfMipsRequired;
                    double tuple_bid = tuples.get(i).getTupleBidPrice();
                    double cost_for_edgeServer = (1 / 2.0) * tuple_bid -(1 / 2.0) * distance_ij ;
                    edgeServerPriorities[j][i] = cost_for_edgeServer ;
                    edgeServerSQuota[j]= deviceInfos.get(j).getMips();
                }
            }
        int[][] tuplesPrefrences1 = sortRowWise(tuplesPrefrences);
        int[][] edgeServerPriorities1 = sortRowWise(edgeServerPriorities);
//        int[][] tuplesPrefrences1 = {{3,2,1,0},{2,1,3,0},{3,2,1,0},{3,2,1,0},{1,2,3,0} };
//        int[][] edgeServerPriorities1 = {{4,2,1,3,0},{1,2,4,3,0},{4,1,2,3,0},{3,2,1,4,0}};
//       long edgeServerSQuota[]={600,200,100,50};
//        int[][] tuplesRequiredMipsOnEachEdgeServer1={{400,100,300,100},{100,200,100,200},{300,100,200,100},{500,100,200,100},{500,200,300,100}};


        int[] matches = Ttcmmech(tuplesPrefrences1.length,edgeServerPriorities1.length, tuplesPrefrences1,edgeServerPriorities1,edgeServerSQuota,tuplesRequiredMipsOnEachEdgeServer);
        //in this phase auctioneer must response to mobiles that which edge server is the best match for them
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

    public int[] Ttcmmech(int n, int m, int[][] tuplePrefs, int[][] serverPrefs, long[] serversQuota,double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] choice = new int[n];
        int num = 0;     //keep track the num of tuples who have been allocated
        Map<Integer, ArrayList<Integer>> tupleRemain = new HashMap<Integer, ArrayList<Integer>>();    //for storing the tuples who are not allocated
        Map<Integer, ArrayList<Integer>> serverRemain = new HashMap<Integer, ArrayList<Integer>>();  //for storing servers who have seats remains
        long [] counter = new long[m];          //keep track how much mips are still available at this server

        for(int i=0;i<n;i++){                    //initialization
            choice[i] = -1;
            ArrayList<Integer> ls = new ArrayList<>();
            for(int j=0; j<m; j++){
                ls.add(tuplePrefs[i][j]);
            }
            tupleRemain.put(i,ls);
        }
        for(int i=0;i<m;i++){             //initialization
            counter[i] = serversQuota[i];
            ArrayList<Integer> ls = new ArrayList<>();
            for(int j=0;j<n;j++){
                ls.add(serverPrefs[i][j]);
            }
            serverRemain.put(i,ls);
        }
        while (!tupleRemain.isEmpty() && !serverRemain.isEmpty()){            //if there are still tuples who are not allocated
            HashMap<Integer,Integer> tupleChoice = new HashMap<>();        //for storing the new matching pairs in this step
            int tpId = tupleRemain.keySet().iterator().next();;
            strongConnected(tpId, true, tupleRemain, serverRemain, tupleChoice, counter, tuplesRequiredMipsOnEachEdgeServer);      // find the circle which include this school node if exist
            num += tupleChoice.keySet().size();
            Iterator<Integer> keyTpl = tupleChoice.keySet().iterator();    //use the tupleChoice for updating the final choice table and the remaining tuples and servers
            while (keyTpl.hasNext()) {
                int tplId = keyTpl.next();
                int srvId = tupleChoice.get(tplId);
                choice[tplId] = srvId;       //update the tuple choice table
                if (num != n) {
                    updateRemain(tplId, srvId, tupleRemain, serverRemain, counter, tuplesRequiredMipsOnEachEdgeServer);   //updating the remaining tuples and servers
                }
            }
        }

        return choice;
    }

    static int[][] sortRowWise(double m[][]) {
        int[][] x= new int[m.length][m[0].length];
        fillDown(x);
        // loop for rows of matrix
        for (int i = 0; i < m.length; i++) {
            // loop for column of matrix
            for (int j = 0; j < m[i].length; j++) {
                // loop for comparison and swapping
                for (int k = 0; k < m[i].length - j - 1; k++) {
                    if (m[i][k] < m[i][k + 1]) {
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
                grid[i][j] = j;
            }
        }
    }


    public static void strongConnected(int vertexId,boolean isTuple, Map<Integer, ArrayList<Integer>> tplRemain,Map<Integer, ArrayList<Integer>>srvRemain,
                                       Map<Integer,Integer> tplChoice, long[] serversQuota,double[][] tuplesRequiredMipsOnEachEdgeServer) {

        if (isTuple) {
            String toAddTuple = "tpl-" + vertexId;
            if (!graph.contains(toAddTuple)) {
                graph.add(toAddTuple);
                int srvId = tplRemain.get(vertexId).get(0);
                strongConnected(srvId, false, tplRemain, srvRemain, tplChoice, serversQuota, tuplesRequiredMipsOnEachEdgeServer);      // find the circle which include this school node if exist
            }
            else {
                graph.add(toAddTuple);
                for (int i = graph.indexOf(toAddTuple); i < graph.size() - 1; i += 2) {
                    String valueOfTuple = graph.get(i);
                    String valueOfServer = graph.get(i + 1);
                    int tupleId = Integer.parseInt(valueOfTuple.substring(0, 0) + valueOfTuple.substring(4, valueOfTuple.length()));
                    int serverId = Integer.parseInt(valueOfServer.substring(0, 0) + valueOfServer.substring(4, valueOfServer.length()));
                    tplChoice.put(tupleId, serverId);
                }
                graph.clear();
            }
        }
        //////////////////////////////////////////////////////////////////////////////////////////////

        else if (!isTuple) {
            String lastValueOfGraph = graph.get(graph.size() - 1);
            String lastTupleId = lastValueOfGraph.substring(0, 0) + lastValueOfGraph.substring(4, lastValueOfGraph.length());
            int lastTupleIdToInt = Integer.parseInt(lastTupleId);
            Iterator<Integer> keyIter = tplRemain.get(lastTupleIdToInt).iterator();
            while (keyIter.hasNext()) {
                int sId = keyIter.next();
            if (tuplesRequiredMipsOnEachEdgeServer[lastTupleIdToInt][sId] <= serversQuota[sId]) {
                String toAddServer = "srv-" + sId;
                if (!graph.contains(toAddServer)) { ///if dont create a cycle
                    graph.add(toAddServer);
                    int tplId = srvRemain.get(sId).get(0);
                    strongConnected(tplId, true, tplRemain, srvRemain, tplChoice, serversQuota, tuplesRequiredMipsOnEachEdgeServer);      // find the circle which include this school node if exist
                }
                else {     //if create a cycle
                    graph.add(toAddServer);
                    for (int i = graph.indexOf(toAddServer) + 1; i < graph.size() - 1; i += 2) {
                        String valueOfTuple = graph.get(i);
                        String valueOfServer = graph.get(i + 1);
                        int tupleId = Integer.parseInt(valueOfTuple.substring(0, 0) + valueOfTuple.substring(4, valueOfTuple.length()));
                        int serverId = Integer.parseInt(valueOfServer.substring(0, 0) + valueOfServer.substring(4, valueOfServer.length()));
                        tplChoice.put(tupleId, serverId);
                    }
                    graph.clear();
                }
                break;
            }

            }
            if(keyIter.hasNext()==false && graph.size()>0) {
                String valueOfGraph = graph.get(graph.size() - 1);
                String tupleId = valueOfGraph.substring(0, 0) + valueOfGraph.substring(4);
                int tupleIdToInt = Integer.parseInt(tupleId);
                if (tplChoice.get(tupleIdToInt) == null) {
                    tplChoice.put(lastTupleIdToInt, -1);
                    graph.clear();

                }
            }

        }
    }

    public  static void updateRemain(int tplId, int srvId, Map<Integer, ArrayList<Integer>> tplRemain, Map<Integer, ArrayList<Integer>>srvRemain, long [] counter, double[][] tuplesRequiredMipsOnEachEdgeServer){
        tplRemain.remove(tplId);
        if (srvId!=-1) {
            counter[srvId] -= tuplesRequiredMipsOnEachEdgeServer[tplId][srvId];
            if (counter[srvId] <= 0) {
                //remove this server from the list
                srvRemain.remove(srvId);
                Iterator<Integer> keyTpl = tplRemain.keySet().iterator();
                while (keyTpl.hasNext()) {
                    int tplKey = keyTpl.next();
                    ArrayList<Integer> srvList = tplRemain.get(tplKey);
                    int index = srvList.indexOf(srvId);
                    srvList.remove(index);
                }
            }
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

    // Function to calculate the log base 2 of an integer
    public static double log2(double N)
    {

        // calculate log2 N indirectly
        // using log() method
        double result = (Math.log(N) / Math.log(2));
        return result;
    }

    private double calculateDistance(double sourcexCoordinate,double sourceyCoordinate, double destinationxCoordinate,double destinationyCoordinate) {
        return Math.sqrt(Math.pow(sourcexCoordinate-destinationxCoordinate, 2.00)+
                Math.pow(sourceyCoordinate -destinationyCoordinate, 2.00));}

    private double dbm2watt(double input) {
        double pow = input / 10;
        return Math.pow(10.0, pow)/1000;
    }

}
