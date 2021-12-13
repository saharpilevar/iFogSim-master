package org.edgeComputing.outputWriter;

import org.edgeComputing.Env;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.edgeComputing.model.DeviceInfo;
import org.edgeComputing.util.*;
import org.fog.entities.FogDevice;
import org.fog.utils.Config;
public class Output {
    public static List<SubTaskResult> subTaskResultList = new ArrayList<>();
    public static List<String> successfulTasks = new ArrayList<>();
    public static Map<String, String> failedTasks = new HashMap<>();
    public static Map<Double, List<Double>> latencyMap = new HashMap<>();
    public static Map<Double, List<Long>> transferredDataMap = new HashMap<>();
    public static Map<Double, List<Long>> mustBeTransferredDataMap = new HashMap<>();
    public static Map<Double, Map<String, Double>> consumedEnergyMap = new HashMap<>();
    public static boolean isWritten = false;

    public static void writeResult() {
        writeSubTasksInfo();
        writeConsumedEnergy();
        writeTransferredDataInfo();
        writeLatencyInfo();
        writeTasksInfo();
    }

    public static void writeSubTasksInfo(){
        try (PrintWriter writer = new PrintWriter(new File(
                Config.getOutputPath() + Config.OS_SEPARATOR + "_SubTasksInfo.csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append("taskId");
            sb.append(',');
            sb.append("tupleId");
            sb.append(',');
            sb.append("execTime");
            sb.append(',');
            sb.append("delay");
            sb.append(',');
            sb.append("networkDelay");
            sb.append(',');
            sb.append("execCost");
            sb.append(',');
            sb.append("transferData");
            sb.append(',');
            sb.append("executor");
            sb.append(',');
            sb.append("executorId");
            sb.append(',');
            sb.append("owner");
            sb.append(',');
            sb.append("ownerId");
            sb.append(',');
            sb.append("status");
            sb.append(',');
            sb.append("upLinkBandwidth");
            sb.append(',');
            sb.append("downLinkBandwidth");
            sb.append(',');
            sb.append("modifiedTime");
            sb.append('\n');

            for (SubTaskResult subTaskResult : subTaskResultList) {
                sb.append(subTaskResult.getTaskId());
                sb.append(',');
                sb.append(subTaskResult.getTupleId());
                sb.append(',');
                sb.append(subTaskResult.getExecTime());
                sb.append(',');
                sb.append(subTaskResult.getDelay());
                sb.append(',');
                sb.append(subTaskResult.getNetworkDelay());
                sb.append(',');
                sb.append(subTaskResult.getExecCost());
                sb.append(',');
                sb.append(subTaskResult.getTransferData());
                sb.append(',');
                sb.append(subTaskResult.getExecutor());
                sb.append(',');
                sb.append(subTaskResult.getExecutorId());
                sb.append(',');
                sb.append(subTaskResult.getOwner());
                sb.append(',');
                sb.append(subTaskResult.getOwnerId());
                sb.append(',');
                sb.append(subTaskResult.getStatus().toString());
                sb.append(',');
                sb.append(subTaskResult.getUpLinkBandwidth());
                sb.append(',');
                sb.append(subTaskResult.getDownLinkBandwidth());
                sb.append(',');
                sb.append(subTaskResult.getModifiedTime());
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }
    public static void writeConsumedEnergy() {
        try (PrintWriter writer = new PrintWriter(new File(
                Config.getOutputPath() + Config.OS_SEPARATOR + "_ConsumedEnergy.csv"))) {

            List<String> deviceNames = Env.deviceInfoList.stream().map(DeviceInfo::getName).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            sb.append("time");
            sb.append(",");
            for (String deviceName : deviceNames) {
                sb.append(deviceName);
                sb.append(",");
            }
            sb.append("total");
            sb.append('\n');

            for(double time : consumedEnergyMap.keySet()) {
                double totalEnergyConsumption = 0.0;
                sb.append(time);
                sb.append(",");
                for (String deviceName : deviceNames) {
                    sb.append(consumedEnergyMap.get(time).get(deviceName));
                    sb.append(',');
                    totalEnergyConsumption += consumedEnergyMap.get(time).get(deviceName);
                }
                sb.append(totalEnergyConsumption);
                sb.append("\n");
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void writeTransferredDataInfo() {
        try (PrintWriter writer = new PrintWriter(new File(
                Config.getOutputPath() + Config.OS_SEPARATOR + "_TransferredData.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("time");
            sb.append(',');
            sb.append("sum");
            sb.append(',');
            sb.append("avg");
            sb.append(',');
            sb.append("std");
            sb.append(',');
            sb.append("var");
            sb.append(',');
            sb.append("mustBeTransferredSum");
            sb.append(',');
            sb.append("mustBeTransferredAvg");
            sb.append(',');
            sb.append("mustBeTransferredStd");
            sb.append(',');
            sb.append("mustBeTransferredVar");
            sb.append('\n');

            for(double time: transferredDataMap.keySet()) {
                List<Long> transferredDataList = transferredDataMap.get(time);
                List<Long> mustBeTransferredDataList = mustBeTransferredDataMap.get(time);
                double sum = MathTools.sumLong(transferredDataList);
                double sumMustBeTransferredDataList = MathTools.sumLong(mustBeTransferredDataList);
                double mean = MathTools.meanLong(transferredDataList);
                double meanMustBeTransferredDataList = MathTools.meanLong(mustBeTransferredDataList);
                double std = MathTools.standardDeviationLong(transferredDataList);
                double stdMustBeTransferredDataList = MathTools.standardDeviationLong(mustBeTransferredDataList);
                double var = MathTools.varianceLong(transferredDataList);
                double varMustBeTransferredDataList = MathTools.varianceLong(mustBeTransferredDataList);
                sb.append(time);
                sb.append(',');
                sb.append(sum);
                sb.append(',');
                sb.append(mean);
                sb.append(',');
                sb.append(std);
                sb.append(',');
                sb.append(var);
                sb.append(',');
                sb.append(sumMustBeTransferredDataList);
                sb.append(',');
                sb.append(meanMustBeTransferredDataList);
                sb.append(',');
                sb.append(stdMustBeTransferredDataList);
                sb.append(',');
                sb.append(varMustBeTransferredDataList);
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void writeTasksInfo() {
        try (PrintWriter writer = new PrintWriter(new File(
                Config.getOutputPath() + Config.OS_SEPARATOR + "_TasksInfo.csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append("taskId");
            sb.append(',');
            sb.append("status");
            sb.append(',');
            sb.append("failedSubTaskId");
            sb.append('\n');

            for (String taskId : successfulTasks) {
                sb.append(taskId);
                sb.append(',');
                sb.append("DONE");
                sb.append(',');
                sb.append(-1);
                sb.append('\n');
            }
            for (String taskId : failedTasks.keySet()) {
                sb.append(taskId);
                sb.append(',');
                sb.append("FAILED");
                sb.append(',');
                sb.append(failedTasks.get(taskId));
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void writeLatencyInfo() {
        try (PrintWriter writer = new PrintWriter(new File(
                Config.getOutputPath() + Config.OS_SEPARATOR + "_TasksLatencyInfo.csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append("time");
            sb.append(',');
            sb.append("sum");
            sb.append(',');
            sb.append("avg");
            sb.append(',');
            sb.append("std");
            sb.append(',');
            sb.append("var");
            sb.append('\n');

            for(double time: latencyMap.keySet()) {
                List<Double> latencyList = latencyMap.get(time);
                double sum = MathTools.sumDouble(latencyList);
                double mean = MathTools.meanDouble(latencyList);
                double std = MathTools.standardDeviationDouble(latencyList);
                double var = MathTools.varianceDouble(latencyList);
                sb.append(time);
                sb.append(',');
                sb.append(sum);
                sb.append(',');
                sb.append(mean);
                sb.append(',');
                sb.append(std);
                sb.append(',');
                sb.append(var);
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sampleData(double time){
        List<Double> latencyList = subTaskResultList.stream().map(SubTaskResult::getDelay).collect(Collectors.toList());
        List<Long> transferredDataList = subTaskResultList.stream().map(SubTaskResult::getTransferData).collect(Collectors.toList());
        List<Long> mustBeTransferredDataList =subTaskResultList.stream().map(SubTaskResult::getMustBeTransferredData).collect(Collectors.toList());
        List<Double> latencyListCp = new ArrayList<>(latencyList);
        List<Long> transferredDataListCp = new ArrayList<>(transferredDataList);
        List<Long> mustBeTransferredDataListCp = new ArrayList<>(mustBeTransferredDataList);
        latencyMap.put(time, latencyListCp);
        transferredDataMap.put(time, transferredDataListCp);
        mustBeTransferredDataMap.put(time, mustBeTransferredDataListCp);


        Map<String, Double> CEyMap = new HashMap<>();
        for (FogDevice fogDevice : Env.fogDevices) {
            CEyMap.put(fogDevice.getName(), fogDevice.getEnergyConsumption());
        }
        consumedEnergyMap.put(time, CEyMap);
    }
}