package org.fog.entities;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

public class Tuple extends Cloudlet{

	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int ACTUATOR = 3;
	
	private String appId;
	
	private String tupleType;
	private String destModuleName;
	private String srcModuleName;
	private int actualTupleId;
	private int direction;
	private int actuatorId;
	private int sourceDeviceId;
	private int sourceModuleId;

	//My Custom Fields
	private int destinationId = -1;
	private int actualSourceId = -1;
	private Tuple actualRequestedTuple;
	private String executorName = "";
	private int executorId = -1;
	private double creationTime = -1.0;
	private double doneTime = -1.0;
	private double networkDelay = 0.0;
	private double runningTime = -1.0;
	private String taskId = "";

	//Fields of source mobile device
	private long mipsOfSourceDevice;
	private double idlePowerOfSourceDevice;
	private double busyPowerOfSourceDevice;
	private double transmissionPowerOfSourceDevice;
	/**
	 * Map to keep track of which module instances has a tuple traversed.
	 * 
	 * Map from moduleName to vmId of a module instance
	 */
	private Map<String, Integer> moduleCopyMap;
	
	public Tuple(String appId, int cloudletId, int direction, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		setAppId(appId);
		setDirection(direction);
		setSourceDeviceId(-1);
		setModuleCopyMap(new HashMap<String, Integer>());
	}

	public int getActualTupleId() {
		return actualTupleId;
	}

	public void setActualTupleId(int actualTupleId) {
		this.actualTupleId = actualTupleId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getTupleType() {
		return tupleType;
	}

	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public String getDestModuleName() {
		return destModuleName;
	}

	public void setDestModuleName(String destModuleName) {
		this.destModuleName = destModuleName;
	}

	public String getSrcModuleName() {
		return srcModuleName;
	}

	public void setSrcModuleName(String srcModuleName) {
		this.srcModuleName = srcModuleName;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getActuatorId() {
		return actuatorId;
	}

	public void setActuatorId(int actuatorId) {
		this.actuatorId = actuatorId;
	}

	public int getSourceDeviceId() {
		return sourceDeviceId;
	}

	public void setSourceDeviceId(int sourceDeviceId) {
		this.sourceDeviceId = sourceDeviceId;
	}

	public Map<String, Integer> getModuleCopyMap() {
		return moduleCopyMap;
	}

	public void setModuleCopyMap(Map<String, Integer> moduleCopyMap) {
		this.moduleCopyMap = moduleCopyMap;
	}

	public int getSourceModuleId() {
		return sourceModuleId;
	}

	public void setSourceModuleId(int sourceModuleId) {
		this.sourceModuleId = sourceModuleId;
	}

	//////////////////////////////////////////

	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public double getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(double creationTime) {
		this.creationTime = creationTime;
	}

	public double getDoneTime() {
		return doneTime;
	}

	public void setDoneTime(double doneTime) {
		this.doneTime = doneTime;
	}

	public double getNetworkDelay() {
		return networkDelay;
	}

	public void setNetworkDelay(double networkDelay) {
		this.networkDelay = networkDelay;
	}

	public int getExecutorId() {
		return executorId;
	}

	public void setExecutorId(int executorId) {
		this.executorId = executorId;
	}

	public String getExecutorName() {
		return executorName;
	}

	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}

	public double getDelay(){
		double delay = 0;
		if(this.getActualRequestedTuple() != null){
			delay = this.doneTime - this.getActualRequestedTuple().getCreationTime();
		}
		else {
			delay = this.doneTime - this.creationTime;
		}
		return delay;
	}

	public double getExecutionTime(){
		double executionTime = this.getActualRequestedTuple().getFinishTime()-this.getActualRequestedTuple().getExecStartTime();
		return executionTime;
	}

	public Tuple getActualRequestedTuple() {
		return actualRequestedTuple;
	}

	public void setActualRequestedTuple(Tuple actualRequestedTuple) {
		this.actualRequestedTuple = actualRequestedTuple;
	}

	public long getMipsOfSourceDevice() {
		return mipsOfSourceDevice;
	}

	public void setMipsOfSourceDevice(long mipsOfSourceDevice) {
		this.mipsOfSourceDevice = mipsOfSourceDevice;
	}

	public double getIdlePowerOfSourceDevice() {
		return idlePowerOfSourceDevice;
	}

	public void setIdlePowerOfSourceDevice(double idlePowerOfSourceDevice) {
		this.idlePowerOfSourceDevice = idlePowerOfSourceDevice;
	}

	public double getBusyPowerOfSourceDevice() {
		return busyPowerOfSourceDevice;
	}

	public void setBusyPowerOfSourceDevice(double busyPowerOfSourceDevice) {
		this.busyPowerOfSourceDevice = busyPowerOfSourceDevice;
	}

	public double getTransmissionPowerOfSourceDevice() {
		return transmissionPowerOfSourceDevice;
	}

	public void setTransmissionPowerOfSourceDevice(double transmissionPowerOfSourceDevice) {
		this.transmissionPowerOfSourceDevice = transmissionPowerOfSourceDevice;
	}


}