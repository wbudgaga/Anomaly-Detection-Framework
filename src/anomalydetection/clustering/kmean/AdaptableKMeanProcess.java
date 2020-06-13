package anomalydetection.clustering.kmean;

import opendap.servlet.GetInfoHandler;
import anomalydetection.clustering.AnomalousDetector;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Setting;
import anomalydetection.util.UtilClass;
import anomalydetection.util.UtilDate;


public class AdaptableKMeanProcess extends KMeanProcess{		
	protected int numOfClusteredObsSinceLastUpdate = 0;
	protected int numOfClusteredObsSinceLastSave = 0;

	public AdaptableKMeanProcess(AnomalousDetector ad, SimpleKMeanClusterer_maha clusterer) throws Exception{
		super(ad,clusterer);
		if (Setting.SAVE_RATE > 0)
			UtilClass.mkdirs(Setting.TP_DIR);
	}
					
	public void cluster(Instance instance) {
		synchronized (clusterer){			
			try {
				double[] clusteringResults = clusterer.clusterInstance(instance);
				++numOfClusteredObsSinceLastUpdate;
				if (numOfClusteredObsSinceLastUpdate == Setting.CLUSTERING_RATE){
					numOfClusteredObsSinceLastUpdate=0;
					clusterer.updateClusterParam(instance, clusteringResults);
				}
				if (Setting.SAVE_RATE>0){
					++numOfClusteredObsSinceLastSave;
					if (numOfClusteredObsSinceLastSave == Setting.SAVE_RATE){
						numOfClusteredObsSinceLastUpdate=0;
						clusterer.saveModel(Setting.TP_DIR+getProcessID()+"_"+UtilDate.getDateString()+".txt");
					}
				}
			}catch(Exception e){
			   System.out.println(getProcessID()+": Exception thrown in AdaptiveKMeanProcess ("+e.getMessage()+")");
			}
		}
	}	
}
