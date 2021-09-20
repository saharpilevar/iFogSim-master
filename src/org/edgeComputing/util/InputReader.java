package org.edgeComputing.util;

import org.edgeComputing.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class InputReader {

    public List<EdgeServer> readEdgeServersCSV(String filePath) {
        Path pathToFile = Paths.get(filePath);
        List<EdgeServer> edgeServerList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                EdgeServer edgeServer = new EdgeServer();
                edgeServer.setNodeName(attributes[0]);
                edgeServer.setBusyPower(Double.valueOf(attributes[1]));
                edgeServer.setDownBw(Long.valueOf(attributes[2]));
                edgeServer.setIdlePower(Double.valueOf(attributes[3]));
                edgeServer.setLevel(Integer.valueOf(attributes[4]));
                edgeServer.setMips(Long.valueOf(attributes[5]));
                edgeServer.setRam(Integer.valueOf(attributes[6]));
                edgeServer.setRatePerMips(Double.valueOf(attributes[7]));
                edgeServer.setUpBw(Long.valueOf(attributes[8]));
                edgeServer.setUpLinkLatency(Double.valueOf(attributes[9]));
                edgeServer.setAreaId(Integer.parseInt(attributes[10]));
                edgeServer.setJoinDelay(Integer.parseInt(attributes[11]));
                edgeServerList.add(edgeServer);
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return edgeServerList;
    }

    public List<Cloud> readCloudCSV(String filePath) {
        Path pathToFile = Paths.get(filePath);
        List<Cloud> cloudList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                Cloud cloud = new Cloud();
                cloud.setNodeName(attributes[0]);
                cloud.setBusyPower(Double.valueOf(attributes[1]));
                cloud.setDownBw(Long.valueOf(attributes[2]));
                cloud.setIdlePower(Double.valueOf(attributes[3]));
                cloud.setLevel(Integer.valueOf(attributes[4]));
                cloud.setMips(Long.valueOf(attributes[5]));
                cloud.setRam(Integer.valueOf(attributes[6]));
                cloud.setRatePerMips(Double.valueOf(attributes[7]));
                cloud.setUpBw(Long.valueOf(attributes[8]));
                cloud.setUpLinkLatency(Double.valueOf(attributes[9]));
                cloudList.add(cloud);
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return cloudList;
    }

    public List<ProxyServer> readProxyServerCSV(String filePath) {
        Path pathToFile = Paths.get(filePath);
        List<ProxyServer> proxyServerList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                ProxyServer proxyServer = new ProxyServer();
                proxyServer.setNodeName(attributes[0]);
                proxyServer.setBusyPower(Double.valueOf(attributes[1]));
                proxyServer.setDownBw(Long.valueOf(attributes[2]));
                proxyServer.setIdlePower(Double.valueOf(attributes[3]));
                proxyServer.setLevel(Integer.valueOf(attributes[4]));
                proxyServer.setMips(Long.valueOf(attributes[5]));
                proxyServer.setRam(Integer.valueOf(attributes[6]));
                proxyServer.setRatePerMips(Double.valueOf(attributes[7]));
                proxyServer.setUpBw(Long.valueOf(attributes[8]));
                proxyServer.setUpLinkLatency(Double.valueOf(attributes[9]));
                proxyServerList.add(proxyServer);
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return proxyServerList;
    }

    public List<AuctioneerServer> readAuctioneerCSV(String filePath) {
        Path pathToFile = Paths.get(filePath);
        List<AuctioneerServer> auctioneerList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                AuctioneerServer auctioneerServer = new AuctioneerServer();
                auctioneerServer.setNodeName(attributes[0]);
                auctioneerServer.setBusyPower(Double.valueOf(attributes[1]));
                auctioneerServer.setDownBw(Long.valueOf(attributes[2]));
                auctioneerServer.setIdlePower(Double.valueOf(attributes[3]));
                auctioneerServer.setLevel(Integer.valueOf(attributes[4]));
                auctioneerServer.setMips(Long.valueOf(attributes[5]));
                auctioneerServer.setRam(Integer.valueOf(attributes[6]));
                auctioneerServer.setRatePerMips(Double.valueOf(attributes[7]));
                auctioneerServer.setUpBw(Long.valueOf(attributes[8]));
                auctioneerServer.setUpLinkLatency(Double.valueOf(attributes[9]));
                auctioneerList.add(auctioneerServer);
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return auctioneerList;
    }
    public Map<Integer, List<Task>> readTasksCSV(String tasksFilePath, String subTasksFilePath
//                                                 ,String subTasksGraphsFilePath
                                             ) {
        Path pathToFile = Paths.get(tasksFilePath);
        Map<Integer, List<Task>> taskListMap = new HashMap<>();
        Map<Integer, List<Task>> subTaskList = this.readSubTasksCSV(subTasksFilePath);
//        Map<Integer, Graph> taskGraphsMap = this.readGraphsCSV(subTasksGraphsFilePath, subTaskList);
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                Task task = new Task(attributes[0],
                        Long.valueOf(attributes[1]), Long.valueOf(attributes[2]),
                        Integer.valueOf(attributes[3]), Long.valueOf(attributes[4]));
                List<Task> subTasks= new ArrayList<>();
                for(int i= Integer.valueOf(attributes[6]); i<= Integer.valueOf(attributes[7]);i++ )
               {
                   String i1= String.valueOf(i);
                   Task SubTask = subTaskList.get(Integer.valueOf(attributes[0])).stream().filter(item -> item.getId()
                           .equals(attributes[0] + '-' + i1)).collect(Collectors.toList()).get(0);
                   subTasks.add(SubTask);
                   task.setSubTasks(subTasks);
               }
//                Task startSubTask = subTaskList.get(Integer.valueOf(attributes[0])).stream().filter(item -> item.getId()
//                        .equals(attributes[0] + '-' + attributes[6])).collect(Collectors.toList()).get(0);
//                Task endSubTask = subTaskList.get(Integer.valueOf(attributes[0])).stream().filter(item -> item.getId()
//                        .equals(attributes[0] + '-' + attributes[7])).collect(Collectors.toList()).get(0);
//                Graph graph = taskGraphsMap.get(Integer.valueOf(attributes[0]));
//                graph.setStartTask(startSubTask);
//                graph.setEndTask(endSubTask);
//                task.setSubTasks(graph);
                if (taskListMap.containsKey(Integer.valueOf(attributes[5]))) {
                    taskListMap.get(Integer.valueOf(attributes[5])).add(task);
                } else {
                    taskListMap.put(Integer.valueOf(attributes[5]), new ArrayList<>(Arrays.asList(task)));
                }
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return taskListMap;
    }

//    private Map<Integer, Graph> readGraphsCSV(String graphFilePath, Map<Integer, List<Task>> subTasks) {
//        Path pathToFile = Paths.get(graphFilePath);
//
//        Map<Integer, Graph> taskGraphMap = new HashMap<>();
//        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
//            br.readLine();
//            String line = br.readLine();
//            while (line != null) {
//                String[] attributes = line.split(",");
//                List<Task> taskSubTasksList = subTasks.get(Integer.valueOf(attributes[3]));
//                Task src = taskSubTasksList.stream().filter(
//                        item -> item.getId().equals(attributes[3] + "-" + attributes[0])).collect(
//                        Collectors.toList()).get(0);
//                List<Task> rights = new ArrayList<>();
//                List<Task> lefts = new ArrayList<>();
//                for (String nodeId : attributes[1].split("\\|")) {
//                    if (nodeId != null && !nodeId.equals("N")) {
//                        Task right = taskSubTasksList.stream().filter(
//                                item -> item.getId().equals(attributes[3] + "-" + nodeId)).collect(
//                                Collectors.toList()).get(0);
//                        rights.add(right);
//                    }
//                }
//                for (String nodeId : attributes[2].split("\\|")) {
//                    if (nodeId != null && !nodeId.equals("N")) {
//                        Task left = taskSubTasksList.stream().filter(
//                                item -> item.getId().equals(attributes[3] + "-" + nodeId)).collect(
//                                Collectors.toList()).get(0);
//                        lefts.add(left);
//                    }
//                }
//
//                if (!taskGraphMap.containsKey(Integer.valueOf(attributes[3]))) {
//                    taskGraphMap.put(Integer.valueOf(attributes[3]), new Graph());
//                }
//                Graph graph = taskGraphMap.get(Integer.valueOf(attributes[3]));
//                graph.addNode(src, lefts, rights);
//                line = br.readLine();
//            }
//
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        return taskGraphMap;
//    }

    private Map<Integer, List<Task>> readSubTasksCSV(String filePath) {
        Path pathToFile = Paths.get(filePath);
        Map<Integer, List<Task>> subTasksListMap = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                Task task = new Task(attributes[5] + '-' + attributes[0],
                        Long.valueOf(attributes[1]), Long.valueOf(attributes[2]),
                        Integer.valueOf(attributes[3]), Long.valueOf(attributes[4]));
                if (subTasksListMap.containsKey(Integer.valueOf(attributes[5]))) {
                    subTasksListMap.get(Integer.valueOf(attributes[5])).add(task);
                } else {
                    subTasksListMap.put(Integer.valueOf(attributes[5]), new ArrayList<>(Arrays.asList(task)));
                }
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return subTasksListMap;

    }

    public List<UE> readUEsCSV(String filePath) {
        Path pathToFile = Paths.get(filePath);
        List<UE> ueList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                UE ue = new UE();
                ue.setNodeName(attributes[0]);
                ue.setBusyPower(Double.valueOf(attributes[1]));
                ue.setDownBw(Long.valueOf(attributes[2]));
                ue.setIdlePower(Double.valueOf(attributes[3]));
                ue.setLevel(Integer.valueOf(attributes[4]));
                ue.setMips(Long.valueOf(attributes[5]));
                ue.setRam(Integer.valueOf(attributes[6]));
                ue.setRatePerMips(Double.valueOf(attributes[7]));
                ue.setUpBw(Long.valueOf(attributes[8]));
                ue.setUpLinkLatency(Double.valueOf(attributes[9]));
                ue.setAreaId(Integer.parseInt(attributes[10]));
                ue.setTransmissionPower(Double.valueOf(attributes[11]));
                ueList.add(ue);
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return ueList;
    }
}
