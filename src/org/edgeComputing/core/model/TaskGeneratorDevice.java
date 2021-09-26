package org.edgeComputing.core.model;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.edgeComputing.model.Task;
import org.edgeComputing.model.status.TaskStatus;
import org.edgeComputing.outputWriter.Output;
import org.edgeComputing.outputWriter.Result;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.Distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class TaskGeneratorDevice extends Sensor {
    private List<Task> taskList;
    private List<Task> runningSubTasks;
    private List<Task> doneSubTasks;
    private String parentName;
    private Integer parentId;

    private boolean loop = false;
    public TaskGeneratorDevice(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
        super(name, tupleType, userId, appId, transmitDistribution);
        this.setAppId(appId);
        this.setTransmitDistribution(transmitDistribution);
        this.taskList = new ArrayList<>();
        this.runningSubTasks = new ArrayList<>();
        this.doneSubTasks = new ArrayList<>();
        setTupleType(tupleType);
        setSensorName(tupleType);
        setUserId(userId);
    }

    public TaskGeneratorDevice(String name, String tupleType, int userId, String appId, Distribution transmitDistribution,
                               List<Task> taskList) {
        super(name, tupleType, userId, appId, transmitDistribution);
        this.setAppId(appId);
        this.setTransmitDistribution(transmitDistribution);
        this.taskList = taskList;
        this.runningSubTasks = new ArrayList<>();
        this.doneSubTasks = new ArrayList<>();
        setTupleType(tupleType);
        setSensorName(tupleType);
        setUserId(userId);
    }

    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case FogEvents.TUPLE_ACK:
//                send(getId(), 1, FogEvents.TUPLE_ACK);
                break;
            case FogEvents.EMIT_TUPLE:
                transmit();
                if(!loop) {
//                    sendNow(getId(), FogEvents.TUPLE_ACK);
                    loop = true;
                }
                break;
            case FogEvents.DONE_TUPLE:
                doneTuple(ev);
                if(!endProcess()) {
                    sendNow(this.getId(), FogEvents.EMIT_TUPLE);
                }
                break;
        }

    }

    public void transmit() {
        AppEdge _edge = null;
        for (AppEdge edge : getApp().getEdges()) {
            if (edge.getSource().equals(getTupleType()))
                _edge = edge;
        }

        List<Task> subTasks = this.getReadySubTaskList();
        List<Tuple> tuples = new ArrayList<>();
        if(subTasks.size() == 0 && this.endProcess()){
            send(this.getGatewayDeviceId(), getLatency(), FogEvents.END_PROCESS);
        }
//        Logger.debug(getName(), subTasks.size() + " ready tuple found!");
        for (Task subTask : subTasks) {
            Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, subTask.getCpuLength(),
                    subTask.getPesNumber(), subTask.getNwLength(), subTask.getOutputSize(), new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            tuple.setUserId(getUserId());
            tuple.setTupleType(getTupleType());
            tuple.setTaskId(subTask.getId());
            tuple.setDestModuleName(_edge.getDestination());
            tuple.setSrcModuleName(getSensorName());
            tuple.setTupleBidPrice(subTask.getBidPrice());

            int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName());
            tuple.setActualTupleId(actualTupleId);
            tuple.setDestinationId(this.getGatewayDeviceId());

            tuples.add(tuple);
            subTask.setTuple(tuple);
            subTask.setStatus(TaskStatus.DOING);
            runningSubTasks.add(subTask);
//            Logger.debug(getName(), "Running task with taskId = " + subTask.getId());
            tuple.setCreationTime(CloudSim.clock());
        }
        for(Tuple tuple: tuples) {
//            Logger.debug(getName(), "Sending tuple with tupleId = " + tuple.getCloudletId());
            send(this.getGatewayDeviceId(), getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
        }
    }
    private int updateTimings(String src, String dest) {
        Application application = getApp();
        for (AppLoop loop : application.getLoops()) {
            if (loop.hasEdge(src, dest)) {
                int tupleId = TimeKeeper.getInstance().getUniqueId();
                if (!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
                    TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
                TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
                TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
                return tupleId;
            }
        }
        return -1;
    }

    private List<Task> getReadySubTaskList() {
        List<Task> readySubTasks = new ArrayList<>();
        List<Task> runningTasks = this.taskList.stream().filter(
                item -> item.getStatus().equals(TaskStatus.DOING)).collect(
                Collectors.toList());
        for (Task task : runningTasks) {
            List<Task> readySubTasksOfTask = task.getReadySubTask();
            if(task.getStatus().equals(TaskStatus.DONE)) {
                Logger.debug(getName(), "Task with id = " + task.getId() + " is finished!");
            }
            readySubTasks.addAll(readySubTasksOfTask);
        }
        List<Task> todoTasks = this.taskList.stream().filter(item -> item.getStatus().equals(TaskStatus.TODO)).collect(
                Collectors.toList());
        for (Task task : todoTasks) {
            List<Task> readySubTasksOfTask = task.getReadySubTask();
            readySubTasks.addAll(readySubTasksOfTask);
            task.setStatus(TaskStatus.DOING);
        }
        return readySubTasks;
    }



    public void doneTuple(SimEvent ev){
        Tuple tuple = (Tuple) ev.getData();
        List<Task> tasks = runningSubTasks.stream().filter(item -> item.getTuple().equals(tuple.getActualRequestedTuple())).collect(
                Collectors.toList());
        if(tasks.size() > 0){
            tasks.get(0).setStatus(TaskStatus.DONE);
            runningSubTasks.remove(tasks.get(0));
            doneSubTasks.add(tasks.get(0));
            tuple.setDoneTime(CloudSim.clock());
            printTupleInfo(tuple);
            Logger.debug(getName(), "SubTask with id = " + tasks.get(0).getId() + " is finished!");
        }
    }

    private void printTupleInfo(Tuple tuple){
//        Logger.debug(getName(), "Tuple with id: " + tuple.getActualRequestedTuple().getCloudletId() + " executed in " + tuple.getExecutionTime() + " on " + tuple.getExecutorName());
//        Logger.debug(getName(), "Tuple with id: " + tuple.getActualRequestedTuple().getCloudletId() +" execution Cost: " + tuple.getProcessingCost());
//        Logger.debug(getName(), "Tuple with id: " + tuple.getActualRequestedTuple().getCloudletId() + " processed in " + tuple.getDelay());
        double networkDelay = tuple.getNetworkDelay();
        networkDelay = tuple.getActualRequestedTuple() != null? networkDelay + tuple.getActualRequestedTuple().getNetworkDelay(): networkDelay;
//        Logger.debug(getName(), "Tuple with id: " + tuple.getActualRequestedTuple().getCloudletId() + " Network Delay: " + networkDelay);
        long transferData = tuple.getCloudletFileSize();
        transferData = tuple.getActualRequestedTuple() != null? transferData + tuple.getActualRequestedTuple().getCloudletFileSize(): transferData;
//        Logger.debug(getName(), "Tuple with id: " + tuple.getActualRequestedTuple().getCloudletId() + " Transfer Data: " + transferData);
        Output.resultList.add(new Result(tuple.getActualRequestedTuple().getTaskId(), tuple.getActualRequestedTuple().getCloudletId(), tuple.getExecutionTime(),
                tuple.getDelay(), networkDelay, tuple.getProcessingCost(), transferData, tuple.getExecutorName(),
                tuple.getExecutorId(), this.parentName, this.parentId));
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public boolean endProcess(){
        boolean endProcess = true;
        for(Task task : taskList){
            if(!task.getStatus().equals(TaskStatus.DONE)){
                endProcess = false;
                break;
            }
        }
        return endProcess;
    }

}

