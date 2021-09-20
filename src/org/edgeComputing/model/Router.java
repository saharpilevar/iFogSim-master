package org.edgeComputing.model;

public class Router extends EdgeEntity{
    public Router(){
        super();
    }

    public Router(String nodeName){
        this.nodeName = nodeName;
        this.mips = 280000L;
        this.ram = 32000;
        this.upBw = 100000L;
        this.downBw = 100000L;
        this.level = 1;
        this.ratePerMips = 0.5;
        this.busyPower = 107.339;
        this.idlePower = 83.4333;
    }
}
