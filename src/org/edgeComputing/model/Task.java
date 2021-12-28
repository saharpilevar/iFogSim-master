package org.edgeComputing.model;

import org.apache.commons.math3.util.Pair;
import org.edgeComputing.model.status.TaskStatus;
import org.fog.entities.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class Task {
    private String id;
    private List<Task> subTasks;
    private String startSubTaskId;
    private String endSubTaskId;
    private Long cpuLength = 1000L;
    private Long nwLength = 1000L;
    private Integer pesNumber = 1;
    private TaskStatus status;
    private Long outputSize = 0L;
    private Tuple tuple;
    private double bidPrice;
    private Double deadline = 10000000000000000000.0D;
    private Map<String, Pair<Integer, Long>> previousTuplesInfo = new HashMap<>();


    public Task(String id) {
        this.id = id;
        this.status = TaskStatus.TODO;
    }

    public Task(String id, Long cpuLength, Long nwLength, Integer pesNumber, Long outputSize, double bidPrice, String startSubTaskId, String endSubTaskId) {
        this.id = id;
        this.cpuLength = cpuLength;
        this.nwLength = nwLength;
        this.pesNumber = pesNumber;
        this.status = TaskStatus.TODO;
        this.outputSize = outputSize;
        this.bidPrice = bidPrice;
        this.startSubTaskId = startSubTaskId;
        this.endSubTaskId = endSubTaskId;
    }

    public List<Task> getReadySubTask() {
        List<Task> readySubTasks = this.getReadyTask();
        if(readySubTasks.size() == 0){
            if(this.isFinished()){
                this.status = TaskStatus.DONE;
            }
        }
        return readySubTasks;
    }

    public List<Task> getReadyTask(){
        List<Task> readyTasks = new ArrayList<>();
        List<Task> todoTasks = this.subTasks.stream().filter(item -> item.getStatus().equals(TaskStatus.TODO)).collect(
                Collectors.toList());
        for(Task task: todoTasks){
            readyTasks.add(task);
        }
        return readyTasks;
    }
    public boolean isFinished(){
        boolean isFinished = this.subTasks.stream().allMatch(item -> item.getStatus().equals(TaskStatus.DONE));
        return isFinished;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getCpuLength() {
        return cpuLength;
    }

    public void setCpuLength(Long cpuLength) {
        this.cpuLength = cpuLength;
    }

    public Long getNwLength() {
        return nwLength;
    }

    public void setNwLength(Long nwLength) {
        this.nwLength = nwLength;
    }

    public Integer getPesNumber() {
        return pesNumber;
    }

    public void setPesNumber(Integer pesNumber) {
        this.pesNumber = pesNumber;
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<Task> subTasks) {
        this.subTasks = subTasks;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }



    public Long getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(Long outputSize) {
        this.outputSize = outputSize;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    public Tuple getTuple() {
        return tuple;
    }

    public void setTuple(Tuple tuple) {
        this.tuple = tuple;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndTaskId() {
        return this.endSubTaskId;
    }

    public String getStartTaskId() {
        return this.startSubTaskId;
    }

    public Double getDeadline() {
        return deadline;
    }

    public void setDeadline(Double deadline) {
        this.deadline = deadline;
    }


    public Map<String, Pair<Integer, Long>> getPreviousTuplesInfo() {
        return previousTuplesInfo;
    }
    public void setPreviousTupleInfo(String id, Integer executorId, Long outputSize) {
        this.previousTuplesInfo.put(id, new Pair<>(executorId, outputSize));
    }

    public void updateTaskDependenciesInfo(String subTaskId) {
        List<Task> subTasks = this.subTasks.stream().filter(item -> item.getId().equals(subTaskId)).collect(
                Collectors.toList());
        if (subTasks.size() > 0) {
            for (Task subTask : subTasks) {
                subTask.setPreviousTupleInfo(subTask.getId(), subTask.getTuple().getExecutorId(),
                        subTask.getOutputSize());
            }
        }
    }
}
