package org.edgeComputing.core.model;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.edgeComputing.Env;
import org.edgeComputing.model.DeviceInfo;
import org.edgeComputing.model.Task;
import org.fog.application.AppModule;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuctioneerDevice extends Device {
        private List<DeviceInfo> edgeServerInfoList = new ArrayList<>();
        private List<Tuple> tupleList = new ArrayList<>();
        private Map<Integer, Boolean> mobileDevicesMap = new HashMap<>();
        private boolean endProcess = false;

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

    private  void findMatchesPeriodically(){
        if(!endProcess) {
            //Pair of tuple id and edge_server id
        List<Pair<Integer, Integer>> matchResponse = new ArrayList<>();
        List<Tuple> tuples = tupleList;

        List<DeviceInfo> deviceInfos = edgeServerInfoList;
        double[][] matrix = new double[tuples.size()][deviceInfos.size()];

            // we want to obtain score of each mobile and edge server in this loop
            for (int i = 0; i < tuples.size() ; i++) {
                for (int j = 0; j < deviceInfos.size(); j++) {
                    long inputSize= tuples.get(i).getCloudletFileSize();
                    long cpuLength=tuples.get(i).getCloudletLength();


                    //compute time and energy when tuple execute locally
                    double t_local = cpuLength / tuples.get(i).getMipsOfSourceDevice();
                    double e_local = cpuLength * tuples.get(i).getBusyPowerOfSourceDevice();

                    //compute time and energy when tuple offload
                    //1) first phase: time and energy for transmit tuple to edge server j
//                    long r_ij= B_ij log(1+ P_i*H_ij/sigma^2)
                    long r_ij=1;
                    double t_ij_transmit = inputSize / r_ij;
                    double e_ij_transmit = t_ij_transmit * tuples.get(i).getTransmissionPowerOfSourceDevice();
                    //2) second phase: time and energy for process tuple in edge server j
                    double t_ij_process = cpuLength / deviceInfos.get(j).getMips();
                    double e_ij_process = t_ij_process * tuples.get(i).getIdlePowerOfSourceDevice();

                    double t_ij_offload = t_ij_transmit + t_ij_process;
                    double e_ij_offload = e_ij_transmit + e_ij_process;

                    matrix[i][j]= 0.5 * t_ij_offload + 0.5 * e_ij_offload;
                }

            }


        }
    }
    private Integer getDestinationId(Tuple tuple) {

        Integer selectedServerId = 1;
        return selectedServerId;
    }

}
