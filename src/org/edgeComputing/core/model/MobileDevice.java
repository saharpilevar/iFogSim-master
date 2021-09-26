package org.edgeComputing.core.model;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.edgeComputing.Env;
import org.edgeComputing.model.Task;
import org.edgeComputing.outputWriter.Output;
import org.fog.application.AppModule;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileDevice extends Device {

    private int taskGeneratorDeviceId = -1;
    private long mips = 0;
    private PowerModel powerModel;
    private int ram = 0;
    private double transmissionPower =0;
    private double idlePower =0;
    private double busyPower =0;

   // private Map<Integer, DeviceInfo> envState = new HashMap<>();
  //  private Map<Integer, Map<Integer, DeviceInfo>> stateMap = new HashMap<>();


    public MobileDevice(String name, FogDeviceCharacteristics characteristics,
                        VmAllocationPolicy vmAllocationPolicy,
                        List<Storage> storageList, double schedulingInterval,
                        double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency,
                        double ratePerMips) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth,
                downlinkBandwidth, uplinkLatency, ratePerMips);
    }

    public MobileDevice(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth,
                        double ratePerMips,
                        PowerModel powerModel, double idlePower, double busyPower, double transmissionPower, FogDeviceCharacteristics characteristics,
                        VmAllocationPolicy vmAllocationPolicy) throws Exception {
        super(name,characteristics, vmAllocationPolicy, null, 0, uplinkBandwidth, downlinkBandwidth, 0, ratePerMips);
        this.mips = mips;
        this.ram = ram;
        this.powerModel = powerModel;
        this.transmissionPower = transmissionPower;
        this.idlePower = idlePower;
        this.busyPower = busyPower;

    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case FogEvents.END_PROCESS:
                // Output.writeDNNRecords(dnnRecordList, this.getId());
                Output.writeResult();
                this.sendNow(this.getParentId(), FogEvents.END_PROCESS);
                break;
            default:
                super.processOtherEvent(ev);
                break;
        }
    }

    protected void handleTasks(SimEvent ev){
        //send tuple information to auctioneer
        Tuple tuple= (Tuple) ev.getData();
        tuple.setDestModuleName(Env.DEVICE_AUCTIONEER);
        tuple.setTupleType(Env.TUPLE_TYPE_TASK_INFO);
        tuple.setMipsOfSourceDevice(this.mips);
        tuple.setIdlePowerOfSourceDevice(this.idlePower);
        tuple.setBusyPowerOfSourceDevice(this.busyPower);
        tuple.setTransmissionPowerOfSourceDevice(this.transmissionPower);
        tuple.setTupleBidPrice(tuple.getTupleBidPrice());
        //tuple.setSrcModuleName(this.getName());
        tuple.setDirection(Tuple.UP);
        tuple.setSrcModuleName(Env.DEVICE_USER_EQUIPMENT);
        //tuple.setSrcModuleName(this.getName());
        //send(Env.DEVICE_AUCTIONEER,0L, FogEvents.TUPLE_ARRIVAL,tuple);
        sendUp(tuple);
    }

    protected void handleResponse(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
        if (this.taskGeneratorDeviceId > -1) {
            sendNow(this.taskGeneratorDeviceId, FogEvents.DONE_TUPLE, tuple);
        }
    }

    protected void handleAuctioneerResponse(SimEvent ev){

        //ev.getData() include list of tuples and servers that matches together
        List<Pair<Integer, Integer>> matchResponse = new ArrayList<>();
        matchResponse = (List<Pair<Integer, Integer>>) ev.getData();


    }

    @Override
    protected void handleEdgeServerInfo(SimEvent ev) {

    }

    @Override
    protected void handleTaskInfo(SimEvent ev) {

    }

    public int getTaskGeneratorDeviceId() {
        return taskGeneratorDeviceId;
    }

    public void setTaskGeneratorDeviceId(int taskGeneratorDeviceId) {
        this.taskGeneratorDeviceId = taskGeneratorDeviceId;
    }


}
