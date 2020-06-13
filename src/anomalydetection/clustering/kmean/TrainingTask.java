
package anomalydetection.clustering.kmean;

import java.io.IOException;

import anomalydetection.clustering.InitialProcess;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.Process;
import anomalydetection.clustering.Setting;
import anomalydetection.threadpool.Task;

public class TrainingTask extends Task{
	private Instances 				instances;
	private SimpleKMeanClusterer_maha 	kmeanClusterer;
	private InitialProcess  		initialProcess;
	
	public  TrainingTask(SimpleKMeanClusterer_maha kmeanClusterer, InitialProcess initProcess){
		this.kmeanClusterer = kmeanClusterer;
		this.initialProcess	= initProcess;
		this.instances 		= this.initialProcess.getTrainingSet();
	}

	private void saveModel(){		
		try {
			kmeanClusterer.saveModel(Setting.CURRENT_MODEL_DIR+"/P_"+initialProcess.getProcessID());
		} catch (IOException e) {
			System.out.println("Could not save the model in: "+Setting.CURRENT_MODEL_DIR+"/P_"+initialProcess.getProcessID()+". Details:"+e.getMessage());
		}
	}
	
	private int findNumOfClusters() throws Exception{
		CrossValidation  cv = new CrossValidation(kmeanClusterer);
		return cv.CVClusters(instances);
	}
	@Override
	public void execute() {
		try {
			System.out.println(initialProcess.getProcessID()+": Training on "+instances.getNumOfInstances()+" instances");
			kmeanClusterer.setNumClusters(findNumOfClusters());
			kmeanClusterer.buildClusterer(instances);
			if (Setting.SAVE_NEW_MODEL)
				saveModel();
			instances = null;
			initialProcess.setModelStatus(Process.CLUSTERING);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
