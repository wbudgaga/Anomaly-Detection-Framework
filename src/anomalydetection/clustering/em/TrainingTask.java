
package anomalydetection.clustering.em;

import java.io.IOException;

import anomalydetection.clustering.InitialProcess;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.Setting;
import anomalydetection.threadpool.Task;

public class TrainingTask extends Task{
	private Instances 		instances;
	private EMClusterer		emClusterer;
	private InitialProcess  initialProcess;
	
	public  TrainingTask(EMClusterer emClusterer, InitialProcess initProcess){
		this.emClusterer 	= emClusterer;
		this.initialProcess	= initProcess;
		this.instances 		= this.initialProcess.getTrainingSet();
	}

	private void saveModel(){		
		try {
			emClusterer.saveModel(Setting.CURRENT_MODEL_DIR+"/P_"+initialProcess.getProcessID());
		} catch (IOException e) {
			System.out.println("Could not save the model in: "+Setting.CURRENT_MODEL_DIR+"/P_"+initialProcess.getProcessID()+". Details:"+e.getMessage());
		}
	}
	
	@Override
	public void execute() {
		try {
			System.out.println(initialProcess.getProcessID()+": Training on "+instances.getNumOfInstances()+" instances");
			emClusterer.buildClusterer(instances);
			if (Setting.SAVE_NEW_MODEL)
				saveModel();
			instances = null;
			initialProcess.setModelStatus(EMProcess.CLUSTERING);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
