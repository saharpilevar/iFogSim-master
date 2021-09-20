package org.edgeComputing.model;

import org.edgeComputing.model.status.TaskStatus;
import org.fog.entities.Tuple;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

public class Task {
    private String id;
    private List<Task> subTasks;
    private Long cpuLength = 1000L;
    private Long nwLength = 1000L;
    private Integer pesNumber = 1;
    private TaskStatus status;
    private Long outputSize = 0L;
    private Tuple tuple;

    public Task(String id) {
        this.id = id;
        this.status = TaskStatus.TODO;
    }

    public Task(String id, Long cpuLength, Long nwLength, Integer pesNumber, Long outputSize) {
        this.id = id;
        this.cpuLength = cpuLength;
        this.nwLength = nwLength;
        this.pesNumber = pesNumber;
        this.status = TaskStatus.TODO;
        this.outputSize = outputSize;
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
}
