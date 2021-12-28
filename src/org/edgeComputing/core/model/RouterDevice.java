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
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case FogEvents.RECEIVE_DEVICE_INFO:
                forwardEdgeServerInfoToAuctioneer(ev);
                break;
            case FogEvents.END_PROCESS:
                deviceEndProcess(ev);
                break;

            default:
                super.processOtherEvent(ev);
                break;
        }
    }
    protected void handleTasks(SimEvent ev) {
        this.forwardTuple(ev);
    }

    protected void handleResponse(SimEvent ev) {
        this.forwardMatchResponseToMobile(ev);
    }

    protected void handleTaskInfo(SimEvent ev) {
        this.forwardTupleToAuctioneer(ev);
    }
    protected void handleAuctioneerResponse(SimEvent ev) {
        this.forwardMatchResponseToMobile(ev);
    }
    protected void handleEdgeServerInfo(SimEvent ev) {
        this.forwardEdgeServerInfoToAuctioneer(ev);
    }
    ////////////////////////////////////////////////////
    private void forwardTupleToAuctioneer(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
        if(tuple.getDestModuleName().equals(Env.DEVICE_AUCTIONEER)){
            //3.send tuple information to Auctioneer
            sendUp(tuple);
        }
    }

    private void forwardMatchResponseToMobile(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
        //7.send result of auction to mobile device
        sendDown(tuple, tuple.getSourceDeviceId());
    }


    private void forwardTuple(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();

        if(tuple.getDestModuleName().equals("Cloud")){
            sendUp(tuple);
        }

        else {
            //9. send tuple to edge server
            sendDown(tuple, tuple.getDestinationId());
        }
    }
    private void forwardEdgeServerInfoToAuctioneer(SimEvent ev){
        DeviceInfo deviceInfo = (DeviceInfo) ev.getData();

        send(Env.DEVICE_AUCTIONEER, 0, FogEvents.RECEIVE_DEVICE_INFO, deviceInfo);

    }

    private void deviceEndProcess(SimEvent ev){
        int sourceId = ev.getSource();
        mobileDevicesMap.replace(sourceId, Boolean.FALSE);
        boolean endProcess = true;
        for(int id : mobileDevicesMap.keySet()){
            if(mobileDevicesMap.get(id)){
                endProcess = false;
                break;
            }
        }
        if(endProcess){
            for(int id: edgeServersList) {
                this.sendNow(id, FogEvents.END_PROCESS);
            }
        }
    }

}
