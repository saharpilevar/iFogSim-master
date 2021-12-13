package org.fog.utils;

public class Config {

	public static final double RESOURCE_MGMT_INTERVAL = 100;
	public static int MAX_SIMULATION_TIME = 10000;
	public static int RESOURCE_MANAGE_INTERVAL = 100;
	public static String FOG_DEVICE_ARCH = "x86";
	public static String FOG_DEVICE_OS = "Linux";
	public static String FOG_DEVICE_VMM = "Xen";
	public static double FOG_DEVICE_TIMEZONE = 10.0;
	public static double FOG_DEVICE_COST = 3.0;
	public static double FOG_DEVICE_COST_PER_MEMORY = 0.05;
	public static double FOG_DEVICE_COST_PER_STORAGE = 0.001;
	public static double FOG_DEVICE_COST_PER_BW = 0.0;


	public final static String PROJECT_PATH = "E:\\Eclipse_workSpace\\iFogSim-master";
	public final static String OS_SEPARATOR = "\\";
	public final static boolean MULTI_EPISODE = false;
	public final static double CONTROLLER_COLLECT_DATA_INTERVAL = 10.0;



	public static String getOutputPath(){
		String path = PROJECT_PATH + OS_SEPARATOR + "output" + OS_SEPARATOR + "result";
		return path;
	}
}
