package anomalydetection.clustering.distance;

import anomalydetection.clustering.Instances;

public class Euclidean implements Distance{

	@Override
	public double distance(double[] obs1, double[] obs2, double[][] ranges) {
		double sum = 0, dif;
		for(int i=0;i<obs1.length  ;++i){ 
			double att1 = (obs1[i] - ranges[i][Instances.MIN])/ranges[i][Instances.DIF];
			double att2 = (obs2[i] - ranges[i][Instances.MIN])/ranges[i][Instances.DIF];
			dif = att1 - att2;
			sum += dif *  dif;
		}
		return sum;
	}

	@Override
	public double distance(double[] obs1, double[] obs2) {
		double sum = 0, dif;
		for(int i=0;i<obs1.length  ;++i){ 
			dif = obs1[i]-obs2[i];
			sum += dif *  dif;
		}
		return sum;
	}

	@Override
	public double distance(double[] obs1, double[] obs2, double[][] ranges,	double cutOffValue) throws Exception {
		double sum = 0, dif;
		for(int i=0;i<obs1.length  ;++i){ 
			double att1 = (obs1[i] - ranges[i][Instances.MIN])/ranges[i][Instances.DIF];
			double att2 = (obs2[i] - ranges[i][Instances.MIN])/ranges[i][Instances.DIF];
			dif = att1 - att2;
			sum += dif *  dif;
			if (sum>cutOffValue)
				return Double.MAX_VALUE;
		}
		return sum;
	}

	@Override
	public double distance(double[] obs1, double[] obs2, double cutOffValue)throws Exception {
		double sum = 0, dif;
		for(int i=0;i<obs1.length  ;++i){ 
			dif = obs1[i]-obs2[i];
			sum += dif *  dif;
			if (sum>cutOffValue)
				return Double.MAX_VALUE;
		}
		return sum;
	}

	@Override
	public void updateCovInv() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
