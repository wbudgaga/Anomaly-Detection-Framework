package anomalydetection.clustering.em;

import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Setting;

public class AdabtableMainProcess extends EMProcess{		
	protected int numOfClusteredObsSinceLastUpdate = 0;
	
	public AdabtableMainProcess(EMAnomalousDetector ad,EMClusterer	emClusterer) throws Exception {
		super(ad,emClusterer);
	}

	public void cluster(Instance instance){
		try {
			double[] logDensityForInstance={-1};
			
			++numOfClusteredObsSinceLastUpdate;
			if (numOfClusteredObsSinceLastUpdate == Setting.CLUSTERING_RATE){
				numOfClusteredObsSinceLastUpdate=0;
				double llk= emClusterer.clusterUpdate(instance);
			}else{
				double clusteringResults = emClusterer.evaluateInstance(instance, logDensityForInstance);
			}

		}catch(Exception e){
		}
	}

}
