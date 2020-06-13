package anomalydetection.clustering.kmean;

import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Instances;

public interface KMeansClustering {
	public void buildClusterer(Instances data) throws Exception;
	public double[] clusterInstance(Instance instance) throws Exception;
	public double getMinDist(Instance instance) throws Exception;
	public void setNumClusters(int n);
}
