package anomalydetection.clustering.distance;

public interface Distance {
	public double distance(double[] obs1, double[] obs2, double[][]ranges) throws Exception;
	public double distance(double[] obs1, double[] obs2, double[][]ranges,  double cutOffValue) throws Exception;
	
	public double distance(double[] obs1, double[] obs2) throws Exception;
	public double distance(double[] obs1, double[] obs2,  double cutOffValue) throws Exception;
	
	public void updateCovInv() throws Exception;
}
