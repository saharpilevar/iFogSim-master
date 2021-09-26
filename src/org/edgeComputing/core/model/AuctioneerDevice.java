package org.edgeComputing.core.model;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.edgeComputing.Env;
import org.edgeComputing.model.DeviceInfo;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctioneerDevice extends Device {
        private List<DeviceInfo> edgeServerInfoList = new ArrayList<>();
        private List<Tuple> tupleList = new ArrayList<>();
        private Map<Integer, Boolean> mobileDevicesMap = new HashMap<>();
        private boolean endProcess = false;
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
    protected void processTupleArrival(SimEvent ev) {
        Tuple tuple = (Tuple) ev.getData();

        send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);


//        Logger.debug(getName(),
//                "Received tuple " + tuple.getCloudletId() + "with tupleType = " + tuple.getTupleType() + "\t| Source : " +
//                        CloudSim.getEntityName(ev.getSource()) + "|Dest : " + CloudSim.getEntityName(
//                        ev.getDestination()));

        if(tuple.getTupleType().equals(Env.TUPLE_TYPE_TASK_INFO) ){
            this.handleTaskInfo(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_EDGE_SERVER_INFO)){
            this.handleEdgeServerInfo(ev);
        }
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case FogEvents.RECEIVE_DEVICE_INFO:
                handleEdgeServerInfo(ev);
                break;
            case FogEvents.FIND_MATCHES_PERIODICALLY:
                findMatchesPeriodically(ev);
                break;
            case FogEvents.END_PROCESS:
                this.endProcess = true;
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


    protected void handleResponse(SimEvent ev) {
    }

    protected void handleAuctioneerResponse(SimEvent ev) {

    }



    private  void findMatchesPeriodically(SimEvent ev){
//        if(!endProcess) {
            //Pair of tuple id and edge_server id
        List<Pair<Integer, Integer>> matchResponse = new ArrayList<>();
        List<Tuple> tuples = tupleList;

        List<DeviceInfo> deviceInfos = edgeServerInfoList;
        double[][] tuplesPrefrences = new double[tuples.size()][deviceInfos.size()+1];
        double[][] edgeServerPriorities = new double[deviceInfos.size()][tuples.size()];
        if (!endProcess) {
            // we want to obtain score of each mobile and edge server in this loop
            for (int i = 0; i < tuples.size(); i++) {
                for (int j = 1; j < deviceInfos.size() + 1; j++) {
                    long inputSize = tuples.get(i).getCloudletFileSize();
                    long cpuLength = tuples.get(i).getCloudletLength();

                    //compute time and energy when tuple execute locally
                    double t_local = cpuLength / tuples.get(i).getMipsOfSourceDevice();
                    double e_local = cpuLength * tuples.get(i).getBusyPowerOfSourceDevice();

                    //compute time and energy when tuple offload
                    //1) first phase: time and energy for transmit tuple to edge server j
//                    long r_ij= B_ij log(1+ P_i*H_ij/sigma^2)
                    long r_ij = 1;
                    double t_ij_transmit = inputSize / r_ij;
                    double e_ij_transmit = t_ij_transmit * tuples.get(i).getTransmissionPowerOfSourceDevice();
                    //2) second phase: time and energy for process tuple in edge server j
                    double t_ij_process = cpuLength / deviceInfos.get(j - 1).getMips();
                    double e_ij_process = t_ij_process * tuples.get(i).getIdlePowerOfSourceDevice();

                    double t_ij_offload = t_ij_transmit + t_ij_process;
                    double e_ij_offload = e_ij_transmit + e_ij_process;

                    double edgeServer_bid = deviceInfos.get(j - 1).getBidPrice();

                    tuplesPrefrences[i][0] = 0.5 * t_local + 0.5 * e_local;
                    tuplesPrefrences[i][j] = 0.5 * t_ij_offload + 0.5 * e_ij_offload + edgeServer_bid;

                    double tuple_bid = tuples.get(i).getTupleBidPrice();
//                    double distance_ij
//                    edgeServerPriorities[j][i] = 0.5 * tuple_bid + 0.5 * distance_ij ;

//                    edgeServerPriorities[j][i] = tuple_bid;
                }
            }
            send(this.getId(), heartBeatDelay, FogEvents.FIND_MATCHES_PERIODICALLY);
        }
//        sortRowWise(tuplesPrefrences);

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
                grid[i][j] = j;
            }
        }
    }

}
