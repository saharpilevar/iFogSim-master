package org.edgeComputing.core.model;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.edgeComputing.Env;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Device extends FogDevice {
    private Map<Integer, Tuple> tupleCloudletMap = new HashMap<>();
    public Device(String name, FogDeviceCharacteristics characteristics,
                  VmAllocationPolicy vmAllocationPolicy,
                  List<Storage> storageList, double schedulingInterval,
                  double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency,
                  double ratePerMips) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth,
                downlinkBandwidth, uplinkLatency, ratePerMips);
    }

    public Device(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips,
                  PowerModel powerModel) throws Exception {
        super(name, mips, ram, uplinkBandwidth, downlinkBandwidth, ratePerMips, powerModel);
    }
    public void addTupleCloudletMap(int cloudletId, Tuple tuple) {
        this.tupleCloudletMap.put(cloudletId, tuple);
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case FogEvents.TUPLE_ARRIVAL:
                processTupleArrival(ev);
                break;
            default:
                super.processOtherEvent(ev);
                break;
        }
    }

    @Override
    protected void processTupleArrival(SimEvent ev) {
        Tuple tuple = (Tuple) ev.getData();
        send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);

         if(tuple.getTupleType().equals(Env.TUPLE_TYPE_TASK_INFO)){
            this.handleTaskInfo(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_TASK)){
            this.handleTasks(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_RESPONSE)){
            this.handleResponse(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE)){
            this.handleAuctioneerResponse(ev);
        }

        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_EDGE_SERVER_INFO)){
            this.handleEdgeServerInfo(ev);
        }
    }

    protected abstract void handleTasks(SimEvent ev);

    protected abstract void handleResponse(SimEvent ev);

    protected abstract void handleAuctioneerResponse(SimEvent ev);

    protected abstract void handleTaskInfo(SimEvent ev);

    protected abstract void handleEdgeServerInfo(SimEvent ev);

    @Override
    protected void checkCloudletCompletion() {
        boolean cloudletCompleted = false;
        List<? extends Host> list = getVmAllocationPolicy().getHostList();
        for (int i = 0; i < list.size(); i++) {
            Host host = list.get(i);
            for (Vm vm : host.getVmList()) {
                while (vm.getCloudletScheduler().isFinishedCloudlets()) {
                    Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
                    if (cl != null) {
                        cloudletCompleted = true;
                        Tuple tuple = (Tuple)cl;
                        TimeKeeper.getInstance().tupleEndedExecution(tuple);
                        Application application = getApplicationMap().get(tuple.getAppId());
//                        Logger.debug(getName(), "Completed execution of tuple "+tuple.getCloudletId()+"on "+tuple.getDestModuleName());
                        List<Tuple> resultantTuples = application.getResultantTuples(tuple.getDestModuleName(), tuple, getId(), vm.getId());
                        for(Tuple resTuple : resultantTuples){
                            resTuple.setModuleCopyMap(new HashMap<String, Integer>(tuple.getModuleCopyMap()));
                            resTuple.getModuleCopyMap().put(((AppModule)vm).getName(), vm.getId());
                            Tuple tpl = tupleCloudletMap.get(cl.getCloudletId());
                            resTuple.setSourceDeviceId(tpl.getSourceDeviceId());
                            resTuple.setDestinationId(tpl.getActualSourceId());
                            resTuple.setActualRequestedTuple(tpl);
                            resTuple.setExecutorName(tpl.getExecutorName());
                            resTuple.setExecutorId(tpl.getExecutorId());
                            resTuple.setTaskId(tpl.getTaskId());
                            resTuple.setParentTaskId(tpl.getParentTaskId());

                            updateTimingsOnSending(resTuple);
                            sendToSelf(resTuple);
                        }
                        sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
                    }
                }
            }
        }
        if(cloudletCompleted)
            updateAllocatedMips(null);
    }

    @Override
    protected void sendUpFreeLink(Tuple tuple){
        double networkDelay = tuple.getCloudletFileSize()/getUplinkBandwidth();
        setNorthLinkBusy(true);
        tuple.setNetworkDelay(tuple.getNetworkDelay()+ networkDelay+ getUplinkLatency());
        send(getId(), networkDelay, FogEvents.UPDATE_NORTH_TUPLE_QUEUE);
        send(parentId, networkDelay+getUplinkLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
        NetworkUsageMonitor.sendingTuple(getUplinkLatency(), tuple.getCloudletFileSize());
    }

    @Override
    protected void sendDownFreeLink(Tuple tuple, int childId){
        double networkDelay = tuple.getCloudletFileSize()/getDownlinkBandwidth();
        //Logger.debug(getName(), "Sending tuple with tupleType = "+tuple.getTupleType()+" DOWN");
        setSouthLinkBusy(true);
        double latency = getChildToLatencyMap().get(childId);
        tuple.setNetworkDelay(tuple.getNetworkDelay()+ networkDelay+ latency);
        send(getId(), networkDelay, FogEvents.UPDATE_SOUTH_TUPLE_QUEUE);
        send(childId, networkDelay+latency, FogEvents.TUPLE_ARRIVAL, tuple);
        NetworkUsageMonitor.sendingTuple(latency, tuple.getCloudletFileSize());
    }


}
