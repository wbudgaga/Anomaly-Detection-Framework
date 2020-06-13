package anomalydetection.clustering.em;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import anomalydetection.clustering.Instance;
import anomalydetection.clustering.ObservationClusteringTask;
import anomalydetection.clustering.Setting;
/*
 * this class is used in the case of clustering only when the number of collected samples reaches user defined pariodSize
 */
public class PariodProcess extends EMProcess{
	private  int 					pariodSize =10;
	private  int 					currNumber =0;
	private  ObservationClusteringTask 	clusteringTask;
	
	public PariodProcess(EMAnomalousDetector ad, EMClusterer	emClusterer) throws Exception{
		super(ad,emClusterer);
		clusteringTask  = new ObservationClusteringTask();
		//setModelStatus(COLLECTING);
		pariodSize 		= Setting.CLUSTERING_RATE;
		//log("Collecting taringing data)");
	}
	
	public void process(Instance instance) throws Exception{
		++currNumber;
		enqueueInstance(instance);
	}
	

	private void enqueueInstance(Instance instance) throws Exception{
		clusteringTask.enqueueInstance(instance);
		if (currNumber==pariodSize){
			anomalousDetector.addTaskToThreadPool(clusteringTask);
			clusteringTask  = new ObservationClusteringTask();
			currNumber =0;
		}
	}
	
	public void loadModel(String fileName) throws NumberFormatException, IOException{
		emClusterer.loadModel(fileName);	
	}

}
