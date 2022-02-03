package org.edgeComputing.core.model;

import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.edgeComputing.Env;
import org.edgeComputing.model.DeviceInfo;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.Logger;

import java.util.*;
import java.lang.Math;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.edgeComputing.ethereum.Web3JClient;
import org.edgeComputing.ethereum.AuctionManager;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

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
            case FogEvents.FIND_MATCHES:
                // calculateInputMatrix();
                findMatchesFromContract();
                break;
            case FogEvents.LAUNCH_MODULE:
                this.send(this.getId(), 160, FogEvents.FIND_MATCHES);
                processModuleArrival(ev);
                break;
            default:
                super.processOtherEvent(ev);
                break;
        }
    }

    protected void handleTaskInfo(SimEvent ev) {
        Tuple tuple = (Tuple) ev.getData();
        tupleList.add(tuple);
        Logger.debug(this.getName(), "********** REGISTERING TUPLE");
        Web3JClient client = new Web3JClient();
        AuctionManager auctionContract = client.getContract();

        String[] partsOfTupleId = tuple.getTaskId().split("-");
        int tupleID = Integer.parseInt(partsOfTupleId[1]);
        Double scaledbid = tuple.getTupleBidPrice() * 1000;
        Double scaledtr = tuple.getTransmissionPowerOfSourceDevice() * 10;
        Double scaledidle = tuple.getIdlePowerOfSourceDevice() * 10000;
        Double x = tuple.getSourcexCoordinate();
        Double y = tuple.getSourceyCoordinate();
        try {

            TransactionReceipt r = auctionContract.registerMobileTask(
                new BigInteger(Integer.toString(tupleID + 1)), 
                new BigInteger(Long.toString(tuple.getCloudletLength())), // cpu length
                new BigInteger(Long.toString(tuple.getCloudletFileSize())), // nw length
                new BigInteger(Long.toString(tuple.getCloudletOutputSize())),
                new BigInteger(Integer.toString(tuple.getDeadline().intValue())),
                new BigInteger(Integer.toString(scaledbid.intValue())),
                new BigInteger(Integer.toString(tuple.getUpLinkBandwidth().intValue())),
                new BigInteger(Integer.toString(x.intValue())),
                new BigInteger(Integer.toString(y.intValue())),
                new BigInteger(Integer.toString(scaledtr.intValue())),
                new BigInteger(Integer.toString(scaledidle.intValue()))
            ).send();
            
            // Logger.debug(this.getName(), r.getLogs()[0]);
            Logger.debug(this.getName(), r.toString());
            AuctionManager.MobileTaskRegisteredEventResponse res = auctionContract.getMobileTaskRegisteredEvents(r).get(0);
            Logger.debug(this.getName(), Integer.toString(res.id.intValue()));         

        } catch (Exception e) {
            Logger.debug(this.getName(), e.getMessage());
        }
    }

    protected void handleEdgeServerInfo(SimEvent ev) {
        DeviceInfo edgeServerInfo = (DeviceInfo) ev.getData();
        edgeServerInfoList.add(edgeServerInfo);
        Logger.debug(this.getName(), "********** REGISTERING SERVER");
        Web3JClient client = new Web3JClient();
        AuctionManager auctionContract = client.getContract();

        try {
            TransactionReceipt r = auctionContract.registerServerNode(
                edgeServerInfo.getName(),
                new BigInteger(Integer.toString(new Double(edgeServerInfo.getMips()).intValue())),
                new BigInteger(Integer.toString(new Double(edgeServerInfo.getxCoordinate()).intValue())),
                new BigInteger(Integer.toString(new Double(edgeServerInfo.getyCoordinate()).intValue())),
                new BigInteger(Integer.toString(new Double(edgeServerInfo.getBidPrice() * 1000).intValue()))
            ).send();
            
            // Logger.debug(this.getName(), r.getLogs()[0]);
            Logger.debug(this.getName(), r.toString());
            AuctionManager.ServerNodeRegisteredEventResponse res = auctionContract.getServerNodeRegisteredEvents(r).get(0);
            Logger.debug(this.getName(), res.name);         

        } catch (Exception e) {
            Logger.debug(this.getName(), e.getMessage());
        }
    }

    protected void findMatchesFromContract() {
        Logger.debug(this.getName(), "********** FIND OUT MATCHES");
        Web3JClient client = new Web3JClient();
        AuctionManager auctionContract = client.getContract();
        int[] matches = new int[tupleList.size()];
        for (int i=0; i < tupleList.size(); i ++) {
            String[] partsOfTupleId = tupleList.get(i).getTaskId().split("-");
            int tupleID = Integer.parseInt(partsOfTupleId[1]);
            try {
                TransactionReceipt r = auctionContract.auctionResultTuple(
                    new BigInteger(Integer.toString(tupleID + 1))
                ).send();
                
                // Logger.debug(this.getName(), r.getLogs()[0]);
                Logger.debug(this.getName(), r.toString());
                AuctionManager.AuctionTupleResultEventResponse res = auctionContract.getAuctionTupleResultEvents(r).get(0);
                if (res.serverName.equals("NOTFOUND")) {
                    Logger.debug(this.getName(), "not assinged");
                    matches[i] = -1;
                } else {
                    Logger.debug(this.getName(), Integer.toString(tupleID) + " " + res.serverName);
                    String[] partsOfServerId = res.serverName.split("EDGE_SERVER_");
                    int j = Integer.parseInt(partsOfServerId[1]);
                    Logger.debug(this.getName(), Integer.toString(tupleID) + " " + Integer.toString(j));
                    matches[i] = j;
                }        

            } catch (Exception e) {
                Logger.debug(this.getName(), e.getMessage());
            }

        }
        //for (int i = 0; i < matches.length; i ++ ) {
          //  Logger.debug(this.getName(), " for tupleID: server=> " + Integer.toString(i) + " " + Integer.toString(matches[i]));
        //}
        sendAuctioneerResponseToMobile(matches);
    }

    protected void handleTasks(SimEvent ev) {
    }

    protected void handleResponse(SimEvent ev) {
    }

    protected void handleAuctioneerResponse(SimEvent ev) {

    }


    private void calculateInputMatrix() {
        List<Tuple> tuples = tupleList;
        List<DeviceInfo> deviceInfos = edgeServerInfoList;
        Double[][] tuplesPrefrences = new Double[tuples.size()][deviceInfos.size()];
        Double[][] edgeServerPriorities = new Double[deviceInfos.size()][tuples.size()];
        double edgeServerSQuota[] = new double[deviceInfos.size()];
        double[][] tuplesRequiredMipsOnEachEdgeServer = new double[tuples.size()][deviceInfos.size()];
        // we want to obtain score of each mobile and edge server in this loop
        for (Tuple tuple : tupleList) {
            for (DeviceInfo deviceInfo : deviceInfos) {
                String[] partsOfTupleId = tuple.getTaskId().split("-");
                int i = Integer.parseInt(partsOfTupleId[1]);
                String[] partsOfServerId = deviceInfo.getName().split("EDGE_SERVER_");
                int j = Integer.parseInt(partsOfServerId[1]) - 1;
                double inputSize = tuple.getCloudletFileSize();
                double cpuLength = tuple.getCloudletLength();
                long mipsOfSourceDevice = tuple.getMipsOfSourceDevice();
                //compute time and energy when tuple execute locally
                double t_local = cpuLength / mipsOfSourceDevice;
                double e_local = cpuLength * tuple.getBusyPowerOfSourceDevice();
//                double cost_local= t_local + e_local;
                double distance_ij = calculateDistance(tuple.getSourcexCoordinate(),
                        tuple.getSourceyCoordinate(),
                        deviceInfo.getxCoordinate(),
                        deviceInfo.getyCoordinate());



                //compute time and energy when tuple offload
                //1) first phase: time and energy for transmit tuple to edge server j
//                    long r_ij= B_ij log(1+ P_i*H_ij/sigma^2)
//                double H_ij = 128.1 + (37.5 * (Math.log(distance_ij) / Math.log(2)));
                double H_ij = Math.pow(distance_ij, -4);
//                double H_ij = 127 + (30 * log2(distance_ij)); //the channel power gain
//                double noisePower = dbm2watt(-100); //-100 in dBm to watt //The power of White Gaussian Noise
                //Mahla Noise Power
                double noisePower = 2 * Math.pow(10, -13);
                double r_ij = tuple.getUpLinkBandwidth() *
                        log2(1 + (tuple.getTransmissionPowerOfSourceDevice() * H_ij / noisePower));
                double t_ij_transmit = inputSize / r_ij;
                Logger.debug("Auctioneer", "i = " + i);
                Logger.debug("Auctioneer", "j = " + j);
                Logger.debug("Auctioneer", "t_ij_transmit = " + t_ij_transmit);

                double e_ij_transmit = t_ij_transmit * tuple.getTransmissionPowerOfSourceDevice();
                Logger.debug("Auctioneer", "e_ij_transmit = " + e_ij_transmit);
                double timeForExecution = tuple.getDeadline() - t_ij_transmit;
//                double timeForExecution = deadline-t_ij_offload;


                double numberOfMipsRequired;
                if (timeForExecution > 0) {
                    numberOfMipsRequired = cpuLength / timeForExecution;
                    Logger.debug("Auctioneer", "numberOfMipsRequired = " + numberOfMipsRequired);
                    Logger.debug("Auctioneer", "////////////////////////////");

                } else {
                    numberOfMipsRequired = 0;
                    timeForExecution = 0;
                }

                //2) second phase: time and energy for process tuple in edge server j
                double t_ij_process = cpuLength / deviceInfo.getMips();
//                double t_ij_process = cpuLength / numberOfMipsRequired;
                double e_ij_process = t_ij_process * tuple.getIdlePowerOfSourceDevice();

                double t_ij_offload = t_ij_transmit + t_ij_process;
                double e_ij_offload = e_ij_transmit + e_ij_process;
                Logger.debug("Auctioneer", "t_ij_process = " + t_ij_process);
                Logger.debug("Auctioneer", "e_ij_process = " + e_ij_process);


                Logger.debug("Auctioneer", "t_ij_offload = " + t_ij_offload);

                Logger.debug("Auctioneer", "e_ij_offload = " + e_ij_offload);


                double edgeServer_bid = deviceInfo.getBidPrice();

                double deadline = tuple.getDeadline();
                Logger.debug("Auctioneer", "deadline = " + deadline);

                double maxEnergyToOffload = deadline * (tuple.getTransmissionPowerOfSourceDevice() + tuple.getIdlePowerOfSourceDevice());
                double maxEdgeServersBidPrice = 0.435207782;
//                double reverse_cost_offload_ij_for_tuple = -(1 / 3.0) * t_ij_offload/deadline -(1 / 3.0) * e_ij_offload/maxEnergyToOffload -(1 / 3.0) * edgeServer_bid/maxEdgeServersBidPrice;
//                double reverse_cost_offload_ij_for_tuple = -(1 / 3.0) * t_ij_offload/deadline -(1 / 3.0) * e_ij_offload/maxEnergyToOffload -(1 / 3.0) * edgeServer_bid/maxEdgeServersBidPrice;
                double reverse_cost_offload_ij_for_tuple = -(1 / 3.0) * t_ij_offload - (1 / 3.0) * e_ij_offload - (1 / 3.0) * edgeServer_bid;

                if (t_local < t_ij_offload && e_local < e_ij_offload) {
                    tuplesPrefrences[i][j] = null;
                } else {
                    tuplesPrefrences[i][j] = reverse_cost_offload_ij_for_tuple;
                }

                tuplesRequiredMipsOnEachEdgeServer[i][j] = numberOfMipsRequired;
                double tuple_bid = tuple.getTupleBidPrice();
                double maxTuplesBid = 0.9;
                double maxDistance = calculateDistance(0, 0, 300, 300);
//                double reverse_cost_for_edgeServer = (1 / 2.0) * tuple_bid/maxTuplesBid -(1 / 2.0) * distance_ij/maxDistance ;
                double reverse_cost_for_edgeServer = (1 / 2.0) * tuple_bid - (1 / 2.0) * distance_ij / maxDistance;
                edgeServerPriorities[j][i] = reverse_cost_for_edgeServer;
                edgeServerSQuota[j] = deviceInfo.getMips();
            }

        }
                findMatchesBasedOnBFDA(tuplesPrefrences,edgeServerPriorities,edgeServerSQuota,tuplesRequiredMipsOnEachEdgeServer);
//        findMatchesBasedOnDAMB(tuplesPrefrences, edgeServerPriorities, edgeServerSQuota, tuplesRequiredMipsOnEachEdgeServer);
//        findMatchesBasedOfMyFirstAlgothitm(tuplesPrefrences,edgeServerPriorities,edgeServerSQuota,tuplesRequiredMipsOnEachEdgeServer);
//          findMathesBasedOnMySecondAlgorithm(tuplesPrefrences, edgeServerPriorities, edgeServerSQuota, tuplesRequiredMipsOnEachEdgeServer);




    }

    private void findMatchesBasedOfMyFirstAlgothitm(Double[][] tuplesPrefrences, Double[][] edgeServerPriorities, double edgeServerSQuota[], double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[][] tuplesPrefrences1 = sortRowWise(tuplesPrefrences);
        int[][] edgeServerPriorities1 = sortRowWise(edgeServerPriorities);
//        int[][] tuplesPrefrences1 = {{3,2,1,0},{2,1,3,0},{3,2,1,0},{3,2,1,0},{1,2,3,0} };
//        int[][] edgeServerPriorities1 = {{4,2,1,3,0},{1,2,4,3,0},{4,1,2,3,0},{3,2,1,4,0}};
//       long edgeServerSQuota[]={600,200,100,50};
//        int[][] tuplesRequiredMipsOnEachEdgeServer1={{400,100,300,100},{100,200,100,200},{300,100,200,100},{500,100,200,100},{500,200,300,100}};


        int[] matches = Ttcmmech(tuplesPrefrences1.length, edgeServerPriorities1.length, tuplesPrefrences1, edgeServerPriorities1, edgeServerSQuota, tuplesRequiredMipsOnEachEdgeServer);
        //in this phase auctioneer must response to mobiles that which edge server is the best match for them
        sendAuctioneerResponseToMobile(matches);
//            send(this.getId(), 10, FogEvents.FIND_MATCHES_PERIODICALLY);
    }

    public int[] Ttcmmech(int n, int m, int[][] tuplePrefs, int[][] serverPrefs, double[] serversQuota, double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] choice = new int[n];
        int num = 0;     //keep track the num of tuples who have been allocated
        Map<Integer, ArrayList<Integer>> tupleRemain = new HashMap<Integer, ArrayList<Integer>>();    //for storing the tuples who are not allocated
        Map<Integer, ArrayList<Integer>> serverRemain = new HashMap<Integer, ArrayList<Integer>>();  //for storing servers who have mipss remains
        Map<Integer, ArrayList<Double>> tuplesRequiredMips = new HashMap<>();

        double[] counter = new double[m];          //keep track how much mips are still available at this server
        int[] numberOfAllocatedTasksOnServer = new int[m];  //keep track how many tasks are executing on this edge server

        for (int i = 0; i < n; i++) {                    //initialization
            choice[i] = -1;
            ArrayList<Integer> ls = new ArrayList<>();
            ArrayList<Double> ls1 = new ArrayList<>();

            for (int j = 0; j < m; j++) {
                ls.add(tuplePrefs[i][j]);
                ls1.add(tuplesRequiredMipsOnEachEdgeServer[i][j]);
            }
            tupleRemain.put(i, ls);
            tuplesRequiredMips.put(i, ls1);
        }
        for (int i = 0; i < m; i++) {             //initialization
            counter[i] = serversQuota[i];
            numberOfAllocatedTasksOnServer[i] = 0;
            ArrayList<Integer> ls = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                ls.add(serverPrefs[i][j]);
            }
            serverRemain.put(i, ls);
        }
        while (!tupleRemain.isEmpty() && !serverRemain.isEmpty()) {            //if there are still tuples who are not allocated
            HashMap<Integer, Integer> tupleChoice = new HashMap<>();        //for storing the new matching pairs in this step
            int tpId = tupleRemain.keySet().iterator().next();
            strongConnected(tpId, true, tupleRemain, serverRemain, tupleChoice, counter, numberOfAllocatedTasksOnServer, tuplesRequiredMips);      // find the circle which include this school node if exist
            Iterator<Integer> keyTpl = tupleChoice.keySet().iterator();    //use the tupleChoice for updating the final choice table and the remaining tuples and servers
            num += tupleChoice.keySet().size();

            while (keyTpl.hasNext()) {
                int tplId = keyTpl.next();
                int srvId = tupleChoice.get(tplId);
                choice[tplId] = srvId;       //update the tuple choice table
                if (num <= n) {

                    updateRemain(tplId, srvId, tupleRemain, serverRemain, counter, numberOfAllocatedTasksOnServer, tuplesRequiredMips);   //updating the remaining tuples and servers
                }
            }
        }
        return choice;
    }

    static int[][] sortRowWise(Double m[][]) {
        int[][] x = new int[m.length][m[0].length];
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
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = j;
            }
        }
    }

    /**
     * This function add the vertex to the chain and if by adding this vertex, create a cycle, all vertex of that cycle
     * add as final choices
     */
    public static void strongConnected(int vertexId, boolean isTuple, Map<Integer, ArrayList<Integer>> tplRemain, Map<Integer, ArrayList<Integer>> srvRemain,
                                       Map<Integer, Integer> tplChoice, double[] serversQuota, int[] numberOfAllocatedTasksOnServer, Map<Integer, ArrayList<Double>> tuplesRequiredMips) {

        if (isTuple) {
            String toAddTuple = "tpl-" + vertexId;
            if (!graph.contains(toAddTuple)) {
                graph.add(toAddTuple);
                int srvId = tplRemain.get(vertexId).get(0);
                strongConnected(srvId, false, tplRemain, srvRemain, tplChoice, serversQuota, numberOfAllocatedTasksOnServer, tuplesRequiredMips);
            } else {
                graph.add(toAddTuple);
                for (int i = graph.indexOf(toAddTuple); i < graph.size() - 1; i += 2) {
                    String valueOfTuple = graph.get(i);
                    String valueOfServer = graph.get(i + 1);
                    int tupleId = Integer.parseInt(valueOfTuple.substring(0, 0) + valueOfTuple.substring(4));
                    int serverId = Integer.parseInt(valueOfServer.substring(0, 0) + valueOfServer.substring(4));
                    tplChoice.put(tupleId, serverId);
                }
                graph.clear();
            }
        }
        //////////////////////////////////////////////////////////////////////////////////////////////

        else if (!isTuple) {
            String lastValueOfGraph = graph.get(graph.size() - 1);
            String lastTupleId = lastValueOfGraph.substring(0, 0) + lastValueOfGraph.substring(4);
            int lastTupleIdToInt = Integer.parseInt(lastTupleId);
            Iterator<Integer> keyIter = tplRemain.get(lastTupleIdToInt).iterator();
            while (keyIter.hasNext()) {
                int sId = keyIter.next();

                double capacity = ((numberOfAllocatedTasksOnServer[sId] == 0) ? serversQuota[sId] : serversQuota[sId] / numberOfAllocatedTasksOnServer[sId]);
                if (tuplesRequiredMips.get(lastTupleIdToInt).get(sId) <= capacity) {
//                if (tuplesRequiredMips.get(lastTupleIdToInt).get(sId) <= serversQuota[sId]) {

                    String toAddServer = "srv-" + sId;
                    if (!graph.contains(toAddServer)) { ///if dont create a cycle
                        graph.add(toAddServer);
                        int tplId = srvRemain.get(sId).get(0);
                        strongConnected(tplId, true, tplRemain, srvRemain, tplChoice, serversQuota, numberOfAllocatedTasksOnServer, tuplesRequiredMips);      // find the circle which include this school node if exist
                    } else {     //if create a cycle
                        graph.add(toAddServer);
                        for (int i = graph.indexOf(toAddServer) + 1; i < graph.size() - 1; i += 2) {
                            String valueOfTuple = graph.get(i);
                            String valueOfServer = graph.get(i + 1);
                            int tupleId = Integer.parseInt(valueOfTuple.substring(0, 0) + valueOfTuple.substring(4));
                            int serverId = Integer.parseInt(valueOfServer.substring(0, 0) + valueOfServer.substring(4));
                            tplChoice.put(tupleId, serverId);
                        }
                        graph.clear();
                    }
                    break;
                }
            }
            /// if we check all servers, and there is no server to match with last tuple,
            // we match this tuple to the self user equipment
            if (keyIter.hasNext() == false && graph.size() > 0) {
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

    public static void updateRemain(int tplId, int srvId, Map<Integer, ArrayList<Integer>> tplRemain, Map<Integer, ArrayList<Integer>> srvRemain, double[] counter, int[] numberOfAllocatedTasksOnServer, Map<Integer, ArrayList<Double>> tuplesRequiredMips) {

        tplRemain.remove(tplId);

        if (srvId != -1) {
//            counter[srvId] -= tuplesRequiredMips.get(tplId).get(srvId);
            tuplesRequiredMips.remove(tplId);
            numberOfAllocatedTasksOnServer[srvId] += 1;
//            double capacity = ((numberOfAllocatedTasksOnServer[srvId] == 0) ? counter[srvId] : counter[srvId] / numberOfAllocatedTasksOnServer[srvId]);

            DoubleSummaryStatistics s
                    = tuplesRequiredMips.values().stream()
                    .flatMapToDouble(vs -> vs.stream().mapToDouble(Double::doubleValue))
                    .summaryStatistics();
            Double min = s.getMin();
//            if (capacity <= min) {
            if(counter[srvId] <= min){
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
        while (keySrv.hasNext()) {
            int srvKey = keySrv.next();
            ArrayList<Integer> tplList = srvRemain.get(srvKey);
            int index = tplList.indexOf(tplId);
            tplList.remove(index);
        }
    }

    /**
     * This function calculate the log base 2 of an integer
     */
    public static double log2(double N) {
        double result = (Math.log(N) / Math.log(2));
        return result;
    }

    private double calculateDistance(double sourcexCoordinate, double sourceyCoordinate, double destinationxCoordinate, double destinationyCoordinate) {
        return Math.sqrt(Math.pow(sourcexCoordinate - destinationxCoordinate, 2.00) +
                Math.pow(sourceyCoordinate - destinationyCoordinate, 2.00));
    }

    /**
     * This function convert decibel to watt
     */
    private double dbm2watt(double input) {
        double pow = input / 10;
        return Math.pow(10.0, pow) / 1000;
    }

//////////////////////////////////////////////////////////////

    private void findMatchesBasedOnDAMB(Double[][] tuplesPrefrences, Double[][] edgeServerPriorities, double edgeServerSQuota[], double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] matches = mechOfDAMB(tuplesPrefrences.length, edgeServerSQuota, tuplesRequiredMipsOnEachEdgeServer);
        //in this phase auctioneer must response to mobiles that which edge server is the best match for them
        sendAuctioneerResponseToMobile(matches);

    }

    public int[] mechOfDAMB(int n, double[] serversQuota, double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] choice = new int[n];
        for (int j = 0; j < choice.length; j++) {
            choice[j] = -1;
        }
        int num = 0;     //keep track the num of tuples who have been allocated
        List<Tuple> tuples = tupleList;
        List<DeviceInfo> deviceInfos = edgeServerInfoList;
        double[] A = new double[deviceInfos.size()];

        Map<Integer,Double> myA = new HashMap<>();
        Map<Integer,Map<Integer,Double>> B = new HashMap<>();

        for (DeviceInfo deviceInfo : deviceInfos) {
            String[] partsOfServerId = deviceInfo.getName().split("EDGE_SERVER_");
            int j = Integer.parseInt(partsOfServerId[1]) - 1;
            myA.put(j, deviceInfo.getBidPrice());
            A[j] = deviceInfo.getBidPrice();
        }
        double[] A_bar = sortAscendORDescend(true, A);
        int mu = A_bar.length / 2;
        double a_mu = A_bar[mu];
        myA.values().removeIf(value -> value <= a_mu);
        for (Tuple tuple : tuples) {
            String[] partsOfTupleId = tuple.getTaskId().split("-");
            int i = Integer.parseInt(partsOfTupleId[1]);
            Map<Integer,Double> temp = new HashMap<>();
            if(tuple.getTupleBidPrice() >= a_mu){
                for (int j = 0; j < deviceInfos.size(); j++ ) {
                    temp.put(j,tuple.getTupleBidPrice());
                }
                B.put(i, temp);
            }
        }
        while (!B.isEmpty()) {
            Double max = findMaxInMapOfMap(B);
            positionResult position = findPosition(B, max);
            int tplId = position.getRow();
            int srvId = position.getCol();
            if (tuplesRequiredMipsOnEachEdgeServer[tplId][srvId] <= serversQuota[srvId]) {
                choice[tplId] = srvId;       //update the tuple choice table
                num++;
                if (num <= n) {
                    B.remove(tplId);
                    serversQuota[srvId] -= tuplesRequiredMipsOnEachEdgeServer[tplId][srvId];
                    if (serversQuota[srvId] <= 0) {
                        Iterator<Integer> disIter = B.keySet().iterator();
                        while (disIter.hasNext()) {
                            int tpId = disIter.next();
                            B.get(tpId).remove(srvId);
                        }

                    }
                }
            } else {
                B.get(tplId).remove(srvId);
                if (B.get(tplId).isEmpty()) {
                    B.remove(tplId);
                }
            }
        }
        return choice;
    }

        static double[] sortAscendORDescend(boolean AorD, double array[]) {
        double temp = 0;
        if (AorD) {
            for (int i = 0; i < array.length; i++) {
                for (int j = i + 1; j < array.length; j++) {
                    if (array[i] > array[j]) {
                        temp = array[i];
                        array[i] = array[j];
                        array[j] = temp;
                    }
                }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                for (int j = i + 1; j < array.length; j++) {
                    if (array[i] < array[j]) {
                        temp = array[i];
                        array[i] = array[j];
                        array[j] = temp;
                    }
                }
            }

        }
        return array;

    }

    private void sendAuctioneerResponseToMobile(int[] matches) {
        //in this phase auctioneer must response to mobiles that which edge server is the best match for them
        for (Tuple tuple : tupleList) {
            String[] partsOfTupleId = tuple.getTaskId().split("-");
            int tupleId = Integer.parseInt(partsOfTupleId[1]);
            if (matches[tupleId] == -1) {
                tuple.setDestinationId(tuple.getSourceDeviceId());
                tuple.setDestModuleName(tuple.getSrcModuleName());
            } else {
                String EdgeServerName = "EDGE_SERVER_" + matches[tupleId];
                DeviceInfo serverInfo = null;
                for (DeviceInfo devInfo:edgeServerInfoList) {
                    if (devInfo.getName().equals(EdgeServerName))
                        serverInfo = devInfo;
                }


                tuple.setDestinationId(serverInfo.getId());
                tuple.setDestModuleName(serverInfo.getName());
            }
            tuple.setTupleType(Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE);

            for (int childId : getChildrenIds())
                //6.send match response to router
                sendDown(tuple, childId);
        }
    }

    private void findMatchesBasedOnBFDA(Double[][] tuplesPrefrences, Double[][] edgeServerPriorities, double edgeServerSQuota[], double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] matches = Tmmech(tuplesPrefrences.length, edgeServerPriorities.length, tuplesPrefrences, edgeServerPriorities, edgeServerSQuota, tuplesRequiredMipsOnEachEdgeServer);
        //in this phase auctioneer must response to mobiles that which edge server is the best match for them
        sendAuctioneerResponseToMobile(matches);
    }

    public int[] Tmmech(int n, int m, Double[][] tuplePrefs, Double[][] serverPrefs, double[] serversQuota, double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] choice = new int[n];
        int num = 0;     //keep track the num of tuples who have been allocated
        Map<Integer, Map<Integer, Double>> tupleRemain = new HashMap<Integer, Map<Integer, Double>>();    //for storing the tuples who are not allocated
        Map<Integer, ArrayList<Double>> serverRemain = new HashMap<Integer, ArrayList<Double>>();  //for storing servers who have mipss remains
        Map<Integer, ArrayList<Double>> tuplesRequiredMips = new HashMap<>();

        double[] counter = new double[m];          //keep track how much mips are still available at this server
        int[] numberOfAllocatedTasksOnServer = new int[m];  //keep track how many tasks are executing on this edge server

        for (int i = 0; i < n; i++) {                    //initialization
            choice[i] = -1;
            Map<Integer, Double> ls = new HashMap<>();
            ArrayList<Double> ls1 = new ArrayList<>();

            for (int j = 0; j < m; j++) {
                ls.put(j, tuplePrefs[i][j]);
                ls1.add(tuplesRequiredMipsOnEachEdgeServer[i][j]);
            }
            tupleRemain.put(i, ls);
            tuplesRequiredMips.put(i, ls1);
        }
        for (int i = 0; i < m; i++) {             //initialization
            counter[i] = serversQuota[i];
            numberOfAllocatedTasksOnServer[i] = 0;
            ArrayList<Double> ls = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                ls.add(serverPrefs[i][j]);
            }
            serverRemain.put(i, ls);
        }

        while (!tupleRemain.isEmpty() && !serverRemain.isEmpty()) {            //if there are still tuples who are not allocated
            Double max = findMaxInMapOfMap(tupleRemain);
            positionResult positionResult = findPosition(tupleRemain, max);
            int tplId = positionResult.getRow();
            int srvId = positionResult.getCol();

            if (tuplesRequiredMipsOnEachEdgeServer[tplId][srvId] <= counter[srvId]) {
                choice[tplId] = srvId;       //update the tuple choice table
                num++;
                if (num <= n) {
                    updateRemain1(tplId, srvId, tupleRemain, serverRemain, counter, numberOfAllocatedTasksOnServer, tuplesRequiredMips);   //updating the remaining tuples and servers
                }
            } else {
                tupleRemain.get(tplId).remove(srvId);
                if (tupleRemain.get(tplId).values().size() == 0) {
                    tupleRemain.remove(tplId);
                }
            }
        }
        return choice;
    }

    public static void updateRemain1(int tplId, int srvId, Map<Integer, Map<Integer, Double>> tplRemain, Map<Integer, ArrayList<Double>> srvRemain, double[] counter, int[] numberOfAllocatedTasksOnServer, Map<Integer, ArrayList<Double>> tuplesRequiredMips) {

        tplRemain.remove(tplId);

        if (srvId != -1) {
//            counter[srvId] -= tuplesRequiredMips.get(tplId).get(srvId);
            numberOfAllocatedTasksOnServer[srvId] += 1;

            DoubleSummaryStatistics s
                    = tuplesRequiredMips.values().stream()
                    .flatMapToDouble(vs -> vs.stream().mapToDouble(Double::doubleValue))
                    .summaryStatistics();
            Double min = s.getMin();
            if (counter[srvId] <= min) {
                //remove this server from the list
                srvRemain.remove(srvId);
                Iterator<Integer> keyTpl = tplRemain.keySet().iterator();
                while (keyTpl.hasNext()) {
                    int tplKey = keyTpl.next();
                    tplRemain.get(tplKey).remove(srvId);
                }

            }
        }

    }

    /////////////////////////////////////////////////////////////////
    private void findMathesBasedOnMySecondAlgorithm(Double[][] tuplesPrefrences, Double[][] edgeServerPriorities, double edgeServerSQuota[], double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] matches = mechOfSecondAlgorithm(tuplesPrefrences.length, tuplesPrefrences, edgeServerPriorities, edgeServerSQuota, tuplesRequiredMipsOnEachEdgeServer);
        //in this phase auctioneer must response to mobiles that which edge server is the best match for them
        sendAuctioneerResponseToMobile(matches);
    }

    public int[] mechOfSecondAlgorithm(int n, Double[][] tuplePrefs, Double[][] serverPrefs, double[] serversQuota, double[][] tuplesRequiredMipsOnEachEdgeServer) {
        int[] choice = new int[n];
        for (int j = 0; j < choice.length; j++) {
            choice[j] = -1;
        }
        int num = 0;     //keep track the num of tuples who have been allocated
        Map<Integer, Map<Integer, Double>> distances = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Map<Integer, Double> distance = new HashMap<>();
            for (int j = 0; j < tuplePrefs[0].length; j++) {
                Double srverScore = serverPrefs[j][i];
                Double tupleScore = tuplePrefs[i][j];
                Double abs= Math.abs(tupleScore - srverScore);
                distance.put(j, abs);
            }
            distances.put(i, distance);
        }
        while (!distances.isEmpty()) {
            DoubleSummaryStatistics s
                    = distances.values().stream()
                    .flatMapToDouble(vs -> vs.values().stream().mapToDouble(Double::doubleValue))
                    .summaryStatistics();
//            Double max = s.getMin();
            Double max = s.getMax();

//            Double max = findMaxInMapOfMap(distances);
            positionResult position= findPosition(distances,max);
            int tplId = position.getRow();
            int srvId = position.getCol();
            if (tuplesRequiredMipsOnEachEdgeServer[tplId][srvId] <= serversQuota[srvId]) {
                choice[tplId] = srvId;       //update the tuple choice table
                num++;
                if (num <= n) {
                    distances.remove(tplId);
//                    serversQuota[srvId] -= tuplesRequiredMipsOnEachEdgeServer[tplId][srvId];
                    if (serversQuota[srvId]<=0){
                        Iterator<Integer> disIter = distances.keySet().iterator();
                        while (disIter.hasNext()) {
                            int tpId = disIter.next();
                            distances.get(tpId).remove(srvId);
                        }

                    }
                }
            }
            else {
                distances.get(tplId).remove(srvId);
                if(distances.get(tplId).isEmpty()){
                    distances.remove(tplId);
                }
            }
        }
        return choice;

    }
    private double findMaxInMapOfMap(Map<Integer, Map<Integer, Double>> input){
        DoubleSummaryStatistics s
                = input.values().stream()
                .flatMapToDouble(vs -> vs.values().stream().mapToDouble(Double::doubleValue))
                .summaryStatistics();
        Double max = s.getMax();
        return max;
    }
    public static positionResult findPosition(Map<Integer, Map<Integer, Double>> map,double input) {
        int row = 0;
        int col = 0;
        // this while is for finding the position of max
        Iterator<Integer> keyTpl = map.keySet().iterator();
        while (keyTpl.hasNext()) {
            int tplId = keyTpl.next();
            Iterator<Integer> keyTpl1 = map.get(tplId).keySet().iterator();
            while (keyTpl1.hasNext()) {
                int srvId = keyTpl1.next();
                if ((map.get(tplId).get(srvId).equals(input))) {
                    row = tplId;
                    col = srvId;
                    break;
                }
            }
        }

        return new positionResult(row, col);
    }
}

final class positionResult {
    private final int row;
    private final int col;

    public positionResult(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
