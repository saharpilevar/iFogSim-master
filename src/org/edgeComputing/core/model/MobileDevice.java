package org.edgeComputing.core.model;

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
import org.edgeComputing.outputWriter.Output;
import org.fog.application.AppModule;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.Logger;

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
    private double maxTaskDeadline = -1.0;

    private TaskGeneratorDevice taskGeneratorDevice;

    private Map<Integer, DeviceInfo> envState = new HashMap<>();
    private Map<Integer, Map<Integer, DeviceInfo>> stateMap = new HashMap<>();
    private Map<String, Integer> edgeServersMap = new HashMap<>();




    public MobileDevice(String name, FogDeviceCharacteristics characteristics,
                        VmAllocationPolicy vmAllocationPolicy,
                        List<Storage> storageList, double schedulingInterval,
                        double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency,
                        double ratePerMips, long mips, int ram, double idlePower, double busyPower, double transmissionPower) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth,
                downlinkBandwidth, uplinkLatency, ratePerMips);
        this.mips = mips;
        this.ram = ram;
        this.transmissionPower = transmissionPower;
        this.idlePower = idlePower;
        this.busyPower = busyPower;
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
    private Map<Integer, DeviceInfo> copyMap(Map<Integer, DeviceInfo> map) {
        Map<Integer, DeviceInfo> copiedObj = new HashMap<>();
        for (Map.Entry<Integer, DeviceInfo> mapEntry : map.entrySet()) {
            copiedObj.put(mapEntry.getKey(), mapEntry.getValue());
        }
        return copiedObj;
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {

            case FogEvents.END_PROCESS:
                Output.writeResult();
                this.sendNow(this.getParentId(), FogEvents.END_PROCESS);
                break;
//            case FogEvents.MATCHES_RESPONSE_TO_MOBILE:
//                handleAuctioneerResponse(ev);
//                break;
            case FogEvents.FAIL_TASK:
                this.failTask(ev);
                break;

            default:
                super.processOtherEvent(ev);
                break;
        }
    }

    protected void handleTasks(SimEvent ev){
        Tuple tuple= (Tuple) ev.getData();
        if (tuple.getSrcModuleName()=="TASK") {
            tuple.setDestModuleName(Env.DEVICE_AUCTIONEER);
            tuple.setTupleType(Env.TUPLE_TYPE_TASK_INFO);
            tuple.setMipsOfSourceDevice(this.mips);
            tuple.setIdlePowerOfSourceDevice(this.idlePower);
            tuple.setBusyPowerOfSourceDevice(this.busyPower);
            tuple.setTransmissionPowerOfSourceDevice(this.transmissionPower);
            tuple.setTupleBidPrice(tuple.getTupleBidPrice());
            tuple.setSourceDeviceId(this.getId());
            tuple.setDirection(Tuple.UP);
            tuple.setSrcModuleName(Env.DEVICE_USER_EQUIPMENT);
            tuple.setSourcexCoordinate(this.getxCoordinate());
            tuple.setSourceyCoordinate(this.getyCoordinate());
            tuple.setUpLinkBandwidth(getUplinkBandwidth());
            //2.send tuple information to router
            sendUp(tuple);
        }
    }

    protected void handleAuctioneerResponse(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
        this.stateMap.put(tuple.getCloudletId(), copyMap(envState));
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
        int destinationId = tuple.getDestinationId();
        this.send(this.getId(), tuple.getParentDeadline(), FogEvents.FAIL_TASK, tuple);

//        tuple.setActualSourceId(this.getId());
        tuple.setTupleType(Env.TUPLE_TYPE_TASK);
        if (destinationId == this.getId()) {
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
        } else if (destinationId != this.getId()) {
            if (tuple.getDirection() == Tuple.UP) {
                tuple.setDestinationId(destinationId);
                tuple.setDestModuleName(Env.DEVICE_EDGE_SERVER);
                tuple.setSrcModuleName(Env.DEVICE_USER_EQUIPMENT);
//                tuple.setTupleType(Env.TUPLE_TYPE_TASK);
                //8. send tuple to router for offloading to edge server
                sendUp(tuple);
            } else if (tuple.getDirection() == Tuple.DOWN) {
                for (int childId : getChildrenIds())
                    sendDown(tuple, childId);
            }
        }
    }

    protected void handleResponse(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
        if (tuple.isFailed())
            return;
        if (this.taskGeneratorDevice != null) {
            this.taskGeneratorDevice.doneTuple(tuple);
        }

//        Tuple tuple = (Tuple) ev.getData();
//        if (this.taskGeneratorDeviceId > -1) {
//            sendNow(this.taskGeneratorDeviceId, FogEvents.DONE_TUPLE, tuple);
//        }
    }


    protected void handleEdgeServerInfo(SimEvent ev) {  }

    protected void handleTaskInfo(SimEvent ev) {  }

    public TaskGeneratorDevice getTaskGeneratorDevice() {
        return taskGeneratorDevice;
    }

    public void setTaskGeneratorDevice(TaskGeneratorDevice taskGeneratorDevice) {
        this.taskGeneratorDevice = taskGeneratorDevice;
    }

    protected void failTask(SimEvent ev) {
        Tuple tuple = (Tuple) ev.getData();
        tuple.setFailed(true);
        tuple.setExecutorId(tuple.getDestinationId());
        String executorName = "";
        for (String key : edgeServersMap.keySet()) {
            if (edgeServersMap.get(key).equals(tuple.getDestinationId())) {
                executorName = key;
            }
        }
        executorName = executorName.equals("") ? "UE" : executorName;
        tuple.setExecutorName(executorName);

        if (this.taskGeneratorDevice != null) {
            sendNow(this.taskGeneratorDevice.getId(), FogEvents.FAIL_TASK, tuple);
        }
    }


}
