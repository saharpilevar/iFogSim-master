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

//        Logger.debug(getName(),
//                "Received tuple " + tuple.getCloudletId() + "with tupleType = " + tuple.getTupleType() + "\t| Source : " +
//                        CloudSim.getEntityName(ev.getSource()) + "|Dest : " + CloudSim.getEntityName(
//                        ev.getDestination()));

        if(tuple.getTupleType().equals(Env.TUPLE_TYPE_TASK)){
            this.handleTasks(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_RESPONSE)){
            this.handleResponse(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE)){
            this.handleAuctioneerResponse(ev);
        }
        else if(tuple.getTupleType().equals(Env.TUPLE_TYPE_TASK_INFO)){
            this.handleTaskInfo(ev);
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




}
