package org.edgeComputing.model;

public class DeviceInfo {
    private String name;
    private Integer id;
    private Double time;
    private Double energyConsumption;
    private Double lastUtilization;
    private Double networkDelay;
    private Double mips;
    private Integer ram;
    private Double bidPrice;
    private double xCoordinate;
    private double yCoordinate;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(Double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public Double getLastUtilization() {
        return lastUtilization;
    }

    public void setLastUtilization(Double lastUtilization) {
        this.lastUtilization = lastUtilization;
    }

    public Double getNetworkDelay() {
        return networkDelay;
    }

    public void setNetworkDelay(Double networkDelay) {
        this.networkDelay = networkDelay;
    }
    public Double getMips() {
        return mips;
    }

    public void setMips(Double mips) {
        this.mips = mips;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public double getxCoordinate() {
        return xCoordinate;}
    public void setxCoordinate(double xCoordinate) {
        this.xCoordinate = xCoordinate;}
    public double getyCoordinate() {
        return yCoordinate;}
    public void setyCoordinate(double yCoordinate) {
        this.yCoordinate = yCoordinate;}

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", time=" + time +
                ", energyConsumption=" + energyConsumption +
                ", lastUtilization=" + lastUtilization +
                ", networkDelay=" + networkDelay +
                '}';
    }
}

