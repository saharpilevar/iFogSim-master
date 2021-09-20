package org.edgeComputing.outputWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Output {
    public static List<Result> resultList = new ArrayList<>();
    public static boolean isWritten = false;

    public static void writeResult(){
        try (PrintWriter writer = new PrintWriter(new File("E:\\Eclipse_workSpace\\iFogSim-master\\results\\rs.csv"))) {

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
            sb.append('\n');

            for(Result result : resultList){
                sb.append(result.getTaskId());
                sb.append(',');
                sb.append(result.getTupleId());
                sb.append(',');
                sb.append(result.getExecTime());
                sb.append(',');
                sb.append(result.getDelay());
                sb.append(',');
                sb.append(result.getNetworkDelay());
                sb.append(',');
                sb.append(result.getExecCost());
                sb.append(',');
                sb.append(result.getTransferData());
                sb.append(',');
                sb.append(result.getExecutor());
                sb.append(',');
                sb.append(result.getExecutorId());
                sb.append(',');
                sb.append(result.getOwner());
                sb.append(',');
                sb.append(result.getOwnerId());
                sb.append('\n');
            }

            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

}