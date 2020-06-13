package anomalydetection.clustering.kmean;

import anomalydetection.clustering.AnomalousDetector;
import anomalydetection.clustering.ClusteringTask;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.InitialProcess;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Setting;
import anomalydetection.clustering.em.EMClusterer;

public class KMeanAnomalousDetector extends AnomalousDetector{
	
	private SimpleKMeanClusterer_maha simpleKmeanClusterer;
	
	public KMeanAnomalousDetector(DetectorMaster master, String pID,double clusterThreshold){
		super(master, pID);
	}
	
	public KMeanAnomalousDetector(DetectorMaster master, String pID, int number){
		super(master, pID);
		simpleKmeanClusterer = new SimpleKMeanClusterer_maha(number);
	}

	public void train(){
		addTaskToThreadPool(new TrainingTask(simpleKmeanClusterer,(InitialProcess) this.process));
	}

	@Override
	public void falseAlarmResponse(Instance instance) {
	}

	@Override
	public void loadModel(String fileName) throws Exception {
		try {
			createDetectorProcess(new ClusteringTask());
			this.process.loadModel(fileName);
			System.out.println("Process: "+getProcessID()+" successufully loaded a model from "+fileName);
		} catch (Exception e) {
			System.out.println("Process: "+getProcessID()+" could not load a model from "+fileName+"==>"+e.getMessage());
		}
	}

	public String getBuiltMSG(){
			return "KMeans clustering model is built with squared error: "+simpleKmeanClusterer.getSquaredError();
	}

	public void createDetectorProcess(ClusteringTask clusteringTask) throws Exception{
		synchronized(lock){
			if (Setting.ONLINE_ADAPTABLE)
				this.process = new AdaptableKMeanProcess(this,simpleKmeanClusterer);
			else
				this.process = new KMeanProcess(this,simpleKmeanClusterer);
			
			this.process.setClusteringTask(clusteringTask);
		}
	}
}
