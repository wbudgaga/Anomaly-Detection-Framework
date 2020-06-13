package anomalydetection.clustering;

import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.Setting;

public class InitialProcess extends Process{
	private final int 		trainingSetSize;
	private Instances 		trainingSet;
	protected volatile int	modelStatus;		
	
	public InitialProcess(AnomalousDetector ad, int numOfAttributes, int trainingSize) throws Exception{
		super(ad);
		trainingSetSize	= trainingSize;
		trainingSet 	= new Instances(numOfAttributes);
		setClusteringTask(new ClusteringTask());
		setModelStatus(COLLECTING);
		System.out.println("Collecting training data has started on instance: "+getProcessID()+" (required size:"+trainingSetSize+")");
	}
	
	public Instances getTrainingSet(){
		return trainingSet;
	}
	public int getModelStatus() {
		return modelStatus;
	}

	public void setModelStatus(int modelStatus) {
		this.modelStatus = modelStatus;
	}
	
	public boolean isModelStatus(int modelStatus) {
		return this.modelStatus == modelStatus;
	}

	public void process(Instance instance) throws Exception{
		if (isModelStatus(COLLECTING)){
			trainingSet.add(instance);
			if ((trainingSet.getNumOfInstances() == trainingSetSize)){
				train();
			}
		}else{
			enqueueInstance(instance);
		}
	}
	
	public void train() throws Exception{
		setModelStatus(TRAINING);
		anomalousDetector.train();
	}
	
	private void enqueueInstance(Instance instance) throws Exception{
		clusteringTask.enqueueInstance(instance);
		if (isModelStatus(CLUSTERING)){
			anomalousDetector.createClusteringTask(clusteringTask); 
		}
	}
}
