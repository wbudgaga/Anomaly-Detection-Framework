
package anomalydetection.clustering;

import java.io.IOException;

import anomalydetection.clustering.em.EMAnomalousDetector;
import anomalydetection.clustering.kmean.KMeanAnomalousDetector;
import anomalydetection.clustering.kmean.SimpleKMeanClusterer;

public class ClustererFactory{
	private static final ClustererFactory instance = new ClustererFactory();
	private ClustererFactory(){}
	
	public static ClustererFactory getInstance(){
		return instance;
	}
	
/*	public Clusterer getClusterer(String algorithmName, double arg){
		if (algorithmName.compareToIgnoreCase("kmean")==0)
			return new KMeanClusterer(arg);// MST_Class.newInstance();
			
		return null;
	}
*/
	public SimpleKMeanClusterer getKMeansClusterer(String algorithmName, int numOfClusters){
		if (algorithmName.compareToIgnoreCase("kmean")==0)
			return new SimpleKMeanClusterer(numOfClusters);
			
		return null;
	}


	public AnomalousDetector getAnomalousDetector(String algorithmName, DetectorMaster master, String id) throws Exception{
		if (algorithmName.compareToIgnoreCase("EM")==0)
			return new EMAnomalousDetector(master, id);
		if (algorithmName.compareToIgnoreCase("KMEANS")==0)
			return new KMeanAnomalousDetector(master, id, Setting.NUM_OF_CLUSTERS);
			
		return null;
	}

}
