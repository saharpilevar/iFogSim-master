package org.edgeComputing.outputWriter;

import org.cloudbus.cloudsim.core.CloudSim;
import org.edgeComputing.model.status.TaskStatus;


/**
 * Created by Sajad.Khosravi on 2021/08/14 @OffloadingEdgeComputing.
 */

public class SubTaskResult {
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
    private TaskStatus status;
    private Double upLinkBandwidth;
    private Double downLinkBandwidth;
    private Long mustBeTransferredData;
    private Double modifiedTime;

    public SubTaskResult(String taskId, Integer tupleId, Double execTime, Double delay, Double networkDelay, Double execCost,
                         Long transferData, Long mustBeTransferredData, String executor, Integer executorId, String owner, Integer ownerId,
                         TaskStatus status, Double upLinkBandwidth, Double downLinkBandwidth){
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
        this.status = status;
        this.upLinkBandwidth = upLinkBandwidth;
        this.downLinkBandwidth = downLinkBandwidth;
        this.mustBeTransferredData = mustBeTransferredData;
        this.modifiedTime = CloudSim.clock();
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Double getUpLinkBandwidth() {
        return upLinkBandwidth;
    }

    public void setUpLinkBandwidth(Double upLinkBandwidth) {
        this.upLinkBandwidth = upLinkBandwidth;
    }

    public Double getDownLinkBandwidth() {
        return downLinkBandwidth;
    }

    public void setDownLinkBandwidth(Double downLinkBandwidth) {
        this.downLinkBandwidth = downLinkBandwidth;
    }

    public Long getMustBeTransferredData() {
        return mustBeTransferredData;
    }

    public void setMustBeTransferredData(Long mustBeTransferredData) {
        this.mustBeTransferredData = mustBeTransferredData;
    }

    public Double getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Double modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
