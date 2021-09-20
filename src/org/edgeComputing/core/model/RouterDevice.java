package org.edgeComputing.core.model;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouterDevice extends Device {
    private List<Integer> edgeServersList = new ArrayList<>();
    private Map<Integer, Boolean> mobileDevicesMap = new HashMap<>();
    public RouterDevice(String name, FogDeviceCharacteristics characteristics,
                        VmAllocationPolicy vmAllocationPolicy,
                        List<Storage> storageList, double schedulingInterval,
                        double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency,
                        double ratePerMips) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth,
                downlinkBandwidth, uplinkLatency, ratePerMips);
    }

    public RouterDevice(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth,
                        double ratePerMips,
                        PowerModel powerModel) throws Exception {
        super(name, mips, ram, uplinkBandwidth, downlinkBandwidth, ratePerMips, powerModel);
    }
    public void addMobileDevice(int id){
        if(!mobileDevicesMap.containsKey(id)){
            mobileDevicesMap.put(id, true);
        }
    }

    public void removeMobileDevice(int id){
        mobileDevicesMap.remove(id);
    }

    public void addEdgeServer(int id){
        if(!edgeServersList.contains(id)){
            edgeServersList.add(id);
        }
    }

    public void removeEdgeServer(int id){
        edgeServersList.remove(id);
    }

    @Override
    protected void processTupleArrival(SimEvent ev) {
        Tuple tuple = (Tuple) ev.getData();

        send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);

//        Logger.debug(getName(),
//                "Received tuple " + tuple.getCloudletId() + "with tupleType = " + tuple.getTupleType() + "\t| Source : " +
//                        CloudSim.getEntityName(ev.getSource()) + "|Dest : " + CloudSim.getEntityName(
//                        ev.getDestination()));

        if(tuple.getTupleType().equals(Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE) || tuple.getTupleType().equals(Env.TUPLE_TYPE_MATCH_RESPONSE_TO_EDGE_SERVER)){
            this.forwardTuple(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_TASK_INFO)){
            this.forwardTupleToAuctioneer(ev);
        }

        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_EDGE_SERVER_INFO)){
            this.forwardEdgeServerInfoToAuctioneer(ev);
        }
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case FogEvents.RECEIVE_DEVICE_INFO:
                forwardEdgeServerInfoToAuctioneer(ev);
                break;

            default:
                super.processOtherEvent(ev);
                break;
        }
    }
    private void forwardTuple(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
            sendDown(tuple, tuple.getDestinationId());
    }

    private void forwardTupleToAuctioneer(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();

        if(tuple.getDestModuleName().equals("AUCTIONEER")){
            //send(auctioneerId,0L,tuple);
            send(Env.DEVICE_AUCTIONEER,0, FogEvents.TUPLE_ARRIVAL, tuple);
        }
    }

    private void forwardEdgeServerInfoToAuctioneer(SimEvent ev){
        DeviceInfo deviceInfo = (DeviceInfo) ev.getData();
        send(Env.DEVICE_AUCTIONEER, 0, FogEvents.RECEIVE_DEVICE_INFO, deviceInfo);

    }

    @Override
    protected void handleEdgeServerInfo(SimEvent ev) {

    }

    @Override
    protected void handleAuctioneerResponse(SimEvent ev) {

    }

    @Override
    protected void handleResponse(SimEvent ev) {

    }

    @Override
    protected void handleTaskInfo(SimEvent ev) {

    }

    @Override
    protected void handleTasks(SimEvent ev) {

    }
}
