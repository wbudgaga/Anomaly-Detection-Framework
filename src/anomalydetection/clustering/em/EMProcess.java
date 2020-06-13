package anomalydetection.clustering.em;

import java.io.IOException;

import anomalydetection.clustering.AnomalousDetector;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.MainProcess;
import anomalydetection.clustering.Process;

public class EMProcess extends MainProcess{		
	protected EMClusterer	emClusterer;
	private double[]		logDensityForInstance={1.0};


	public EMProcess(AnomalousDetector ad,EMClusterer	emClusterer) throws Exception{
		super(ad);
		this.emClusterer = emClusterer;
	}
					
	public void cluster(Instance instance) {
		synchronized (emClusterer){			
			try {
				double clusteringResults = emClusterer.evaluateInstance(instance, logDensityForInstance);
				if (clusteringResults<-5)
					System.out.println(getProcessID()+":  "+clusteringResults+","+instance.toString());
				
			}catch(Exception e){
				System.out.println(getProcessID()+"########################  "+e+" === "+e.getMessage()+"  vvv  :"+emClusterer);
			}
		}
	}
	
	public void loadModel(String fileName) throws NumberFormatException, IOException{
		emClusterer.loadModel(fileName);	
	}
	
}
