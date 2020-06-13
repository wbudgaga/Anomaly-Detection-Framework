package anomalydetection.clustering;

import anomalydetection.util.UtilClass;


public class DataSample implements Instance{
	private String 		id;
    private double[] 	features ;
    private double 		weight;

    private DataSample(DataSample dataSample) {
    	this.id 	= dataSample.id;
    	features 	= dataSample.features.clone();
    	setWeight(dataSample.weight);
    }

    public Instance clone(){
    	return new DataSample(this);
    }
    public DataSample(double[] input) {
    	this.id 	= "-1";
    	features 	= input.clone();
    	setWeight(1);
    }

    public DataSample(String id, double[] input) {
    	this.id 	= id;
    	features 	= input.clone();
    	setWeight(1);
//    	HelperClass.normalize(features);
    }
    
    public DataSample(String id,double weight, double[] input) {
    	this(id, input);
    	setWeight(weight);
    }

    public int numAttributes(){
    	return features.length;
    }
 
    public double get(int i) {
        return features[i];
    }
    
    public void set(int i, double value) {
        features[i] = value;
    }

    public double[] get() {
        return features;
    }
 
	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}
	
	public double distance(Instance dataSample, double[][]ranges){
		double[] values = dataSample.get();
		double sum = 0, dif;
		for(int i=0;i<features.length  ;++i){ 
			double att1 = (features[i]-ranges[i][Instances.MIN])/ranges[i][Instances.DIF];
			double att2 = (values[i] -ranges[i][Instances.MIN])/ranges[i][Instances.DIF];
			dif = att1 - att2;
			sum += dif *  dif;
		}
		return Math.sqrt(sum);
	}

	public double distance(Instance dataSample){
		double[] values = dataSample.get();
		double sum = 0, dif;
		for(int i=0;i<features.length  ;++i){ 
			dif = features[i]-values[i];
			sum += dif *  dif;
		}
		return Math.sqrt(sum);
	}

	public DataSample getSTD(){
		double[] std = features.clone();
		UtilClass.normalizeSTD(std);
		return new DataSample(std);
	}
	
	public String toString(){
		String s =getID()+"==>";
		for(int i=0;i<features.length;++i)
			s += features[i]+",";

		return s.substring(0, s.length() -2);
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
