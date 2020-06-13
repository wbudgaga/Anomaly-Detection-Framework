package anomalydetection.clustering.em;

import java.io.IOException;
import java.util.ArrayList;

import anomalydetection.clustering.AnomalousDetector;
import anomalydetection.clustering.ClusteringTask;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.InitialProcess;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Process;
import anomalydetection.clustering.Setting;

public class EMAnomalousDetector extends AnomalousDetector{
	private 	EMClusterer			emClusterer;
	private	  	ArrayList<String>	anomalousList		= new ArrayList<String>();
	
	public EMAnomalousDetector(DetectorMaster master, String pID) throws Exception {
		super(master, pID);
		emClusterer = new EMClusterer(Setting.EM_INIT_CLUSTERER);
	}

		
	public void train(){
		addTaskToThreadPool(new TrainingTask(emClusterer,(InitialProcess) this.process));
	}

	protected void addAnomalous(String instance, double logDensity){
		synchronized(anomalousList){ 
			anomalousList.add(instance+" : ("+logDensity+")");
		}
	}

	public void printAnomalous(){
		for(String inst:anomalousList)
			System.out.println(inst);
	}

	@Override
	public void falseAlarmResponse(Instance instance) {
		System.out.println(this.process.tempCounterForMeasureTime);
	}

	@Override
	public void loadModel(String fileName) {
		try {
			emClusterer = new EMClusterer(Setting.EM_INIT_CLUSTERER);
			createDetectorProcess(new ClusteringTask());
			this.process.loadModel(fileName);
			System.out.println("Process: "+getProcessID()+" successufully loaded a model from "+fileName);
		} catch (Exception e) {
			System.out.println("Process: "+getProcessID()+" could not load a model from "+fileName);
		}
		
	}
	

	public String getBuiltMSG(){
		return "GMM clustering model is built with llk: "+emClusterer.getModelLlk();
}

	public void createDetectorProcess(ClusteringTask clusteringTask) throws Exception{
		synchronized(lock){
			if (Setting.ONLINE_ADAPTABLE)
				this.process = new AdabtableMainProcess(this,emClusterer);
			else
				this.process = new EMProcess(this,emClusterer);
			
			this.process.setClusteringTask(clusteringTask);
		}
	}

}
