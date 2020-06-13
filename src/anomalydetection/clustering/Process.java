
package anomalydetection.clustering;

import java.io.IOException;

import anomalydetection.clustering.Instance;

public abstract class Process{
	public static final int COLLECTING  = 0;
	public static final int TRAINING	= 1;
	public static final int CLUSTERING	= 2;
	
	protected AnomalousDetector 	anomalousDetector;
	protected ClusteringTask 		clusteringTask;

	public long tempCounterForMeasureTime = 0;
	
	public  Process(AnomalousDetector ad){
		anomalousDetector = ad;
	}

	protected void log(String text){
		//UtilClass.log(anomalousDetector.logger,text);
	}
	
	protected void cluster(Instance	sample){}

	public void setClusteringTask(ClusteringTask clusteringTask) {
		this.clusteringTask = clusteringTask;
		this.clusteringTask.setMainProcess(this);
	}

	public String getProcessID() {
		return anomalousDetector.getProcessID();
	}

	public void addProcessedSample() {
		anomalousDetector.addProcessedSample();
	}

	public void loadModel(String fileName) throws NumberFormatException, IOException, Exception{}
	public void train()throws Exception{}
    public abstract void process(Instance instance) throws Exception;

}
