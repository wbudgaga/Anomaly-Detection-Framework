package anomalydetection.clustering;

public interface Instance {
	public Instance 	clone();
	public String 		getID();	
	public void 		setID(String id);
	public int 			numAttributes();
	public double 		getWeight();
	public void 		setWeight(double weight); 
	public void 		set(int i, double value);
	public double 		get(int attIndex);	
	public double[] 	get();
	public double 		distance(Instance dataSample);
	public double distance(Instance dataSample, double[][]ranges);
	public DataSample 	getSTD();
}