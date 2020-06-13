package anomalydetection.clustering.kmean;


import java.util.ArrayList;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Instance;

public class Cluster {
	private Instance 	mean;
	private double 		squaredError = 0;
	private double[]	sum;
	private double[]	sumSquared;
	private double		sumOfWeights;
	private ArrayList<String> members = new ArrayList<String>();

	public Cluster(Instance dataSample){
//		sum						= dataSample.clone();
		mean		= dataSample.clone();
		sum 		= new double[dataSample.numAttributes()];
		sumSquared 	= new double[dataSample.numAttributes()];
		addMember(dataSample);
	}

	public double getSquaredError(){
		return  squaredError;
	}
		
	public void setSquaredError(double minDist){
		squaredError += (minDist * minDist);
	}

	public void addMember(Instance dataSample,double minDist){
		setSquaredError(minDist);
		addMember(dataSample);
	}
	
	public void addMember(Instance dataSample){
		members.add(dataSample.getID());
		double weightedAtt;
		for(int i=0; i<sum.length;++i){
			weightedAtt 	=  dataSample.getWeight() * dataSample.get(i);
			sum[i] 			+= weightedAtt;
			sumSquared[i] 	+= weightedAtt *  dataSample.get(i);	
			mean.set(i, sum[i]/members.size());
		}
		sumOfWeights += dataSample.getWeight();
	}
	
	public double[] variance(){
		double[] var= new double[sum.length];
		for(int i=0; i < sum.length;++i){
			var[i] = Math.sqrt((sumSquared[i] - (sum[i] * sum[i] / sumOfWeights)) / (sumOfWeights - 1));
			if (var[i] < 0)
				var[i] = 0;
		}
		
		return var;
	}
	public double distance(Instance dataSample){
		return mean.distance(dataSample);
	}
	public double distance(Instance dataSample, double[][] ranges){
		return mean.distance(dataSample,ranges);
	}

	
	public void reset(){
		members.clear();
		sum 		= new double[sum.length];
		addMember(mean);
		squaredError = 0;
		sumOfWeights = 0;
	}
			
	public ArrayList<String> getMembers(){
		return members;
	}
	public Instance getMean(){
		return mean;
	}
	
	
	public String getClusterKey(int[] featuresIdx){
		String id ="";
		for(int i=0;i<featuresIdx.length  ;++i)
			id += mean.get(featuresIdx[i]);
		return  id;
	}


	public int getSize(){
		return (int)sumOfWeights;
	}
	
	public String toString(){
		String s ="";
		for (double ds:mean.get())
			s += ds+",";
		return s.substring(0, s.length() -2);
	}
	
	 public static void main(String[] args){
		 DataSample d = new DataSample("1",new double[]{93.0 ,  1080.0,   397.9013671875,      0.0,   0.0 ,  -20.0  , 4.313460826873779 });
		 DataSample d1 = new DataSample("1",new double[]{93.0 ,  1080.0,   297.9013671875,      0.0,   0.0 ,  80.0  , 4.313460826873779 });
		 Cluster c = new Cluster(d);
		 System.out.println(c.distance(d1));
	 }
}
