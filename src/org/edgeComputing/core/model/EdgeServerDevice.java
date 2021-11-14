package org.edgeComputing.core.model;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.edgeComputing.Env;
import org.edgeComputing.model.DeviceInfo;
import org.fog.application.AppModule;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EdgeServerDevice extends Device {
    private double heartBeatDelay = 10;
    private double joinTime = 0;
    private long mips = 0;
    private int ram = 0;
    private double bidPrice = 0;
    private boolean endProcess = false;
    public EdgeServerDevice(String name, FogDeviceCharacteristics characteristics,
                            VmAllocationPolicy vmAllocationPolicy,
                            List<Storage> storageList, double schedulingInterval,
                            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency,
                            double ratePerMips, double joinTime, long mips, int ram, double bidPrice) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth,
                downlinkBandwidth, uplinkLatency, ratePerMips);
        this.joinTime = joinTime;
        this.mips = mips;
        this.ram = ram;
        this.bidPrice = bidPrice;
    }

    public EdgeServerDevice(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth,
                            double ratePerMips,
                            PowerModel powerModel) throws Exception {
        super(name, mips, ram, uplinkBandwidth, downlinkBandwidth, ratePerMips, powerModel);
    }
    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case FogEvents.SEND_PERIODIC_DEVICE_INFO:
                sendDeviceInfo();
                break;
            case FogEvents.LAUNCH_MODULE:
                this.send(this.getId(), joinTime, FogEvents.SEND_PERIODIC_DEVICE_INFO);
                processModuleArrival(ev);
                break;
            case FogEvents.END_PROCESS:
                this.endProcess = true;
                break;
            default:
                super.processOtherEvent(ev);
                break;
        }
    }

    protected void handleTasks(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();

        if (getHost().getVmList().size() > 0) {
            final AppModule operator = (AppModule) getHost().getVmList().get(0);
            if (CloudSim.clock() > 0) {
                getHost().getVmScheduler().deallocatePesForVm(operator);
                getHost().getVmScheduler().allocatePesForVm(operator, new ArrayList<Double>() {
                    protected static final long serialVersionUID = 1L;

                    {
                        add((double) getHost().getTotalMips());
                    }
                });
            }
        }

        int vmId = -1;

        for (Vm vm : getHost().getVmList()) {
            if (((AppModule) vm).getName().equals(tuple.getDestModuleName()))
                vmId = vm.getId();
        }
        if (vmId < 0
                || (tuple.getModuleCopyMap().containsKey(tuple.getDestModuleName()) &&
                tuple.getModuleCopyMap().get(tuple.getDestModuleName()) != vmId)) {
            return;
        }
        tuple.setVmId(vmId);

        updateTimingsOnReceipt(tuple);

        executeTuple(ev, tuple.getDestModuleName());

        this.addTupleCloudletMap(tuple.getCloudletId(), tuple);

        tuple.setExecutorId(this.getId());
        tuple.setExecutorName(this.getName());
//        tuple.setTupleType(Env.TUPLE_TYPE_RESPONSE);
    }

    protected void handleResponse(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
        sendUp(tuple);
    }

    protected void handleAuctioneerResponse(SimEvent ev){
            }

    protected void handleEdgeServerInfo(SimEvent ev) {

    }

    protected void handleTaskInfo(SimEvent ev) {

    }

    public void sendDeviceInfo(){
        if (!endProcess) {
            DeviceInfo info = new DeviceInfo();
            info.setId(this.getId());
            info.setName(this.getName());
            info.setTime(CloudSim.clock());
            info.setEnergyConsumption(this.getEnergyConsumption());
            info.setLastUtilization(this.lastUtilization);
            info.setNetworkDelay(this.uplinkLatency);
            info.setMips(this.mips);
            info.setRam(this.ram);
            info.setBidPrice(this.bidPrice);
            info.setxCoordinate(this.getxCoordinate());
            info.setyCoordinate(this.getyCoordinate());
            send(parentId, getUplinkLatency(), FogEvents.RECEIVE_DEVICE_INFO, info);
            send(this.getId(), heartBeatDelay, FogEvents.SEND_PERIODIC_DEVICE_INFO);
            endProcess =true;
        }

    }
}
