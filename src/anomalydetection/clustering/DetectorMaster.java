package anomalydetection.clustering;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import anomalydetection.experiments.throuhput.TimerThread;
import anomalydetection.threadpool.Task;
import anomalydetection.threadpool.ThreadPoolManager;
import anomalydetection.util.UtilClass;

public class DetectorMaster {
	private ConcurrentHashMap  <String,AnomalousDetector> 	detectorsMap = new  ConcurrentHashMap  <String,AnomalousDetector>();
    private ThreadPoolManager 		threadPool; 
	private ClustererFactory 		factory = ClustererFactory.getInstance();
	private String 					algorithmName;
	private int 					numOfAttributes;
	
	public DetectorMaster(int threadPoolSize) throws Exception{	
		threadPool 	= new ThreadPoolManager(threadPoolSize);
		threadPool.start();
		//createTimer();//for benchmark
	}
	
	public void start(String algorithmName, int numOfAttributes) throws Exception{
		this.algorithmName 		= algorithmName;
		this.numOfAttributes 	= numOfAttributes;
		if (Setting.LOADMODEL_AT.compareTo("none")!=0){
			String modelsDir = Setting.MODEL_DIR +Setting.LOADMODEL_AT;
			System.out.println("Loading models from "+modelsDir);
			if (!loadModels(modelsDir))
				System.out.println("models could not be loaded from"+modelsDir);
		}else if( Setting.SAVE_NEW_MODEL) {
			UtilClass.mkdirs(Setting.CURRENT_MODEL_DIR);
		}
		
/*		for (int i = 0; i < detectors.length; ++i){
			detectors[i] = factory.getAnomalousDetector(algorithmName, this, i);
		//	detectors[i].init(Setting.TRAINING_SIZE, numOfAttributes);
			detectors[i].loadModel(Setting.WORK_DIR+"/models/P"+i+"_2013_11_12"+UtilDate.getDateString());
		}
*/	}	
	
	private boolean loadModels(String modelsDir) throws Exception{
		File dir = new File(modelsDir);
		if (dir==null || !dir.exists() || !dir.isDirectory())
			return false;
		File[] models = dir.listFiles();
		if (models==null || models.length==0)
			return false;
		
		for (int i = 0; i < models.length; ++i){
			AnomalousDetector detector = createDetector(models[i].getName().substring(2));
			detector.loadModel(models[i].getAbsolutePath());
		}
		return true;
	}

	private synchronized AnomalousDetector createDetector(String modelID) throws Exception{
		AnomalousDetector detector = factory.getAnomalousDetector(algorithmName, this, modelID);
		detectorsMap.put(modelID, detector);
		return detector;
	}
	
	public synchronized void addTaskToThreadPool(Task task){
		threadPool.addTask(task);
	}

	public void processCommand(String geohash, Instance instance) throws Exception{
		if (instance.get(0)==Setting.START_TRAINING)
			startTraining();
/*		else if(instance.get(0)==Setting.START_TIMER)
			startTimer();
		else if(instance.get(0)==Setting.PUBLISH_OBS)
			processAll(geohash,instance);
*/		
	}
	private synchronized void processAll(String geohash, Instance instance) throws Exception{
		System.out.println("Publish command has been received!");
		for(AnomalousDetector detector : detectorsMap.values()){
			detector.addReceivedSample();
			detector.process(instance);
		}
	}

	//this should check the command in geohash and then call the appropriate method
	//but now we call just train
	private synchronized void startTraining() throws Exception{
		long total=0;
		System.out.println("Training command has been received!");
		for(AnomalousDetector detector : detectorsMap.values()){
			total+=detector.enforceTrain(detectorsMap.size());
		}
		System.out.println("Master Statistic- number of instances:"+detectorsMap.size()+"   & number of received samples:"+total);
	}
	
	public synchronized void process(String geohash, Instance instance) throws Exception{
		if (instance.get(0) < 0)
			processCommand(geohash,instance);
		else
			processInstance(geohash,instance);
	}

	public void processInstance(String geohash, Instance instance) throws Exception{
		instance.setID(geohash);
		String modelID = geohash.substring(0, 4);
		AnomalousDetector detector = detectorsMap.get(modelID);
		if (detector == null){
			detector = createDetector(modelID);
			detector.init(Setting.TRAINING_SIZE, numOfAttributes);
		}
		if (detector != null){
			detector.addReceivedSample();
			detector.process(instance);
		}
	}

/*	public void process(String geohash, Instance instance) throws Exception{
        instance.setID(geohash);
        int processID = UtilClass.getRegionNum(geohash, 12, detectors.length);
        	
        detectors[processID].process(instance);		
	}
*/
	public void anomaliesAlarm(int notifier, Instance instance){
/*		for (int i = 0; i < detectors.length; ++i){
			 detectors[i].falseAlarmResponse(null);
		}
*/	}
	
	public void anomaliesSuspicion(int Notifier, Instance instance){
		
	}
	
/*	//////////////////////////////////////////////////////////////////////////
	//		benchmark part
	//////////////////////////////////////////////////////////////////////////
	private TimerThread throuhputTimer;
	private void createTimer(){
		throuhputTimer = new TimerThread(this);
		System.out.println("TimerThread is created");
	}
	
	private void startTimer(){
		if (!throuhputTimer.isAlive()){
			throuhputTimer.start();
			System.out.println("ThrouhputReport command has been received!");
		}
		
	}
	
*/	public synchronized void  printThrouhputReport(){
		long rtotal=0, received =0;
		long ptotal=0, processed=0;
		for(AnomalousDetector detector : detectorsMap.values()){
			received  = detector.getReceivedSample();
			processed = detector.getProcessedSample();
			detector.resestProcessedSample();
			rtotal += received;
			ptotal += processed;
	//		System.out.println(detector.getProcessID()+" , received:"+received+" , processed:"+processed);
		}
		System.out.println("Master Statistic- number of instances:"+detectorsMap.size()+" , number of received samples:"+rtotal+" , number of processed samples:"+ptotal);
	}

}
