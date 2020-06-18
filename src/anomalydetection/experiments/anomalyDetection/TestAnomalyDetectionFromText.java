package anomalydetection.experiments.anomalyDetection;
import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.Setting;
import anomalydetection.threadpool.ThreadPoolManager;
import anomalydetection.util.UtilClass;

public class TestAnomalyDetectionFromText {
	private ThreadPoolManager threadPool; 
	private DetectorMaster 	anomalousDetector;
	public  int numOfPublisher = 5;

    public TestAnomalyDetectionFromText() throws Exception {
		try{
			anomalousDetector 	= new DetectorMaster(1); 
			anomalousDetector.start(Setting.MAIN_ALGORITHM, Setting.NUM_OF_ATTRIBUTES);
		}catch(Exception e){
			System.out.println("Exception in StorageNode: "+e.getMessage());
		}
		threadPool 	= new ThreadPoolManager(numOfPublisher);
		threadPool.start();
    }

    public void startPublish(String fName) throws Exception{
    	DataPublisherTask dpt = new DataPublisherTask(fName, anomalousDetector);
    	threadPool.addTask(dpt);
    }      
       
    public synchronized  void sendCommand(double command) throws Exception{
    	while (!threadPool.isIdle())
    		Thread.sleep(100);
    	anomalousDetector.process("9pkv", new DataSample(new double[]{command,-1,-1,-1,-1,-1,-1,-1,-1}));     	 
    	System.out.println("done!");
    }
 
    public static void main(String[] args) throws Exception{    		  
    	UtilClass.fileAsOutputDst("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/data/"+Setting.MAIN_ALGORITHM+".txt");
    	TestAnomalyDetectionFromText client = new TestAnomalyDetectionFromText();
    	client.startPublish("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/data/P_9pkv_05.txt");
    	client.startPublish("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/data/P_9pkv_06.txt");
    	client.startPublish("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/data/P_9pkv_07.txt");
		client.startPublish("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/data/P_9pkv_08.txt");
		client.startPublish("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/data/P_9pkv_09.txt");
		client.startPublish("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/data/P_9pkv_10.txt");
		client.sendCommand(Setting.START_TRAINING);
    }
}
