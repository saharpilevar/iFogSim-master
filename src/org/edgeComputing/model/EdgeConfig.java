package org.edgeComputing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EdgeConfig {
    private int numberOfUsers;
    private String appId;
    private int numberOfEdgeArea;
    private List<Integer> numberOfServerPerEdgeArea;
    private List<Integer> numberOfUEPerEdgeArea;
    private int maxServerPerEdgeArea;
    private int maxUEPerEdgeArea;

    public EdgeConfig(int numberOfUsers, String appId){
        this.numberOfUsers = numberOfUsers;
        this.appId = appId;
        this.numberOfEdgeArea = 1;
        this.numberOfServerPerEdgeArea = new ArrayList<>();
        this.numberOfUEPerEdgeArea = new ArrayList<>();
        this.maxServerPerEdgeArea = 10;
        this.maxUEPerEdgeArea = 10;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getNumberOfEdgeArea() {
        return numberOfEdgeArea;
    }

    public void setNumberOfEdgeArea(int numberOfEdgeArea) {
        this.numberOfEdgeArea = numberOfEdgeArea;
    }

    public List<Integer> getNumberOfServerPerEdgeArea() {
        return numberOfServerPerEdgeArea;
    }

    public void setNumberOfServerPerEdgeArea(List<Integer> numberOfServerPerEdgeArea) {
        this.numberOfServerPerEdgeArea = numberOfServerPerEdgeArea;
    }

    public void setNumberOfServerPerEdgeAreaRandom(){
        for(int i=0; i<numberOfEdgeArea; i++){
            Random rand = new Random();
            int int_random = rand.nextInt(this.maxServerPerEdgeArea + 1);
            this.numberOfServerPerEdgeArea.add(int_random);
        }
    }

    public void setNumberOfServerPerEdgeAreaConstant(int value){
        for(int i=0; i<this.numberOfEdgeArea; i++){
            this.numberOfServerPerEdgeArea.add(value);
        }
    }

    public List<Integer> getNumberOfUEPerEdgeArea() {
        return numberOfUEPerEdgeArea;
    }

    public void setNumberOfUEPerEdgeArea(List<Integer> numberOfUEPerEdgeArea) {
        this.numberOfUEPerEdgeArea = numberOfUEPerEdgeArea;
    }

    public void setNumberOfUEPerEdgeAreaRandom() {
        for(int i=0; i<this.numberOfEdgeArea; i++){
            Random rand = new Random();
            int int_random = rand.nextInt(this.maxServerPerEdgeArea + 1);
            this.numberOfUEPerEdgeArea.add(int_random);
        }
    }

    public void setNumberOfUEPerEdgeAreaConstant(int value) {
        for(int i=0; i<this.numberOfEdgeArea; i++){
            this.numberOfUEPerEdgeArea.add(value);
        }
    }

    public int getMaxServerPerEdgeArea() {
        return maxServerPerEdgeArea;
    }

    public void setMaxServerPerEdgeArea(int maxServerPerEdgeArea) {
        this.maxServerPerEdgeArea = maxServerPerEdgeArea;
    }

    public int getMaxUEPerEdgeArea() {
        return maxUEPerEdgeArea;
    }

    public void setMaxUEPerEdgeArea(int maxUEPerEdgeArea) {
        this.maxUEPerEdgeArea = maxUEPerEdgeArea;
    }
}
