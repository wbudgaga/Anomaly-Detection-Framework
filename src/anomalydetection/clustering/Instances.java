package anomalydetection.clustering;


import java.util.ArrayList;
import java.util.Random;

import anomalydetection.util.UtilClass;

public class Instances {
  public static final int MIN = 0;
  public static final int MAX = 1;
  public static final int DIF = 2;
  
  protected ArrayList<Instance> instances 		= new ArrayList<Instance> ();
  protected int 				numOfAttribute 	= 0;
  protected double[][]			ranges;
 
 
  public Instances(int numOfAttribute) {
	  this.numOfAttribute 	= numOfAttribute;
	  ranges 				= new double[numOfAttribute][3];
	  for(int i = 0; i < numOfAttribute; ++i){
		  ranges[i][MIN] =  Double.MAX_VALUE;
		  ranges[i][MAX] = -Double.MAX_VALUE;;
	  }
  }

  public Instances(ArrayList<Instance>  inst) {
	  this(inst.get(0).numAttributes());
	  for (int i=0; i<inst.size();++i)
		  add(inst.get(i));
  }
  public void cleanData(){
	  instances.clear();
  }
  
  public Instances(Instances instances) {
	  this(instances,instances.getNumOfInstances());
	  instances.copyInstances(0,this,instances.getNumOfInstances());
  }

  public Instances(Instances source, int capacity) {
	  this(source.instance(0).numAttributes());
	  instances = new ArrayList<Instance>(capacity);
  }

  private void updateRanges(Instance inst){
	  for(int i = 0; i < numOfAttribute; ++i){
		  if (inst.get(i) < ranges[i][MIN])
			  ranges[i][MIN] = inst.get(i);
		  if (inst.get(i) > ranges[i][MAX])
			  ranges[i][MAX] = inst.get(i);
		  ranges[i][DIF] = ranges[i][MAX]  - ranges[i][MIN] ;
		  if (ranges[i][DIF] <0.5)
			  ranges[i][DIF] = 1 - ranges[i][DIF] ;
	  }
  }
  
  public int getNumOfAttribute() {
    return numOfAttribute;
  }
  
  public ArrayList<Instance> getInstances(){
	  return instances;
  }
  
  public int getNumOfInstances(){
	  return instances.size();
  }
  
  public void add(Instance instance) {
	  updateRanges(instance);
	  instances.add(instance);
  }

  public Instance instance(int i) {
	  return (Instance)instances.get(i);
  }

  protected void copyInstances(int from, Instances dest, int num) {
	  for (int i = 0; i < num; i++) {
		  dest.add(instance(from + i));
	  }
  }

  /**
  * Gets the value of all instances in this dataset for a particular
  * attribute. Useful in conjunction with Utils.sort to allow iterating
  * through the dataset in sorted order for some attribute.
  *
  * @param index the index of the attribute.
  * @return an array containing the value of the desired attribute for
  * each instance in the dataset. 
  */
 //@ requires 0 <= index && index < numAttributes();
  public double [] attributeToDoubleArray(int index) {
	  double [] result = new double[getNumOfInstances()];
	  for (int i = 0; i < result.length; i++) {
		  result[i] = instance(i).get(index);
	  }
	  return result;
  }
  
  public void swap(int i, int j){
	  Instance tmp = instances.get(i);
	  instances.set(i, instances.get(j));
	  instances.set(j, tmp);
  }

  public void remove(int index) {
	  instances.remove(index);
  }

  public Instance set(int index, Instance instance) {
	    Instance newInstance = (Instance) instance.clone();
	    Instance oldInstance = instances.get(index);   
	   instances.set(index, newInstance);
	    return oldInstance;
  }

  public void randomize(Random random) {
	  for (int j = getNumOfInstances() - 1; j > 0; j--)
		  swap(j, random.nextInt(j+1));
	  }

  /**
   * Creates the training set for one fold of a cross-validation 
   * on the dataset. The data is subsequently randomized based
   * on the given random number generator.
   *
   * @param numFolds the number of folds in the cross-validation. Must
   * be greater than 1.
   * @param numFold 0 for the first fold, 1 for the second, ...
   * @param random the random number generator
   * @return the training set 
   * @throws IllegalArgumentException if the number of folds is less than 2
   * or greater than the number of instances.
   */
  //@ requires 2 <= numFolds && numFolds < numInstances();
  //@ requires 0 <= numFold && numFold < numFolds;
  public Instances trainCV(int numFolds, int numFold, Random random) {
	  Instances train = trainCV(numFolds, numFold);
	  train.randomize(random);
	  return train;
  }

  /**
   * Creates the training set for one fold of a cross-validation 
   * on the dataset. 
   *
   * @param numFolds the number of folds in the cross-validation. Must
   * be greater than 1.
   * @param numFold 0 for the first fold, 1 for the second, ...
   * @return the training set 
   * @throws IllegalArgumentException if the number of folds is less than 2
   * or greater than the number of instances.
   */
  //@ requires 2 <= numFolds && numFolds < numInstances();
  //@ requires 0 <= numFold && numFold < numFolds;
  public Instances trainCV(int numFolds, int numFold) {
	  int numInstForFold, first, offset;
	  Instances train;

	  numInstForFold = getNumOfInstances() / numFolds;
	  if (numFold < getNumOfInstances() % numFolds) {
		  numInstForFold++;
		  offset = numFold;
	  }else
		  offset = getNumOfInstances() % numFolds;
    
	  train = new Instances(this, getNumOfInstances() - numInstForFold);
	  first = numFold * (getNumOfInstances() / numFolds) + offset;
	  copyInstances(0, train, first);
	  copyInstances(first + numInstForFold, train,  getNumOfInstances() - first - numInstForFold);
    return train;
  }
  
  public Instances testCV(int numFolds, int numFold) {
	  int numInstForFold, first, offset;
	  Instances test;
	  numInstForFold = getNumOfInstances() / numFolds;
	  
	  if (numFold < getNumOfInstances() % numFolds){
	      numInstForFold++;
	      offset = numFold;
	    }else
	      offset = getNumOfInstances() % numFolds;
	    test = new Instances(this, numInstForFold);
	    first = numFold * (getNumOfInstances() / numFolds) + offset;
	    copyInstances(first, test, numInstForFold);
	    return test;
  }
	 
  public double[][] getRanges() {
		return ranges;
	}


  /**
  * Calculates summary statistics on the values that appear in this
  * set of instances for a specified attribute.
  *
  * @param index the index of the attribute to summarize (index starts with 0)
  * @return an AttributeStats object with it's fields calculated.
  **/
 //@ requires 0 <= index && index < numAttributes();
  public AttributeStats attributeStats(int index) {
	 AttributeStats result 	= new AttributeStats();
	 result.numericStats 	= new Stats();
   
	 result.totalCount 		= getNumOfInstances();

	 double [] 	attVals 	= attributeToDoubleArray(index);
	 int [] 	sorted 		= UtilClass.sort(attVals);
	 int 		currentCount= 0;
	 double currentWeight 	= 0;
	 double prev = Double.NaN;
	 for (int j = 0; j < getNumOfInstances(); j++) {
		 Instance current = instance(sorted[j]);
		 if (current.get(index) == prev) {
			 currentCount++;
			 currentWeight += current.getWeight();
		 } else {
			 result.addDistinct(prev, currentCount, currentWeight);
			 currentCount = 1;
			 currentWeight = current.getWeight();
			 prev = current.get(index);
		 }
	 }
	 result.addDistinct(prev, currentCount, currentWeight);
	 result.distinctCount--; // So we don't count "missing" as a value 
	 return result;
  }

}
