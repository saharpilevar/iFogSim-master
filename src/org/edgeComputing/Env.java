package org.edgeComputing;

import org.edgeComputing.model.DeviceInfo;
import org.fog.entities.FogDevice;

import java.util.ArrayList;
import java.util.List;

public class Env {
    public final static String DEVICE_EDGE_SERVER = "EDGE_SERVER";
    public final static String DEVICE_USER_EQUIPMENT = "USER_EQUIPMENT";
    public final static String DEVICE_AUCTIONEER = "AUCTIONEER";
    public final static String DEVICE_TASK_GENERATOR = "TASK_GENERATOR";
    public final static String DEVICE_RESPONSE_DISPLAY = "RESPONSE_DISPLAY";
    public final static String DEVICE_ROUTER = "ROUTER";
    public final static String DEVICE_CLOUD = "cloud";
    public final static String DEVICE_PROXY = "PROXY";

    public final static String TUPLE_TYPE_TASK = "TASK";
    public final static String TUPLE_TYPE_RESPONSE = "RESPONSE";
    public final static String TUPLE_TYPE_TASK_INFO = "TASK_INFO";
    public final static String TUPLE_TYPE_EDGE_SERVER_INFO = "EDGE_SERVER_INFO";
    public final static String TUPLE_TYPE_MATCH_RESPONSE_TO_MOBILE = "MATCH_RESPONSE_TO_MOBILE";

    public final static String BROKER_NAME = "BROKER";

    public final static String MASTER_CONTROLLER_NAME = "MASTER_CONTROLLER";

    public static List<DeviceInfo> deviceInfoList = new ArrayList<>();
    public static int numberOfEdgeServers = 0;

    public static List<FogDevice> fogDevices = new ArrayList<>();
}
