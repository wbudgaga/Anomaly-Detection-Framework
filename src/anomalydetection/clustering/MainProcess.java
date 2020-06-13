package anomalydetection.clustering;

import java.io.IOException;

public abstract class MainProcess extends Process{

	public MainProcess(AnomalousDetector ad) {
		super(ad);
	}
	
	public void process(Instance instance) throws Exception{
		clusteringTask.enqueueInstance(instance);
		anomalousDetector.addTaskToThreadPool(clusteringTask);
	}
				
	public abstract void cluster(Instance instance);
	
	public abstract void loadModel(String fileName) throws NumberFormatException, IOException, Exception;

}
