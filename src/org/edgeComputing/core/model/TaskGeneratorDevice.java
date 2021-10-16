package org.edgeComputing.core.model;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.edgeComputing.model.Task;
import org.edgeComputing.model.status.TaskStatus;
import org.edgeComputing.outputWriter.Output;
import org.edgeComputing.outputWriter.SubTaskResult;
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

import java.util.*;
import java.util.stream.Collectors;



public class TaskGeneratorDevice extends Sensor {
    private List<Task> taskList;
    private List<Task> runningSubTasks;
    private List<Task> doneSubTasks;
    private String parentName;
    private Integer parentId;
    private boolean endProcessFlag = false;
    private Long doneTasksCount = 0L;
    private Long failedTasksCount = 0L;

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

            case FogEvents.FAIL_TASK:
                failTask(ev);
                sendNow(this.getId(), FogEvents.EMIT_TUPLE);
                break;
        }

    }

    public void transmit() {
        AppEdge _edge = null;
        for (AppEdge edge : getApp().getEdges()) {
            if (edge.getSource().equals(getTupleType()))
                _edge = edge;
        }

//        List<Task> subTasks1 = this.getReadySubTaskList();
        Map<String, List<Task>> subTasks = this.getReadySubTaskList();

        List<Tuple> tuples = new ArrayList<>();
//        if(subTasks.size() == 0 && this.endProcess()){
//            send(this.getGatewayDeviceId(), getLatency(), FogEvents.END_PROCESS);
//        }
//        Logger.debug(getName(), subTasks.size() + " ready tuple found!");
        for (String taskId : subTasks.keySet()) {
            for (Task subTask : subTasks.get(taskId)) {
                Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, subTask.getCpuLength(),
                        subTask.getPesNumber(), subTask.getNwLength(), subTask.getOutputSize(), new UtilizationModelFull(),
                        new UtilizationModelFull(), new UtilizationModelFull());
                tuple.setUserId(getUserId());
                tuple.setTupleType(getTupleType());
                tuple.setTaskId(subTask.getId());
                tuple.setDestModuleName(_edge.getDestination());
                tuple.setSrcModuleName(getSensorName());
                tuple.setTupleBidPrice(subTask.getBidPrice());
                tuple.setParentTaskId(taskId);
//                tuple.setActualToBeTransferredData(subTask.getNwLength() + subTask.getOutputSize());
                if (subTask.getStatus().equals(TaskStatus.FAILED)) {
                    tuple.setFailed(true);
                }
                Task task = taskList.stream().filter(item -> item.getId().equals(taskId)).collect(
                        Collectors.toList()).get(0);
                if (task.getEndTaskId().equals(subTask.getId())) {
                    tuple.setDoneTuple(true);
                }
            tuple.setParentDeadline(task.getDeadline());


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
        }
        for(Tuple tuple: tuples) {
//            Logger.debug(getName(), "Sending tuple with tupleId = " + tuple.getCloudletId());

            //1.send tuple to mobileDevice
            send(this.getGatewayDeviceId(), getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
        }
        if (tuples.size() == 0 && this.endProcess() && !endProcessFlag) {
            this.endProcessFlag = true;
            send(this.getGatewayDeviceId(), getLatency(), FogEvents.END_PROCESS);
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

    private Map<String, List<Task>> getReadySubTaskList() {
        Map<String, List<Task>> readySubTasks = new HashMap<>();
        List<Task> runningTasks = this.taskList.stream().filter(
                item -> item.getStatus().equals(TaskStatus.DOING)).collect(
                Collectors.toList());
        for (Task task : runningTasks) {
            List<Task> readySubTasksOfTask = task.getReadySubTask();
            if(task.getStatus().equals(TaskStatus.DONE)) {
                Logger.debug(getName(), "Task with id = " + task.getId() + " is finished!");
                doneTasksCount++;
                Output.successfulTasks.add(task.getId());
                Logger.debug(getName(), doneTasksCount + " Tasks is finished!");
            }
            readySubTasks.put(task.getId(), readySubTasksOfTask);
        }
        List<Task> todoTasks = this.taskList.stream().filter(item -> item.getStatus().equals(TaskStatus.TODO)).collect(
                Collectors.toList());
        for (Task task : todoTasks) {
            List<Task> readySubTasksOfTask = task.getReadySubTask();
            readySubTasks.put(task.getId(), readySubTasksOfTask);
            task.setStatus(TaskStatus.DOING);
        }
        return readySubTasks;
    }



    public void doneTuple(Tuple tuple){
        List<Task> tasks = runningSubTasks.stream().filter(
                item -> item.getTuple().equals(tuple.getActualRequestedTuple())).collect(
                Collectors.toList());
        if(tasks.size() > 0){
            tasks.get(0).setStatus(TaskStatus.DONE);
            updateTaskDependenciesInfo(tuple, tasks.get(0).getId());
            runningSubTasks.remove(tasks.get(0));
            doneSubTasks.add(tasks.get(0));
            tuple.setDoneTime(CloudSim.clock());
            printTupleInfo(tuple);
            Logger.debug(getName(), "SubTask with id = " + tasks.get(0).getId() + " is finished!");
            Logger.debug(getName(), doneSubTasks.size() + " SubTask is finished!");
        }
        if (!endProcess()) {
            sendNow(this.getId(), FogEvents.EMIT_TUPLE);
        }
    }

    private void printTupleInfo(Tuple tuple){
        double networkDelay = tuple.getNetworkDelay();
        networkDelay = tuple.getActualRequestedTuple() != null ? networkDelay + tuple.getActualRequestedTuple().getNetworkDelay() : networkDelay;
        long transferData = 0;
        if (tuple.getExecutorId() != this.parentId) {
            transferData = tuple.getCloudletFileSize();
            transferData = tuple.getActualRequestedTuple() != null ? transferData + tuple.getActualRequestedTuple().getCloudletFileSize() : transferData;
        }
        TaskStatus status = tuple.isFailed() ? TaskStatus.FAILED : TaskStatus.DONE;
        Tuple actualTuple = tuple.isFailed() ? tuple : tuple.getActualRequestedTuple();
        double execTime = tuple.isFailed() ? 0.0 : tuple.getExecutionTime();
        List<Task> tasks = doneSubTasks.stream().filter(item -> item.getId().equals(actualTuple.getTaskId())).collect(
                Collectors.toList());
        long mustBeTransferredData = 0;
        if (tasks.size() > 0) {
            mustBeTransferredData = tasks.get(0).getOutputSize() + tasks.get(0).getNwLength();
        }
        Output.subTaskResultList.add(new SubTaskResult(actualTuple.getTaskId(), actualTuple.getCloudletId(), execTime,
                tuple.getDelay(), networkDelay, tuple.getProcessingCost(), transferData, mustBeTransferredData, tuple.getExecutorName(),
                tuple.getExecutorId(), this.parentName, this.parentId, status, actualTuple.getUpLinkBandwidth(),
                actualTuple.getDownLinkBandwidth()));
        Output.sampleData(CloudSim.clock());
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
            if (!task.getStatus().equals(TaskStatus.DONE) && !task.getStatus().equals(TaskStatus.FAILED)) {
                endProcess = false;
                break;
            }
        }
        return endProcess;
    }

    public void updateTaskDependenciesInfo(Tuple tuple, String subTaskId) {
        List<Task> tasks = this.taskList.stream().filter(
                item -> item.getId().equals(tuple.getActualRequestedTuple().getParentTaskId())).collect(
                Collectors.toList());
        if (tasks.size() > 0) {
            tasks.get(0).updateTaskDependenciesInfo(subTaskId);
        }
    }
    public void failTask(SimEvent ev) {
        Tuple tuple = (Tuple) ev.getData();
        Tuple actualTuple = tuple.getActualRequestedTuple() != null ? tuple.getActualRequestedTuple() : tuple;
        List<Task> tasks = runningSubTasks.stream().filter(item -> item.getTuple().equals(actualTuple)).collect(
                Collectors.toList());
        if (tasks.size() > 0) {
            tasks.get(0).setStatus(TaskStatus.FAILED);
            failSubTasks(tuple);
            runningSubTasks.remove(tasks.get(0));
            printTupleInfo(tuple);
            Logger.debug(getName(), "SubTask with id = " + tasks.get(0).getId() + " is failed!");
        }
    }
    public void failSubTasks(Tuple tuple) {
        Tuple actualTuple = tuple.getActualRequestedTuple() != null ? tuple.getActualRequestedTuple() : tuple;
        List<Task> tasks = this.taskList.stream().filter(
                item -> item.getId().equals(actualTuple.getParentTaskId())).collect(
                Collectors.toList());
        if (tasks.size() > 0) {
//            tasks.get(0).getSubTasks().failTasks();
            tasks.get(0).setStatus(TaskStatus.FAILED);
            Logger.debug(getName(), "Task with id = " + tasks.get(0).getId() + " is failed!");
            failedTasksCount++;
            Output.failedTasks.put(tasks.get(0).getId(), actualTuple.getTaskId());
            Logger.debug(getName(), failedTasksCount + " Tasks is failed!");
        }
    }
    public List<Task> getTaskList() {
        return taskList;
    }

}

