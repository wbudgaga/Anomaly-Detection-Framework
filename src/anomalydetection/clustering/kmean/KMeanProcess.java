package anomalydetection.clustering.kmean;

import java.io.IOException;



import anomalydetection.clustering.AnomalousDetector;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.MainProcess;


public class KMeanProcess extends MainProcess{		
	protected SimpleKMeanClusterer_maha	clusterer;

	public KMeanProcess(AnomalousDetector ad, SimpleKMeanClusterer_maha clusterer) throws Exception{
		super(ad);
		this.clusterer = clusterer;
	}
					
	public void cluster(Instance instance) {
		synchronized (clusterer){			
			try {
				double[] c = clusterer.clusterInstance(instance);
				if (c[1]>5)
					System.out.println(getProcessID()+":  "+c[1]+","+instance.toString());

				//Cluster c = clusterer.clusterInstance(instance);
			}catch(Exception e){
			//	System.out.println(getProcessID()+"########################  "+e+" === "+e.getMessage()+"  vvv  :"+emClusterer);
			}
/*			if (emClusterer.clusterInstance(instance, logDensityForInstance)==-1)
				anomalousDetector.addAnomalous(instance.toString(),-1);
*/		}
	}
	
	public void loadModel(String fileName) throws Exception{
		clusterer.loadModel(fileName);	
	}


	
}
