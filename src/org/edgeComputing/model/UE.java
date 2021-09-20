package org.edgeComputing.model;

import java.util.List;

public class UE extends EdgeEntity{
    private int areaId;
    private List<Task> tasks;
    private double transmissionPower;


    public UE(){
        super();
    }

    public UE(int areaId, List<Task> tasks, double transmissionPower){
        super();
        this.areaId = areaId;
        this.tasks = tasks;
        this.transmissionPower=transmissionPower;
    }


    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Double getTransmissionPower() {
        return transmissionPower;
    }

    public void setTransmissionPower(Double transmissionPower) {
        this.transmissionPower = transmissionPower;
    }

}
