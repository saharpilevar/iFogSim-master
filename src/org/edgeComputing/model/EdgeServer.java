package org.edgeComputing.model;

public class EdgeServer extends EdgeEntity{
    private int areaId;
    private double joinDelay;
    private double bidPrice;

    public EdgeServer(){
        super();
    }

    public EdgeServer(int areaId){
        super();
        this.areaId = areaId;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public double getJoinDelay() {
        return joinDelay;
    }

    public void setJoinDelay(double joinDelay) {
        this.joinDelay = joinDelay;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }
}
