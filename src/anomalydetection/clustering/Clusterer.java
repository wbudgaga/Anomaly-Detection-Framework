package anomalydetection.clustering;

import java.io.IOException;
import java.util.ArrayList;

import anomalydetection.clustering.kmean.Cluster;

public interface Clusterer {
	public void 				cluster(Instance sample);
	public void 				cluster(Instance sample, double[][] ranges);
	public void 				buildClusterer(Instances inst) throws Exception;
	public void 				saveModel(String filename) throws NumberFormatException, IOException;
	public ArrayList<Cluster> 	getClusters();
	public int[]			 	getClustersSizes();
	public void 				reset();
	public void				  	removeSmallClusters(int threshold);
	public Instances  getCentroids();
	public double getSquaredError();
}
