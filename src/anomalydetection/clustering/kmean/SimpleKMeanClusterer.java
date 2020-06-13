package anomalydetection.clustering.kmean;

import java.io.IOException;
import java.util.HashMap;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Instances;
import anomalydetection.util.DecisionTableHashKey;
import anomalydetection.util.UtilClass;

public class SimpleKMeanClusterer {
	
	private final double 		coverageThreshold 	= 0.001;
	
	protected int  		numOfInstances;
	protected int  		numOfAttributes;
    protected int 		m_MaxIterations = 500;
    protected int 		numClusters;
    protected Instances clusterCentroids;
    protected Instances clusterStdDevs;
    protected int[] 	clusterSizes;
    protected double[][]ranges;
    protected long 		trainingStartTime;
    protected long 		trainingEndTime;
    /**
     * Holds the squared errors for all clusters.
     */
    protected double[] m_squaredErrors;


    /**
     * Holds the initial start points, as supplied by the initialization method
     * used
     */
    protected Instances m_initialStartPoints;
	
	public SimpleKMeanClusterer(int numOfclusters) {
		numClusters = numOfclusters;
	}
	
	private int clusterProcessedInstance(Instance instance, boolean updateErrors, double[][] ranges) {
		double minDist = Double.MAX_VALUE;
		int bestCluster = -1;
		for (int i = 0; i < numClusters; i++) {
			double dist = clusterCentroids.instance(i).distance(instance, ranges);
			if (dist < minDist) {
				minDist = dist;
			    bestCluster = i;
			}
		}

		if (updateErrors) {
			// Euclidean distance to Squared Euclidean distance
			minDist *= minDist * instance.getWeight();
			m_squaredErrors[bestCluster] += minDist;
		}
		return bestCluster;
	}

	protected double[] moveCentroid(int centroidIndex, Instances members,
		boolean updateClusterInfo, boolean addToCentroidInstances) {
		double[] vals = new double[members.getNumOfAttribute()];

	    for (int j = 0; j < members.getNumOfAttribute(); j++) {
	    	vals[j] = UtilClass.mean(members.attributeToDoubleArray(j));
	    }
	    if (addToCentroidInstances) {
	    	clusterCentroids.add(new DataSample(vals));
	    }
	    return vals;
	}
	
	public void buildClusterer(Instances data) throws Exception{
		int numOfIterations 		= 0;
	    Instances instances 		= new Instances(data);
	    numOfAttributes 			= data.getNumOfAttribute();
	    numOfInstances 				= data.getNumOfInstances();
	    ranges						= data.getRanges();
	    clusterCentroids 			= new Instances(instances, numClusters);
	    int[] clusterAssignments 	= new int[instances.getNumOfInstances()];
	    trainingStartTime = System.currentTimeMillis();
	    int instIndex;
	    HashMap<DecisionTableHashKey, Integer> initC =  new HashMap<DecisionTableHashKey, Integer>();
	    DecisionTableHashKey hk = null;

	    Instances initInstances = instances;
	    // random
	    for (int j = initInstances.getNumOfInstances() - 1; j >= 0; j--) {
	    	instIndex = UtilClass.randomInt(0, j );
	        hk = new DecisionTableHashKey(initInstances.instance(instIndex),initInstances.getNumOfAttribute(), true);
	        if (!initC.containsKey(hk)) {
	        	clusterCentroids.add(initInstances.instance(instIndex));
	        	initC.put(hk, null);
	        }
	        initInstances.swap(j, instIndex);
	        if (clusterCentroids.getNumOfInstances() == numClusters) {
	        	break;
	        }
	    }

	    m_initialStartPoints = new Instances(clusterCentroids);

	    numClusters = clusterCentroids.getNumOfInstances();

	    // removing reference
	    initInstances = null;

	    int i;
	    boolean converged = false;
	    int emptyClusterCount;
	    Instances[] tempI = new Instances[numClusters];
	    m_squaredErrors = new double[numClusters];

	    while (!converged) {
	    	emptyClusterCount = 0;
	    	numOfIterations++;
	    	converged = true;

	        for (i = 0; i < instances.getNumOfInstances(); i++) {
	        	Instance toCluster = instances.instance(i);
	        	int newC = clusterProcessedInstance(toCluster, false,instances.getRanges() );
	        	if (newC != clusterAssignments[i]) {
	        		converged = false;
	        	}
	        	clusterAssignments[i] = newC;
	        }

	        // update centroids
	        clusterCentroids = new Instances(instances, numClusters);
	        for (i = 0; i < numClusters; i++) {
	        	tempI[i] = new Instances(instances, 0);
	        }
	        for (i = 0; i < instances.getNumOfInstances(); i++) {
	        	tempI[clusterAssignments[i]].add(instances.instance(i));
	        }
	        for (i = 0; i < numClusters; i++) {
	        	if (tempI[i].getNumOfInstances() == 0) {
	        		// empty cluster
	        		emptyClusterCount++;
	        	} else {
	        		moveCentroid(i, tempI[i], true, true);
	        	}
	        }

	        if (numOfIterations == m_MaxIterations) {
	        	converged = true;
	        }

	        if (emptyClusterCount > 0) {
	        	numClusters -= emptyClusterCount;
	        	if (converged) {
	        		Instances[] t = new Instances[numClusters];
	        		int index = 0;
	        		for (int k = 0; k < tempI.length; k++) {
	        			if (tempI[k].getNumOfInstances() > 0) {
	        				t[index] = tempI[k];
	        				index++;
	        			}
	        		}
	        		tempI = t;
	        	} else {
	        		tempI = new Instances[numClusters];
	        	}
	        }
	    }//while

	    trainingEndTime = System.currentTimeMillis();
	    for (i = 0; i < instances.getNumOfInstances(); i++) {
	        clusterProcessedInstance(instances.instance(i), true, instances.getRanges() );
	    }
	    clusterStdDevs = new Instances(instances, numClusters);
	    clusterSizes = new int[numClusters];
	    for (i = 0; i < numClusters; i++) {
	    	double[] vals2 = new double[instances.getNumOfAttribute()];
	    	for (int j = 0; j < instances.getNumOfAttribute(); j++) {
	    		double[] clusterInstances= tempI[i].attributeToDoubleArray(j);
	    		vals2[j] = UtilClass.std(clusterInstances, UtilClass.mean(clusterInstances)); //coveriance insead of std
	    	}
	    	clusterStdDevs.add(new DataSample(vals2));
	    	clusterSizes[i] = tempI[i].getNumOfInstances();
	    }
	    //data.cleanData();
	}
	

	public int[] getClustersSizes(){
		return clusterSizes;
	}
	
	
	public Instances  getCentroids(){
		return clusterCentroids;
	}

	
	
	public int clusterInstance(Instance instance){
		return  clusterProcessedInstance(instance, false,ranges );
	}

	
	public int getNumClusters() {
		return numClusters;
	}

	public void setNumClusters(int m_NumClusters) {
		this.numClusters = m_NumClusters;
	}

	public int[] getClusterSizes() {
		return clusterSizes;
	}

	
	public Instances getClusterStdDevs() {
		return clusterStdDevs;
	}

	public void setClusterStdDevs(Instances clusterStdDevs) {
		this.clusterStdDevs = clusterStdDevs;
	}

	public double getSquaredError() {
	    return UtilClass.sum(m_squaredErrors);
	}

	public double getSquaredError(int idx) {
	    return m_squaredErrors[idx];
	}
	public void setSquaredError(int idx, double sq) {
	    m_squaredErrors[idx] = sq;
	} 

	  public void saveModel(String filename) throws NumberFormatException, IOException{
		  int NumClusters 		= getNumClusters();
		  
		  String output = "spentTime:"+trainingStartTime +", "+ trainingEndTime+"\n";
		  output 		+= "numOfInstances:"+numOfInstances +"\n";
		  output 		+= "numOfAttributes:"+numOfAttributes +"\n";
		  output 		+= "numOfClusters:"+NumClusters +"\n\n";
		  
		  String tempString="clustersSizes:";
		  int[] clusterSizes = getClusterSizes();
		  for (int j = 0; j < clusterSizes.length; j++) 
			  tempString += clusterSizes[j]+",";
		  output 		+= tempString.substring(0, tempString.length() - 1) +"\n";
		  
		  tempString="squaredError:";
		  for (int j = 0; j < NumClusters; j++) 
			  tempString += getSquaredError(j)+",";
		  
		  output 		+= tempString.substring(0, tempString.length() - 1) +"\n\n";

		  
		  for(int att = 0; att < numOfAttributes; ++att){
			  Instances centroids = getCentroids();
			  Instances stdv = getClusterStdDevs();
			  tempString = "mean:";  
			  for(int c=0;c<NumClusters;++c)
				  tempString += centroids.instance(c).get(att)+",";
			  output 		+= tempString.substring(0, tempString.length() - 1) +"\n";
			  tempString = "std:";  
			  for(int c=0;c<NumClusters;++c)
				  tempString += stdv.instance(c).get(att)+",";
					  
			  output 		+= tempString.substring(0, tempString.length() - 1) +"\n";
			  
		  }
		  UtilClass.WriteToFile(filename, output);
	  }

/*		  
		  public double loadModel(String file) throws NumberFormatException, IOException{
			  BufferedReader in =   new BufferedReader(new FileReader(new File(file)));
			  //read # of clusters
			  modelllk 			=setS Double.parseDouble(in.readLine().substring("llk:".length()).trim());
			  numOfInstances 	= Long.parseLong(in.readLine().substring("numOfInstances:".length()).trim());
			  numOfAttributes	= Integer.parseInt(in.readLine().substring("numOfAttributes:".length()).trim());
			  numOfClusters		= Integer.parseInt(in.readLine().substring("numOfClusters:".length()).trim());
			  in.readLine();
			  String line 		= in.readLine().substring("clustersPriors:".length()).trim();
			  String[] values	= line.split(",");
			  clustersPriors	= StringConverter.toDoubleArray(values);
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
		  
*/
}
