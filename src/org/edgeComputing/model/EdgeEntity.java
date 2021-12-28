package org.edgeComputing.model;

import org.cloudbus.cloudsim.power.models.PowerModel;

public class EdgeEntity {
    protected String nodeName;
    protected Long mips;
    protected Integer ram;
    protected Long upBw;
    protected Long downBw;
    protected Integer level;
    protected Double ratePerMips;
    protected Double busyPower;
    protected Double idlePower;
    private Double upLinkLatency;
    protected Double xCoordinate;
    protected Double yCoordinate;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getMips() {
        return mips;
    }

    public void setMips(Long mips) {
        this.mips = mips;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Long getUpBw() {
        return upBw;
    }

    public void setUpBw(Long upBw) {
        this.upBw = upBw;
    }

    public Long getDownBw() {
        return downBw;
    }

    public void setDownBw(Long downBw) {
        this.downBw = downBw;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Double getRatePerMips() {
        return ratePerMips;
    }

    public void setRatePerMips(Double ratePerMips) {
        this.ratePerMips = ratePerMips;
    }

    public Double getBusyPower() {
        return busyPower;
    }

    public void setBusyPower(Double busyPower) {
        this.busyPower = busyPower;
    }


    public Double getIdlePower() {
        return idlePower;
    }

    public void setIdlePower(Double idlePower) {
        this.idlePower = idlePower;
    }

    public Double getUpLinkLatency() {
        return upLinkLatency;
    }

    public void setUpLinkLatency(Double upLinkLatency) {
        this.upLinkLatency = upLinkLatency;
    }

    public Double getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(Double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }
    public Double getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(Double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }
}
