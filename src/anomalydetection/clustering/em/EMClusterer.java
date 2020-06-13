package anomalydetection.clustering.em;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import anomalydetection.clustering.kmean.SimpleKMeanClusterer;
import anomalydetection.clustering.ClustererFactory;
import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.kmean.Cluster;
import anomalydetection.util.StatisticUtil;
import anomalydetection.util.UtilClass;

public class EMClusterer {	
	private double 			minLogOfLieklihoodImprovement 	= 1e-6;
	private double 			minLogOfLieklihoodImprovementCV = 1e-6;
	private int 			numOfCVFolds					 =10;
	private int 			numOfClusters;
	private int 			numOfAttributes;
	private Instances 		trainingInstances 				= null;
	private int 			maxNumOfIterations 				= 100;
	private Random 			randomGenerator;
	private int 			seed;
	private double 			minStdD 						= 1e-6;
	private double 			modelNormal[][][];				// normal estimators for each cluster 
	private double 			clustersPriors[];
	private double 			instancesWeightsInClusters[][];	//weights of each instance for each cluster
	private boolean 		inTraining =true;						//False once training has completed
	private SimpleKMeanClusterer 		clusterer; 						// used to find out the number of initial number of clusters
	private String			initClustererName;
	private long 			numOfInstances;
	private double 			modelllk=-99999;
	private Logger 			logger;

	  public EMClusterer(SimpleKMeanClusterer clusterer) throws Exception{
		  if (clusterer == null)
			  throw new Exception("Null clusterer!");
		  this.clusterer = clusterer;
	  }
	  
	  public EMClusterer(String algorithmName) throws Exception{
		  if (algorithmName == null || algorithmName.isEmpty())
			  throw new Exception("Null clusterer!");
		  this.initClustererName = algorithmName;	  
	  }
	  
/*	  public EMClusterer(String algorithmName, double clusterThreshold) throws Exception{
		  if (algorithmName == null || algorithmName.isEmpty())
			  throw new Exception("Null clusterer!");
		  this.clusterer = ClustererFactory.getInstance().getKMeansClusterer(algorithmName, clusterThreshold);
		  
	  }
*/
	  public synchronized boolean isInTraining(){
		  return inTraining;
	  }
	  
	  public synchronized void setInTraining(boolean state){
		  inTraining = state;
	  }

	  /**
	   * New probability estimators for an iteration
	   */
	  private void new_estimators() {
		  for (int i = 0; i < numOfClusters; i++) {
			  for (int j = 0; j < numOfAttributes; j++) {
				  modelNormal[i][j][0] = modelNormal[i][j][1] = modelNormal[i][j][2] = 0.0;
			  }
		  }
	  }

	  /**
	   * calculate prior probabilities for the clusters
	   **/
	  private void estimate_priors(Instances inst) throws Exception {
		  for (int i = 0; i < numOfClusters; i++) {
			  clustersPriors[i] = 0.0;
		  }

		  for (int i = 0; i < inst.getNumOfInstances(); i++) {
			  for (int j = 0; j < numOfClusters; j++) {
				  clustersPriors[j] += inst.instance(i).getWeight() * instancesWeightsInClusters[i][j];
			  }
		  }

		  UtilClass.normalize(clustersPriors);
	  }
	  
	  private void M_reEstimate(Instances inst) {
		    for (int j = 0; j < numOfAttributes; j++) {
		    	for (int i = 0; i < numOfClusters; i++) {
		    		if (modelNormal[i][j][2] <= 0) {
		    			modelNormal[i][j][1] = Double.MAX_VALUE;
		                modelNormal[i][j][0] = minStdD;
		    		} else {
		    			// variance
		    			modelNormal[i][j][1] = (modelNormal[i][j][1] - (modelNormal[i][j][0]
		    									* modelNormal[i][j][0] / modelNormal[i][j][2]))
		    									/ (modelNormal[i][j][2]);

		    			if (modelNormal[i][j][1] < 0) {
		    				modelNormal[i][j][1] = 0;
		    			}

		    			modelNormal[i][j][1] = Math.sqrt(modelNormal[i][j][1]);

		    			if ((modelNormal[i][j][1] <= minStdD)) {
		    				modelNormal[i][j][1] = inst.attributeStats(j).numericStats.stdDev;
		    				if ((modelNormal[i][j][1] <= minStdD)) {
		    					modelNormal[i][j][1] = minStdD;
		    				}
		    			}
		    			if ((modelNormal[i][j][1] <= 0)) {
		    				modelNormal[i][j][1] = minStdD;
		    			}
		    			if (Double.isInfinite(modelNormal[i][j][1])) {
		    				modelNormal[i][j][1] = minStdD;
		    			}
		    			// mean
		    			modelNormal[i][j][0] /= modelNormal[i][j][2];
		    		}
		    	}
		    }
	  }

	  
	  /**
	   * The M step of the EM algorithm.
	   * 
	   */
	  private void M(Instances inst) throws Exception {
		  new_estimators();
		  estimate_priors(inst);
		  // sum
		  for (int i = 0; i < numOfClusters; i++) {
			  for (int j = 0; j < numOfAttributes; j++) {
				  for (int l = 0; l < inst.getNumOfInstances(); l++) {
					  Instance in = inst.instance(l);
					  modelNormal[i][j][0] += (in.get(j) * in.getWeight() * instancesWeightsInClusters[l][i]);

					  modelNormal[i][j][2] += in.getWeight() * instancesWeightsInClusters[l][i];
					  modelNormal[i][j][1] += (in.get(j) * in.get(j) * in.getWeight() * instancesWeightsInClusters[l][i]);
				  }
			  }
		  }
		  // re-estimate Gaussian parameters
		  M_reEstimate(inst);
	  }

	  public double[] getClusterPriors() {
		  return clustersPriors;
	  }

	  /** 
	   * Returns the logs of the joint densities for a given instance X.
	   *   {P(X,C=1),P(X,C=2), ..., P(X,C=k)} ===> where P(X,C=1)= p(X|C=1)p(C=1)
	   *   Log(P(X,C=1)) = log(P(x1|C=1))+log(P(C=1))
	   */
	  public double[] logPXCPC(Instance inst)  {
		  double[] xClustersWights = new double[numOfClusters];
		  
		  for (int i = 0; i < numOfClusters; i++){
			  xClustersWights[i] = StatisticUtil.logPXC_k(inst.get(),modelNormal[i]) +  Math.log(clustersPriors[i]);
		  }
		  return xClustersWights;// Weights={log(P(x1|C=1))+log(P(C=1)),log(P(x1|C=2))+log(P(C=2)),..}
	  }

	  
	  /**
	   * Computes the density for a given instance.
	   * 
	   * @param instance the instance to compute the density for
	   * @return the density.
	   * @exception Exception if the density could not be computed successfully
	   */
	  public double logDensityForInstance(Instance instance)  {
		  double[] instanceWeights = logPXCPC(instance);
		  double max = instanceWeights[UtilClass.maxIndex(instanceWeights)];
		  double sum = StatisticUtil.expOfDiff2Max(instanceWeights,max);
		  return max + Math.log(sum);
	  }

	  /**
	   * Returns the cluster probability distribution for an instance.
	   *
	   * @param instance the instance to be clustered
	   * @return the probability distribution
	   * @throws Exception if computation fails
	   */  
	  public double[] distributionForInstance(Instance instance)  {
		  double[] instanceWeights = logPXCPC(instance);
		  double max = instanceWeights[UtilClass.maxIndex(instanceWeights)];

		  double sum = StatisticUtil.expOfDiff2Max(instanceWeights,max);// instanceWeights is updated by this call
		  StatisticUtil.normalize(instanceWeights, sum);
		  return instanceWeights;
	  }
	  
	  
	  /**
	   * The E step of the EM algorithm. Estimate cluster membership probabilities.
	   * 
	   * @param inst the training instances
	   * @param change_weights whether to change the weights
	   * @return the average log likelihood
	   * @throws Exception if computation fails
	   */
	  private double E(Instances inst, boolean change_weights) throws Exception {	
		  double loglk = 0.0, sOW = 0.0;
		  for (int i = 0; i < inst.getNumOfInstances(); i++) {
			  Instance in = inst.instance(i);
			  loglk += in.getWeight() * logDensityForInstance(in);
			  sOW += in.getWeight();
			  if (change_weights) {
				  instancesWeightsInClusters[i] = distributionForInstance(in);
			  }
		  }
		  return loglk / sOW;
	  }	  	  
	  
	  public double buildClusterer(Instances data) throws Exception {
		  setInTraining(true);
		  trainingInstances	 	=	data;
		  numOfAttributes 		= trainingInstances.getNumOfAttribute();
		  randomGenerator 		= new Random(getSeed());
		  UtilClass.throwAwayRandomNumbers(randomGenerator);
		  CVClusters();
		  randomGenerator = new Random(getSeed());
		  UtilClass.throwAwayRandomNumbers(randomGenerator);
		  EM_Init(trainingInstances);
		  modelllk = iterateEM(trainingInstances);
		  
		  numOfInstances = data.getNumOfInstances();
		  UtilClass.log(logger,"Mixture Gassuian Model (trained on "+numOfInstances+" instances) built with LLK: "+modelllk+" and has "+ numOfClusters+" clusters ");
		  // save memory
		  trainingInstances = new Instances(trainingInstances, 0);		
		  setInTraining(false);
		  return modelllk;
	  }
  
	  private void EM_Init(Instances inst) throws Exception {
		  this.clusterer = ClustererFactory.getInstance().getKMeansClusterer(initClustererName, numOfClusters);
		  clusterer.buildClusterer(inst);
		  
		  //ArrayList<Cluster> clusters 	= clusterer.getClusters();
		  numOfClusters 				= clusterer.getNumClusters();
		  instancesWeightsInClusters 	= new double[inst.getNumOfInstances()][numOfClusters];    
		  modelNormal 					= new double[numOfClusters][numOfAttributes][3];
		  Instances centers 			= new Instances(clusterer.getCentroids());
		  Instances clusterStd			= new Instances(clusterer.getClusterStdDevs());
		  int[] clusterSizes 			= clusterer.getClustersSizes();
		  int i 						= 0;
	  
		  for (i = 0; i < numOfClusters; i++) {
			  Instance center 			= centers.instance(i);
			  for (int j = 0; j < numOfAttributes; j++) {
				  double mean 			= center.get(j);
				  
				  modelNormal[i][j][0] 	= mean;
				  double stdv 			= clusterStd.instance(i).get(j);
				  if (stdv < minStdD) {
					  stdv = inst.attributeStats(j).numericStats.stdDev;
					  if (Double.isInfinite(stdv)) 
						  stdv = minStdD;
					  
					  if (stdv < minStdD) 
						  stdv = minStdD;
				  }
				  if (stdv <= 0) {
					  stdv = minStdD;
				  }
		          modelNormal[i][j][1] = stdv;
		          modelNormal[i][j][2] = 1.0;
			  }
		  }
		  clustersPriors = StatisticUtil.PC_K(clusterSizes);
	  }

	  private double iterateEM(Instances inst) throws Exception {
		  int 	 i 						= 0;
		  double llkold 				= 0.0; // old Loglikely
		  double llk 					= 0.0; // Loglikely
		  try {
			  for (; i < maxNumOfIterations; i++) {
				  llkold = llk;
				  llk = E(inst,true);
				  if (i > 0 && ((llk - llkold) < minLogOfLieklihoodImprovement)) 
					  break;
					  
				  M(inst);
			  }
		  } catch (Exception ex) {
			  ex.printStackTrace();
		  }
		  UtilClass.log(logger,"# of built clusters: "+numOfClusters+" # of performed iterations: " + i+" and the achieved likelihood: "+llk);
		  return llk;
	  }
	  
	  private void CVClusters() throws Exception {
		  double 	CVLogLikely 			= -Double.MAX_VALUE;
		  int 		numFolds 				= numOfCVFolds;
		  int 		num_clusters 			= 10;	
		  int 		seed 					= 1;
		  double 	templl, tll;
		  int	 	i;
		  Random 	random;
		  Instances trainCopy;

		 do{	
			 random 		= new Random(getSeed());
		     trainCopy 	= new Instances(trainingInstances);
		     trainCopy.randomize(random);
		     templl 		= 0.0;
		     for (i = 0; i < numFolds; i++) {
		    	 Instances cvTrain = trainCopy.trainCV(numFolds, i, random);
		    	 if (num_clusters > cvTrain.getNumOfInstances()) 
		    		 throw new Exception("The number of clusters is bigger than the number of instances");
		        
		    	 Instances cvTest 	= trainCopy.testCV(numFolds, i);
		    	 randomGenerator 	= new Random(seed);
	
		    	 UtilClass.throwAwayRandomNumbers(randomGenerator);
		    	 numOfClusters = num_clusters;
		    	 EM_Init(cvTrain);
		    	 try {
		    		 iterateEM(cvTrain);
		    	 } catch (Exception ex) {
			         // catch any problems - i.e. empty clusters occurring
		    		 throw new Exception("Cross Validation has been failed");
		    	 }
		    	 try {
		    		 tll = E(cvTest, false);
		    	 } catch (Exception ex) {
		    		 throw new Exception("Cross Validation has been failed");
		    	 }
		    	 templl += tll;
		     }//for
		     
		     seed = getSeed();
		     templl /= numFolds;
	    	 // if (templl > CVLogLikely) {
	    	 if (templl - CVLogLikely > minLogOfLieklihoodImprovementCV) {
		         CVLogLikely = templl;
		         num_clusters++;
	    	 }
		      
		  }while (numOfClusters != num_clusters) ;
		  numOfClusters = num_clusters - 1;
	  }
	  
	  public double[] getClustersSizes(){
		  double[] clustersSizes = new double[clustersPriors.length];
		  for (int j = 0; j < numOfClusters; j++) {
			  clustersSizes[j] = clustersPriors[j] * (numOfInstances) ;
		  }		  
		  return clustersSizes;
	  }
	  
	  private void loadParam(String line, int paraIdx,int attIdx){
		  String[] paraName = {"mean:","std:","weightsSum:"};
		  line 			= line.substring(paraName[paraIdx].length()).trim();
		  String[] values	= line.split(",");
		  double[] valuesList = UtilClass.toDoubleArray(values);
		  for(int j=0;j<numOfClusters;++j){
			  if (valuesList[j]==0.0)
				  valuesList[j]=0.000000000000001;
			  modelNormal[j][attIdx][paraIdx] = valuesList[j];
		  }
	  }
	  
	  public void saveModel(String filename) throws NumberFormatException, IOException{
		  String output = "llk:"+modelllk +"\n";
		  output 		+= "numOfInstances:"+numOfInstances +"\n";
		  output 		+= "numOfAttributes:"+numOfAttributes +"\n";
		  output 		+= "numOfClusters:"+numOfClusters +"\n\n";
		  
		  String tempString="clustersPriors:";
		  for (int j = 0; j < clustersPriors.length; j++) 
			  tempString += clustersPriors[j]+",";
		  
		  output 		+= tempString.substring(0, tempString.length() - 1) +"\n\n";

		  for(int att = 0; att < numOfAttributes; ++att){
			  String[] paraName = {"mean:","std:","weightsSum:"};
			  for (int p=0;p<3;++p){
				  tempString = paraName[p];  
				  for(int c=0;c<numOfClusters;++c)
					  tempString += modelNormal[c][att][p]+",";
					  
				  output 		+= tempString.substring(0, tempString.length() - 1) +"\n";
			  }
		  }
		  UtilClass.WriteToFile(filename, output);
	  }

	  
	  public double loadModel(String file) throws NumberFormatException, IOException{
		  BufferedReader in =   new BufferedReader(new FileReader(new File(file)));
		  //read # of clusters
		  modelllk 			= Double.parseDouble(in.readLine().substring("llk:".length()).trim());
		  numOfInstances 	= Long.parseLong(in.readLine().substring("numOfInstances:".length()).trim());
		  numOfAttributes	= Integer.parseInt(in.readLine().substring("numOfAttributes:".length()).trim());
		  numOfClusters		= Integer.parseInt(in.readLine().substring("numOfClusters:".length()).trim());
		  in.readLine();
		  String line 		= in.readLine().substring("clustersPriors:".length()).trim();
		  String[] values	= line.split(",");
		  clustersPriors	= UtilClass.toDoubleArray(values);
		  in.readLine();
		  modelNormal = new double[numOfClusters][numOfAttributes][3];
		  for(int i = 0; i < numOfAttributes; ++i ){
			  loadParam(in.readLine(),0,i);
			  loadParam(in.readLine(),1,i);
			  loadParam(in.readLine(),2,i);
		  }
		  in.close();
		  return modelllk;
	  }
	  
	  public double clusterUpdate(Instance instance)throws Exception {
		  double [] dist =  distributionForInstance(instance);
		  double[] clustersSizes = getClustersSizes();
		  double[][] oldMeans = new double[numOfAttributes][numOfClusters];
		  ++numOfInstances;
		  //update clusters priors
		  for (int j = 0; j < numOfClusters; j++) {
			  clustersPriors[j] = (clustersSizes[j] + instance.getWeight() * dist[j])/numOfInstances;
		  }

		  // old sums of mean and std
		  for (int j = 0; j < numOfAttributes; j++) {
			  for (int i = 0; i < numOfClusters; i++) {
				  oldMeans[i][j] = modelNormal[i][j][0];
				  modelNormal[i][j][0] = modelNormal[i][j][0] * modelNormal[i][j][2] + (instance.get(j) * instance.getWeight() * dist[i]);
				  modelNormal[i][j][2] += instance.getWeight() * dist[i];
				  modelNormal[i][j][0] =  modelNormal[i][j][0] / modelNormal[i][j][2];
			  }
		  }
		  		
		  for (int j = 0; j < numOfAttributes; j++) {
			  for (int i = 0; i < numOfClusters; i++) {
				  double n2 = modelNormal[i][j][2] - 2* instance.getWeight() * dist[i];
				  double n1 = modelNormal[i][j][2] - instance.getWeight() * dist[i];
				  double meanDiff = oldMeans[i][j] - modelNormal[i][j][0];
				 modelNormal[i][j][1] = Math.sqrt((n2 * modelNormal[i][j][1] * modelNormal[i][j][1] + n1 * meanDiff * meanDiff) / n1);
			  }
		  }
		  
		  double instanceLLK  =  logDensityForInstance(instance);
		  modelllk = (modelllk * (numOfInstances - 1)+instanceLLK)/numOfInstances;
		  return instanceLLK;
	  }

	  public int clusterInstance(Instance instance, double logDensityForInstance[]) throws Exception {
	    double [] dist =  distributionForInstance(instance);
	    if (dist == null || dist.length==0) 
	    	return -1;
	    
	    logDensityForInstance[0] = logDensityForInstance(instance);
    
	    int maxIdx = UtilClass.maxIndex(dist);
	    return maxIdx;
	  }	  
	  
	  public double evaluateInstance(Instance instance, double logDensityForInstance[]) throws Exception {
		    double [] dist =  distributionForInstance(instance);
		    if (dist == null || dist.length==0) 
		    	return -1.0e10;
		    logDensityForInstance[0] = logDensityForInstance(instance);
		    
		    double llkRatio = (logDensityForInstance[0]-modelllk)/Math.abs(modelllk);
		    int maxIdx = UtilClass.maxIndex(dist);
		    double [][] cluster = modelNormal[maxIdx];
		    double[] instanceItems = instance.get();
		    double distance =0;
		    for(int i=0;i<instanceItems.length;++i){
		    	double diff = instanceItems[i]- cluster[i][0];
		    	distance += (diff * diff)/(cluster[i][1]);
		    }    		    
		    return (llkRatio * Math.sqrt(distance)/*/instanceItems.length*/);
	  }	  
	  
	  public double[][] getComponentParameters(int ComponentID){
		  if (ComponentID<modelNormal.length)
			  return modelNormal[ComponentID];
		  return null;
	  }
	  
	  public int getSeed() {
		return seed;
	  }

	  public void setSeed(int seed) {
		this.seed = seed;
	  }
	  
	  public double getModelLlk(){
		return modelllk;
	  }
	  
	  public void EM_Report() {
		    int i, j;
		    System.out.println("======================================");
		   	System.out.println("\nclustersPriors:");
		   	for (i=0;i<numOfClusters;++i)
		   		System.out.print("c"+i+"="+clustersPriors[i]+"\t");

		    for (i = 0; i < numOfAttributes; i++) {
		    	System.out.println("\n\nC"+(i+1));
		    	System.out.print("\nmean :  ");
		    	for (j = 0; j < numOfClusters; j++) 
		             System.out.print(UtilClass.round(modelNormal[j][i][0],  6)+"\t");
		    	
		    	System.out.print("\nstd :");
		    	for (j = 0; j < numOfClusters; j++) 
		             System.out.print(UtilClass.round(modelNormal[j][i][1],  6)+"\t");
		    	
		    	System.out.print("\nweightsSum :");
		    	for (j = 0; j < numOfClusters; j++) 
		             System.out.print(UtilClass.round(modelNormal[j][i][2],  6)+"\t");

		        
		    }
	}
}
