package org.edgeComputing.core;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.edgeComputing.Env;
import org.edgeComputing.model.*;
import org.edgeComputing.core.model.*;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Edge {

    //Project Models
    private List<EdgeServer> edgeServers;
    private List<UE> ues;
    private Cloud cloud;
    private AuctioneerServer auctioneerServer;

    //IFogSim Models
    private List<FogDevice> ueDevices;
    private List<FogDevice> edgeServerDevices;
    private FogDevice cloudDevice;
    private FogDevice auctioneerDevice;
    private List<FogDevice> proxies;
    private List<FogDevice> routers;
    private List<Sensor> sensors;
    private List<Actuator> actuators;
    private EdgeConfig edgeConfig;
    private double EEG_TRANSMISSION_TIME = 5.1;
    public Edge(EdgeConfig edgeConfig) {
        Log.printLine("Starting Edge...");
        try {
            Log.disable();
            int num_user = edgeConfig.getNumberOfUsers(); // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events
            this.edgeConfig = edgeConfig;

            CloudSim.init(num_user, calendar, trace_flag);
            Log.printLine("Edge Started...");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }
    public void initialEnv(List<EdgeServer> edgeServers, List<UE> ues, Cloud cloud, AuctioneerServer auctioneerServer) {
        Log.printLine("Initialize Edge Environment...");

        this.edgeServers = edgeServers;
        this.ues = ues;
        this.cloud = cloud;
        this.auctioneerServer = auctioneerServer;
        this.proxies = new ArrayList<>();
        this.routers = new ArrayList<>();
        this.ueDevices = new ArrayList<>();
        this.edgeServerDevices = new ArrayList<>();
        this.sensors = new ArrayList<>();
        this.actuators = new ArrayList<>();

        try {
            FogBroker broker = new FogBroker(Env.BROKER_NAME);

            Application application = createApplication(broker.getId());
            application.setUserId(broker.getId());

            this.createDevices(broker.getId());

            Controller controller = null;

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

//            moduleMapping.addModuleToDevice(Env.DEVICE_CLOUD, Env.DEVICE_CLOUD);

            for(FogDevice device : edgeServerDevices){
                moduleMapping.addModuleToDevice(device.getName(), device.getName());
            }

            for(FogDevice device : ueDevices){
                moduleMapping.addModuleToDevice(device.getName(), device.getName());
            }
            for (FogDevice device : routers) {
                moduleMapping.addModuleToDevice(Env.DEVICE_ROUTER, device.getName());
            }
            moduleMapping.addModuleToDevice(Env.DEVICE_AUCTIONEER, auctioneerDevice.getName());

            List<FogDevice> fogDevices = new ArrayList<>();
            fogDevices.addAll(edgeServerDevices);
            fogDevices.addAll(ueDevices);
//            fogDevices.add(cloudDevice);
//            fogDevices.addAll(proxies);
            fogDevices.addAll(routers);
            fogDevices.add(auctioneerDevice);
            addDevicesToCustomEnv();
            Env.fogDevices = fogDevices;


            controller = new Controller(Env.MASTER_CONTROLLER_NAME, fogDevices, sensors,
                    actuators);

            controller.submitApplication(application,
                    new ModulePlacementMapping(fogDevices, application, moduleMapping));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();
            Log.printLine("Edge Computing finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }

    }
    @SuppressWarnings({"serial"})
    private Application createApplication(int userId) {

        Application application = Application.createApplication(this.edgeConfig.getAppId(), userId);
        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        application.addAppModule(Env.DEVICE_CLOUD,cloud.getMips(),cloud.getRam(),1000,cloud.getUpBw());

//        application.addAppModule(Env.DEVICE_AUCTIONEER,auctioneerServer.getMips(),auctioneerServer.getRam(),
//                1000,auctioneerServer.getUpBw());
        application.addAppModule(Env.DEVICE_AUCTIONEER, 30);
        application.addAppModule(Env.DEVICE_ROUTER, 30);

        List<EdgeServer> allEdgeServers = edgeServers.stream().collect(Collectors.toList());
        List<UE> allUEs = ues.stream().collect(Collectors.toList());

        for (int i = 0; i <allEdgeServers.size() ; i++) {
            application.addAppModule(allEdgeServers.get(i).getNodeName(), allEdgeServers.get(i).getMips(), allEdgeServers.get(i).getRam(),
                    1000, allEdgeServers.get(i).getUpBw());
        }
        for (int j = 0; j < allUEs.size() ; j++) {
            application.addAppModule(allUEs.get(j).getNodeName() ,allUEs.get(j).getMips(), allUEs.get(j).getRam(),
                    1000, allUEs.get(j).getUpBw());
        }
        ///////////////////////////////////////////////////////////////////
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
        }};
//
//        List<String> loop2 = new ArrayList<String>() {{
//            add(Env.DEVICE_ROUTER);
//        }};



        for (int j = 0; j < allUEs.size() ; j++) {
            application.addAppEdge(Env.TUPLE_TYPE_TASK, allUEs.get(j).getNodeName(), 1000, 2000, Env.TUPLE_TYPE_TASK, Tuple.UP, AppEdge.SENSOR);
            application.addAppEdge(allUEs.get(j).getNodeName(), Env.DEVICE_ROUTER, 1000, 2000, Env.TUPLE_TYPE_TASK_INFO, Tuple.UP, AppEdge.MODULE);
            application.addAppEdge(Env.DEVICE_ROUTER, allUEs.get(j).getNodeName(), 100, 50, Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE, Tuple.DOWN, AppEdge.MODULE);
            application.addAppEdge(allUEs.get(j).getNodeName(), Env.DEVICE_ROUTER, 1000, 2000, Env.TUPLE_TYPE_TASK, Tuple.UP, AppEdge.MODULE);
            application.addAppEdge(Env.DEVICE_ROUTER, allUEs.get(j).getNodeName(), 100, 50, Env.TUPLE_TYPE_RESPONSE, Tuple.DOWN, AppEdge.MODULE);
            application.addAppEdge(allUEs.get(j).getNodeName(), allUEs.get(j).getNodeName(), 1000, 2000, Env.TUPLE_TYPE_TASK, Tuple.UP, AppEdge.MODULE);
            application.addAppEdge(allUEs.get(j).getNodeName(), allUEs.get(j).getNodeName(), 1000, 2000, Env.TUPLE_TYPE_RESPONSE, Tuple.DOWN, AppEdge.MODULE);
            //////////////////////////////////////////////////////////////////////////////////////
            application.addTupleMapping(allUEs.get(j).getNodeName(), Env.TUPLE_TYPE_TASK, Env.TUPLE_TYPE_TASK_INFO, new FractionalSelectivity(1.0));
            application.addTupleMapping(allUEs.get(j).getNodeName(), Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE, Env.TUPLE_TYPE_TASK, new FractionalSelectivity(1.0));
            application.addTupleMapping(allUEs.get(j).getNodeName(), Env.TUPLE_TYPE_TASK, Env.TUPLE_TYPE_RESPONSE, new FractionalSelectivity(1.0));
            /////////////////////////////////////////////////////////////////////////////////////
            int j_=j;
            List<String> loopTUA = new ArrayList<String>() {{
                add(Env.TUPLE_TYPE_TASK);
                add(allUEs.get(j_).getNodeName());
                add(Env.DEVICE_AUCTIONEER);

            }};
            final AppLoop appLoopTUA = new AppLoop(loopTUA);
            List<String> loopUU = new ArrayList<String>() {{
                add(allUEs.get(j_).getNodeName());
                add(allUEs.get(j_).getNodeName());

            }};
            final AppLoop appLoopUU = new AppLoop(loopUU);

            loops.add(appLoopTUA);
            loops.add(appLoopUU);

                    }
        for (int i = 0; i <allEdgeServers.size() ; i++) {
            application.addAppEdge(allEdgeServers.get(i).getNodeName(), Env.DEVICE_ROUTER, 1000, 2000, Env.TUPLE_TYPE_EDGE_SERVER_INFO, Tuple.UP, AppEdge.MODULE);
            application.addAppEdge(Env.DEVICE_ROUTER, allEdgeServers.get(i).getNodeName(), 1000, 2000, Env.TUPLE_TYPE_TASK, Tuple.UP, AppEdge.MODULE);
            application.addAppEdge(allEdgeServers.get(i).getNodeName(), Env.DEVICE_ROUTER, 1000, 2000, Env.TUPLE_TYPE_RESPONSE, Tuple.DOWN, AppEdge.MODULE);
            //////////////////////////////////////////////////////////////////////////
            application.addTupleMapping(allEdgeServers.get(i).getNodeName(), Env.TUPLE_TYPE_TASK, Env.TUPLE_TYPE_RESPONSE, new FractionalSelectivity(1.0));
            ///////////////////////////////////////////////////////////////////////////////

        }
        application.addAppEdge(Env.DEVICE_ROUTER, Env.DEVICE_AUCTIONEER, 100, 50, Env.TUPLE_TYPE_TASK_INFO, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(Env.DEVICE_ROUTER, Env.DEVICE_AUCTIONEER, 100, 50, Env.TUPLE_TYPE_EDGE_SERVER_INFO, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(Env.DEVICE_AUCTIONEER, Env.DEVICE_ROUTER, 100, 50, Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE, Tuple.DOWN, AppEdge.MODULE);

        application.addTupleMapping(Env.DEVICE_ROUTER, Env.TUPLE_TYPE_TASK_INFO, Env.TUPLE_TYPE_TASK_INFO, new FractionalSelectivity(1.0));
        application.addTupleMapping(Env.DEVICE_ROUTER, Env.TUPLE_TYPE_EDGE_SERVER_INFO, Env.TUPLE_TYPE_EDGE_SERVER_INFO, new FractionalSelectivity(1.0));
        application.addTupleMapping(Env.DEVICE_ROUTER, Env.TUPLE_TYPE_TASK, Env.TUPLE_TYPE_TASK, new FractionalSelectivity(1.0));
        application.addTupleMapping(Env.DEVICE_ROUTER, Env.TUPLE_TYPE_RESPONSE, Env.TUPLE_TYPE_RESPONSE, new FractionalSelectivity(1.0));
        application.addTupleMapping(Env.DEVICE_ROUTER, Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE, Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE, new FractionalSelectivity(1.0));
        application.addTupleMapping(Env.DEVICE_AUCTIONEER, Env.TUPLE_TYPE_TASK_INFO, Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE, new FractionalSelectivity(1.0));
        application.addTupleMapping(Env.DEVICE_AUCTIONEER, Env.TUPLE_TYPE_EDGE_SERVER_INFO, Env.TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE, new FractionalSelectivity(1.0));

////////////////////////////////////////////////////////////////////////////////////
        for (int i = 0; i <allEdgeServers.size() ; i++) {
            for (int j = 0; j <allUEs.size() ; j++) {
                int i_=i;
                int j_=j;
            List<String> loop4 = new ArrayList<String>() {{
                add(allUEs.get(j_).getNodeName());
                add(allEdgeServers.get(i_).getNodeName());

            }};
            final AppLoop appLoop4 = new AppLoop(loop4);
            loops.add(appLoop4);
            }
        }

        application.setLoops(loops);
        return application;
    }


    private void createDevices(int userId) {
        FogDevice cloud = createEdgeDevice(this.cloud);
        cloud.setParentId(-1);
        this.cloudDevice = cloud;
        FogDevice proxy = createEdgeDevice(this.cloud.getProxyServer());
        proxy.setParentId(cloud.getId());
        proxy.setUplinkLatency(this.cloud.getProxyServer().getUpLinkLatency()); // latency of connection between proxy server and cloud is 100 ms
        proxies.add(proxy);
        AuctioneerDevice auctioneer= createAuctioneerDevice (this.auctioneerServer);
        auctioneer.setParentId(proxy.getId());
        this.auctioneerDevice = auctioneer;
        for (int areaId = 0; areaId < this.edgeConfig.getNumberOfEdgeArea(); areaId++) {
            addEdgeArea(areaId, auctioneer.getId(),userId);
//            addEdgeArea(areaId, proxy.getId(), userId);
        }
    }

    private FogDevice addEdgeArea(int areaId, int parentId, int userId) {
        Router routerModel = new Router(Env.DEVICE_ROUTER + "-" + areaId);
        RouterDevice router = createRouterDevice(routerModel);
        routers.add(router);
        router.setUplinkLatency(2);
        router.setParentId(parentId);
        List<EdgeServer> areaServers = edgeServers.stream().filter(item->item.getAreaId()==areaId).collect(Collectors.toList());
        for (EdgeServer server : areaServers) {
            EdgeServerDevice edgeServerDevice = addEdgeServer(server, router.getId());
            edgeServerDevice.setUplinkLatency(server.getUpLinkLatency());
            edgeServerDevices.add(edgeServerDevice);
            router.addEdgeServer(edgeServerDevice.getId());
        }
        List<UE> areaUE = ues.stream().filter(item->item.getAreaId()==areaId).collect(Collectors.toList());
        for (UE ue : areaUE) {
            MobileDevice ueDevice = addUE(ue, userId, this.edgeConfig.getAppId(), router.getId());
//            ueDevice.setUplinkLatency(ueDevice.getUplinkBandwidth());
            ueDevices.add(ueDevice);
            router.addMobileDevice(ueDevice.getId());
        }
        return router;
    }
    private MobileDevice addUE(UE ue, int userId, String appId, int parentId) {
        MobileDevice mobile = createMobileDevice(ue);
        mobile.setParentId(parentId);
        TaskGeneratorDevice taskGeneratorDevice = new TaskGeneratorDevice(Env.DEVICE_TASK_GENERATOR + "-" + ue.getNodeName(),
                Env.TUPLE_TYPE_TASK, userId, appId, new DeterministicDistribution(EEG_TRANSMISSION_TIME), ue.getTasks()); // inter-transmission time of EEG sensor follows a deterministic distribution
        taskGeneratorDevice.setParentName(mobile.getName());
        taskGeneratorDevice.setParentId(mobile.getId());
        sensors.add(taskGeneratorDevice);
//        Actuator display = new Actuator(Env.DEVICE_RESPONSE_DISPLAY + "-" + ue.getNodeName(), userId, appId,
//                Env.DEVICE_RESPONSE_DISPLAY);
//        actuators.add(display);
        taskGeneratorDevice.setGatewayDeviceId(mobile.getId());
        taskGeneratorDevice.setLatency(0.0);  // latency of connection between EEG sensors and the parent Smartphone is 6 ms
//        mobile.setxCoordinate(getValue(5));
//        mobile.setyCoordinate(getValue(15));
        mobile.setxCoordinate(ue.getXCoordinate());
        mobile.setyCoordinate(ue.getYCoordinate());
        mobile.setTaskGeneratorDevice(taskGeneratorDevice);

//        display.setGatewayDeviceId(mobile.getId());
//        display.setLatency(0.0);  // latency of connection between Display actuator and the parent Smartphone is 1 ms
        return mobile;
    }

    private EdgeServerDevice addEdgeServer(EdgeServer edgeServer, int parentId) {
        EdgeServerDevice edgeServerDevice = createEdgeServerDevice(edgeServer);
        edgeServerDevice.setParentId(parentId);
        edgeServerDevice.setxCoordinate(edgeServer.getXCoordinate());
        edgeServerDevice.setyCoordinate(edgeServer.getYCoordinate());
//        edgeServerDevice.setxCoordinate(getValue(10.00));
//        edgeServerDevice.setyCoordinate(getValue(15));
        return edgeServerDevice;
    }
    private FogDevice createEdgeDevice(EdgeEntity edgeEntity) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(
                new Pe(0, new PeProvisionerOverbooking(edgeEntity.getMips()))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(edgeEntity.getRam()),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(edgeEntity.getBusyPower(), edgeEntity.getIdlePower())
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(edgeEntity.getNodeName(), characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, edgeEntity.getUpBw(),
                    edgeEntity.getDownBw(), 0, edgeEntity.getRatePerMips());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(edgeEntity.getLevel());
        return fogdevice;
    }

    private EdgeServerDevice createEdgeServerDevice(EdgeServer edgeServer) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(
                new Pe(0, new PeProvisionerOverbooking(edgeServer.getMips()))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(edgeServer.getRam()),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(edgeServer.getBusyPower(), edgeServer.getIdlePower())
        );


        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        EdgeServerDevice edgeServerDevice = null;
        try {
            edgeServerDevice = new EdgeServerDevice(edgeServer.getNodeName(), characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, edgeServer.getUpBw(),
                    edgeServer.getDownBw(), 0, edgeServer.getRatePerMips(), edgeServer.getJoinDelay(), edgeServer.getMips(), edgeServer.getRam(), edgeServer.getBidPrice());

//            edgeServerDevice = new EdgeServerDevice(edgeServer.getNodeName(),edgeServer.getMips(),edgeServer.getRam(),edgeServer.getUpBw(),edgeServer.getDownBw(),edgeServer.getRatePerMips(),new FogLinearPowerModel(edgeServer.getBusyPower(), edgeServer.getIdlePower()),characteristics, new AppModuleAllocationPolicy(hostList),edgeServer.getBidPrice());
        } catch (Exception e) {
            e.printStackTrace();
        }

        edgeServerDevice.setLevel(edgeServer.getLevel());
        return edgeServerDevice;
    }

    private RouterDevice createRouterDevice(Router router) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(
                new Pe(0, new PeProvisionerOverbooking(router.getMips()))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(router.getRam()),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(router.getBusyPower(), router.getIdlePower())
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        RouterDevice routerDevice = null;
        try {
            routerDevice = new RouterDevice(router.getNodeName(), characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, router.getUpBw(),
                    router.getDownBw(), 0, router.getRatePerMips());
        } catch (Exception e) {
            e.printStackTrace();
        }

        routerDevice.setLevel(router.getLevel());
        return routerDevice;
    }

    private AuctioneerDevice createAuctioneerDevice(AuctioneerServer auctioneerServer) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(
                new Pe(0, new PeProvisionerOverbooking(auctioneerServer.getMips()))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(auctioneerServer.getRam()),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(auctioneerServer.getBusyPower(), auctioneerServer.getIdlePower())
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        AuctioneerDevice auctioneerDevice = null;
        try {
            auctioneerDevice = new AuctioneerDevice(auctioneerServer.getNodeName(), characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, auctioneerServer.getUpBw(),
                    auctioneerServer.getDownBw(), 0, auctioneerServer.getRatePerMips());
        } catch (Exception e) {
            e.printStackTrace();
        }

        auctioneerDevice.setLevel(auctioneerServer.getLevel());
        return auctioneerDevice;
    }
    private MobileDevice createMobileDevice(UE ue){
        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(
                new Pe(0, new PeProvisionerOverbooking(ue.getMips()))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 10000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ue.getRam()),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(ue.getBusyPower(), ue.getIdlePower())
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        MobileDevice mobileDevice = null;
        try {
//            mobileDevice = new MobileDevice(ue.getNodeName(),ue.getMips(),ue.getRam(),ue.getUpBw(),ue.getDownBw(),ue.getRatePerMips(),new FogLinearPowerModel(ue.getBusyPower(), ue.getIdlePower()),ue.getIdlePower(),ue.getBusyPower(),ue.getTransmissionPower(),characteristics, new AppModuleAllocationPolicy(hostList));
            mobileDevice = new MobileDevice(ue.getNodeName(), characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, ue.getUpBw(),
                    ue.getDownBw(), 0, ue.getRatePerMips(), ue.getMips(), ue.getRam(), ue.getIdlePower(), ue.getBusyPower(),ue.getTransmissionPower());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mobileDevice.setLevel(ue.getLevel());
        return mobileDevice;
    }
    private static double getValue(double min) {
        Random rn = new Random();
        return rn.nextDouble()*10 + min;}

    private void addDevicesToCustomEnv() {

        for (FogDevice router : this.routers) {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setId(router.getId());
            deviceInfo.setName(router.getName());
            Env.deviceInfoList.add(deviceInfo);
        }
        for (FogDevice ue : this.ueDevices) {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setId(ue.getId());
            deviceInfo.setName(ue.getName());
            Env.deviceInfoList.add(deviceInfo);
        }
        for (FogDevice edgeServer : this.edgeServerDevices) {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setId(edgeServer.getId());
            deviceInfo.setName(edgeServer.getName());
            Env.deviceInfoList.add(deviceInfo);
        }
        DeviceInfo auctioneerDeviceInfo = new DeviceInfo();
        auctioneerDeviceInfo.setId(this.auctioneerDevice.getId());
        auctioneerDeviceInfo.setName(this.auctioneerDevice.getName());
        Env.deviceInfoList.add(auctioneerDeviceInfo);

        Env.numberOfEdgeServers = this.edgeServerDevices.size();
    }


}
