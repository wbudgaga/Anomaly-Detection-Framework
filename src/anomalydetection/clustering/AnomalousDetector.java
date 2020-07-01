package anomalydetection.clustering;

import java.util.logging.Logger;

import anomalydetection.clustering.ClusteringTask;
import anomalydetection.threadpool.Task;
import anomalydetection.threadpool.ThreadPoolManager;

public abstract class AnomalousDetector {
	public static final int ONLINE  = 0;
	public static final int OFFLINE = 1;
	
	protected ThreadPoolManager threadPool; 
	private   	int 		detectingMode		= ONLINE;
	private		String 		processID; 	
	private 	volatile long 	receivedSamples		= 0;
	private  	volatile long 	processedSamples	= 0;
	protected 	Object 		lock 			= new Object();
	protected 	DetectorMaster 	master;
	protected  	Process		process;

	public AnomalousDetector(DetectorMaster master, String pID){
		setProcessID(pID);
		this.master = master;
	}
	
	protected void addReceivedSample(){
		++receivedSamples;
	}
	public long getReceivedSample(){
		return receivedSamples;
	}

	public void addProcessedSample(){
		++processedSamples;
	}
	public void resestProcessedSample(){
		processedSamples=0;
		receivedSamples = 0;
	}

	
	public long getProcessedSample(){
		return processedSamples;
	}

	public void setDetectingMode(int mode){
		detectingMode = mode;
	}
	
	public int getDetectingMode(){
		return detectingMode;
	}
	
	public void anomaliesAlarm(int Notifier, Instance instance){
		master.anomaliesAlarm(Notifier, instance);
	}
	
	public void anomaliesSuspicion(int Notifier, Instance instance){
		master.anomaliesSuspicion(Notifier, instance);
	}
	
	public void addTaskToThreadPool(Task task){
		master.addTaskToThreadPool(task);
	}
	
	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}
	
	public void process(Instance instance) throws Exception{
		this.process.process(instance);		
	}
	
	public long enforceTrain(int s) throws Exception{
		this.process.train();
		return getReceivedSample();
	}
		
	public void init(int trainingSetSize, int numOfAttributes) throws Exception {
		synchronized(lock){
			this.process = new InitialProcess(this,numOfAttributes,trainingSetSize);
		}
	}
	
	public void createClusteringTask(ClusteringTask clusteringTask) throws Exception{
		createDetectorProcess(clusteringTask);
		addTaskToThreadPool(clusteringTask);
	}
	
	protected abstract String getBuiltMSG();
	public abstract void createDetectorProcess(ClusteringTask clusteringTask) throws Exception;
	public abstract void falseAlarmResponse(Instance instance);
	public abstract void train() throws Exception;
	public abstract void loadModel(String fileName) throws Exception;
}
