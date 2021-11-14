package org.edgeComputing;

import org.edgeComputing.core.Edge;
import org.edgeComputing.model.*;
import org.edgeComputing.util.InputReader;

import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Runner {


    //private final static String SUB_TASKS_GRAPH_CSV_PATH = "E:\\Eclipse_workSpace\\OffloadingEdgeComputing-initialize\\OffloadingEdgeComputing-initialize\\TestData\\SubTasksGraphs.csv";
    public static void main(String[] args){
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        String CLOUD_CSV_PATH = Paths.get(currentPath.toString(), "TestData", "Cloud.csv").toString();
        String PROXY_CSV_PATH = Paths.get(currentPath.toString(), "TestData", "ProxyServer.csv").toString();
        String EDGE_SERVER_CSV_PATH = Paths.get(currentPath.toString(), "TestData", "EdgeServers.csv").toString();
        String TASKS_CSV_PATH = Paths.get(currentPath.toString(), "TestData", "Tasks.csv").toString();
        String UES_CSV_PATH = Paths.get(currentPath.toString(), "TestData", "UEs.csv").toString();
        String SUB_TASKS_CSV_PATH = Paths.get(currentPath.toString(), "TestData", "SubTasks.csv").toString();
        String AUCTIONEER_CSV_PATH = Paths.get(currentPath.toString(), "TestData", "Auctioneer.csv").toString();
        
        InputReader inputReader = new InputReader();

        EdgeConfig edgeConfig = new EdgeConfig(1, "test");
        Edge edge = new Edge(edgeConfig);


        //Create edgeServers
        List<EdgeServer> edgeServerList = inputReader.readEdgeServersCSV(EDGE_SERVER_CSV_PATH);


        List<UE> ues = inputReader.readUEsCSV(UES_CSV_PATH);
        Map<Integer, List<Task>> tasksMap = inputReader.readTasksCSV(TASKS_CSV_PATH, SUB_TASKS_CSV_PATH
                //, SUB_TASKS_GRAPH_CSV_PATH
                );

        for(int i=0; i<ues.size(); i++){
            ues.get(i).setTasks(tasksMap.get(i));
        }

        //Create Proxy
        List<ProxyServer> proxyServerList = inputReader.readProxyServerCSV(PROXY_CSV_PATH);
        ProxyServer proxyServer = proxyServerList.get(0);

        //Create Cloud
        List<Cloud> cloudList = inputReader.readCloudCSV(CLOUD_CSV_PATH);
        Cloud cloud = cloudList.get(0);
        cloud.setProxyServer(proxyServer);

        //Create auctioneerserver
        List<AuctioneerServer> auctioneerServerList = inputReader.readAuctioneerCSV(AUCTIONEER_CSV_PATH);
        AuctioneerServer auctioneerServer = auctioneerServerList.get(0);

        edge.initialEnv(edgeServerList, ues, cloud, auctioneerServer);

    }
}
