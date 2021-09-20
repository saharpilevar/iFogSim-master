package org.edgeComputing.outputWriter;

public class Result {
    private String taskId;
    private Integer tupleId;
    private Double execTime;
    private Double delay;
    private Double networkDelay;
    private Double execCost;
    private Long transferData;
    private String executor;
    private Integer executorId;
    private Integer ownerId;
    private String owner;

    public Result(String taskId, Integer tupleId, Double execTime, Double delay, Double networkDelay, Double execCost,
                  Long transferData, String executor, Integer executorId, String owner, Integer ownerId){
        this.taskId = taskId;
        this.tupleId = tupleId;
        this.execTime = execTime;
        this.delay = delay;
        this.networkDelay = networkDelay;
        this.execCost = execCost;
        this.transferData = transferData;
        this.executor = executor;
        this.executorId = executorId;
        this.owner = owner;
        this.ownerId = ownerId;
    }

    public Integer getTupleId() {
        return tupleId;
    }

    public void setTupleId(Integer tupleId) {
        this.tupleId = tupleId;
    }

    public Double getExecTime() {
        return execTime;
    }

    public void setExecTime(Double execTime) {
        this.execTime = execTime;
    }

    public Double getDelay() {
        return delay;
    }

    public void setDelay(Double delay) {
        this.delay = delay;
    }

    public Double getNetworkDelay() {
        return networkDelay;
    }

    public void setNetworkDelay(Double networkDelay) {
        this.networkDelay = networkDelay;
    }

    public Double getExecCost() {
        return execCost;
    }

    public void setExecCost(Double execCost) {
        this.execCost = execCost;
    }

    public Long getTransferData() {
        return transferData;
    }

    public void setTransferData(Long transferData) {
        this.transferData = transferData;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Integer getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Integer executorId) {
        this.executorId = executorId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}

