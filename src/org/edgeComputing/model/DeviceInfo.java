package org.edgeComputing.model;

public class DeviceInfo {
    private String name;
    private Integer id;
    private Double time;
    private Double energyConsumption;
    private Double lastUtilization;
    private Double networkDelay;
    private Long mips;
    private Integer ram;
    private Double bidPrice;


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
    public long getMips() {
        return mips;
    }

    public void setMips(long mips) {
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

