package anomalydetection.clustering.kmean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;
import anomalydetection.clustering.Canopy;
import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.distance.Distance;
import anomalydetection.clustering.distance.Mahalanobis;
import anomalydetection.util.DecisionTableHashKey;
import anomalydetection.util.MatrixL;
import anomalydetection.util.UtilClass;

public class SimpleKMeanClusterer_maha implements KMeansClustering {

	private final double coverageThreshold = 0.001;
	protected boolean m_FastDistanceCalc = true;
	// ////////////////////////////////////////////////////////////
	public static final int CANOPY = 2;
	public static final int RANDOM = 0;
	protected int m_initializationMethod = CANOPY; // RANDOM;
	protected boolean m_speedUpDistanceCompWithCanopies = true;
	/** Canopies that each centroid falls into (determined by T1 radius) */
	protected List<long[]> m_centroidCanopyAssignments;

	/**
	 * Canopies that each training instance falls into (determined by T1 radius)
	 */
	protected List<long[]> m_dataPointCanopyAssignments;

	/** The canopy clusterer (if being used) */
	protected Canopy m_canopyClusters;

	/**
	 * The maximum number of candidate canopies to hold in memory at any one
	 * time (if using canopy clustering)
	 */
	protected int m_maxCanopyCandidates = 100;

	/**
	 * Prune low-density candidate canopies after every x instances have been
	 * seen (if using canopy clustering)
	 */
	protected int m_periodicPruningRate = 10000;

	/**
	 * The minimum cluster density (according to T2 distance) allowed. Used when
	 * periodically pruning candidate canopies (if using canopy clustering)
	 */
	protected double m_minClusterDensity = 2;

	/** The t2 radius to pass through to Canopy */
	protected double m_t2 = Canopy.DEFAULT_T2;

	/** The t1 radius to pass through to Canopy */
	protected double m_t1 = Canopy.DEFAULT_T1;

	// ///////////////////////////////////////////////////////////

	protected int numOfInstances;
	protected int numOfAttributes;
	protected int m_MaxIterations = 500;
	protected int numClusters;
	protected Instances clusterCentroids;
	protected Matrix[] clusterCovMatrices;
	protected Distance[] mahaDistance;
	protected int[] clusterSizes;
	protected double[][] ranges;
	protected long trainingStartTime;
	protected long trainingEndTime;
	/**
	 * Holds the squared errors for all clusters.
	 */
	protected double[] m_squaredErrors;

	/**
	 * Holds the initial start points, as supplied by the initialization method
	 * used
	 */
	protected Instances m_initialStartPoints;

	public SimpleKMeanClusterer_maha(int numOfclusters) {
		numClusters = numOfclusters;
	}

	public double getMinDist(Instance instance) throws Exception {
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < numClusters; i++) {
			double dist = mahaDistance[i].distance(instance.get(),
					clusterCentroids.instance(i).get(), ranges);
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}

	public Matrix getClusterCovMatrices(int idx) {
		return clusterCovMatrices[idx];
	}

	private int clusterProcessedInstance(Instance instance,	boolean updateErrors, boolean useFastDistCalc,long[] instanceCanopies, double[][] ranges) throws Exception {
		double minDist = Double.MAX_VALUE;
		int bestCluster = -1;
		for (int i = 0; i < numClusters; i++) {
			double dist;
			if (useFastDistCalc)
				dist = mahaDistance[i].distance(instance.get(),	clusterCentroids.instance(i).get(), minDist);
			else
				dist = mahaDistance[i].distance(instance.get(),clusterCentroids.instance(i).get());

			if (dist < minDist) {
				minDist = dist;
				bestCluster = i;
			}
		}
		if (updateErrors)
			m_squaredErrors[bestCluster] += minDist;

		return bestCluster;
	}

	public void updateClusterParam(Instance instance, double[] clusteringResults)
			throws Exception {
		int clusterNr = (int) clusteringResults[0];
		Instance centriod = clusterCentroids.instance(clusterNr);
		int n = clusterSizes[clusterNr] += 1;
		Matrix clusterCov = clusterCovMatrices[clusterNr];
		Instance oldCentriod = centriod.clone();

		double[] features = instance.get();
		for (int i = 0; i < features.length; ++i) {
			double oldMean = centriod.get(i);
			double newMean = (features[i] + (oldMean * (n - 1)))/ clusterSizes[clusterNr];
			centriod.set(i, newMean);
			// compute cov_ii
			double meanDiff = oldCentriod.get(i) - centriod.get(i);
			double valueMeanDiff = features[i] - centriod.get(i);
			double covII = ((n - 2) * clusterCov.get(i, i) + (n - 1)
					* meanDiff * meanDiff + valueMeanDiff * valueMeanDiff)
					/ (n - 1);
			clusterCov.set(i, i, covII);
		}
		// compute cov_ij that are above the diagonal
		for (int i = 0; i < features.length; ++i) {
			for (int j = i + 1; j < features.length; ++j) {
				double meanDiff1 = oldCentriod.get(i) - centriod.get(i);
				double meanDiff2 = oldCentriod.get(j) - centriod.get(j);
				double valueMeanDiff1 = features[i] - centriod.get(i);
				double valueMeanDiff2 = features[j] - centriod.get(j);
				double covIJ = ((n - 2) * clusterCov.get(i, j) + (n - 1)
						* meanDiff1 * meanDiff2 + valueMeanDiff1
						* valueMeanDiff2)
						/ (n - 1);
				clusterCov.set(i, j, covIJ);
				clusterCov.set(j, i, covIJ);

			}
		}
		mahaDistance[clusterNr].updateCovInv();
	}

	protected void canopyInit(Instances data) throws Exception {
		if (m_canopyClusters == null) {
			m_canopyClusters = new Canopy();
			m_canopyClusters.setNumClusters(numClusters);
			m_canopyClusters.setT2(m_t2);
			m_canopyClusters.setT1(m_t1);
			m_canopyClusters
					.setMaxNumCandidateCanopiesToHoldInMemory(m_maxCanopyCandidates);
			m_canopyClusters.setPeriodicPruningRate(m_periodicPruningRate);
			m_canopyClusters.setMinimumCanopyDensity(m_minClusterDensity);
			m_canopyClusters.buildClusterer(data);
		}
		clusterCentroids = m_canopyClusters.getCanopies();
	}

	protected double[] updateCentroid(int centroidIndex, Instances members,
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

	protected void updateCoverianceMatrix(int clusterIdx, Instances members, Instance clusterCentriod) throws Exception {
		Matrix covM = new Matrix(members.getNumOfAttribute(),members.getNumOfInstances());
		
		for (int j = 0; j < members.getNumOfAttribute(); j++) {
			Matrix m = new Matrix( members.attributeToDoubleArray(j),1);
			covM.setMatrix(new int[]{j},0,members.getNumOfInstances()-1,  m);
		}
		clusterCovMatrices[clusterIdx] = MatrixL.covMatrix(covM,clusterCentriod.get());
		mahaDistance[clusterIdx] = new Mahalanobis(clusterCovMatrices[clusterIdx]);
	}

	protected void initParam() {
		clusterCovMatrices = new Matrix[numClusters];
		mahaDistance = new Distance[numClusters];
		clusterSizes = new int[numClusters];
	}

	protected void updateCoverianceMatrices(Instances[] members,Instances clusterCentriod) throws Exception {
		initParam();
		for (int i = 0; i < numClusters; i++) {
			updateCoverianceMatrix(i, members[i], clusterCentriod.instance(i));
			clusterSizes[i] = members[i].getNumOfInstances();
		}
	}

	protected void updateCoverianceMatrices(Instances clusterCentriod)throws Exception {
		initParam();
		int len = clusterCentriod.getNumOfAttribute();
		for (int i = 0; i < numClusters; i++) {
			clusterCovMatrices[i] = Matrix.identity(len,len);
			clusterSizes[i] = 1;
			mahaDistance[i] = new Mahalanobis(clusterCovMatrices[i]);
		}
	}

	public void buildClusterer(Instances data) throws Exception {
		m_canopyClusters = null;
		int numOfIterations = 0;
		Instances instances = new Instances(data);
		numOfAttributes = data.getNumOfAttribute();
		numOfInstances = data.getNumOfInstances();
		ranges = data.getRanges();
		clusterCentroids = new Instances(instances, numClusters);
		int[] clusterAssignments = new int[instances.getNumOfInstances()];
		trainingStartTime = System.currentTimeMillis();
		int instIndex;
		HashMap<DecisionTableHashKey, Integer> initC = new HashMap<DecisionTableHashKey, Integer>();
		DecisionTableHashKey hk = null;

		Instances initInstances = instances;
		if (m_speedUpDistanceCompWithCanopies) {
			m_canopyClusters = new Canopy();
			m_canopyClusters.setNumClusters(numClusters);

			m_canopyClusters.setT2(m_t2);
			m_canopyClusters.setT1(m_t1);
			m_canopyClusters
					.setMaxNumCandidateCanopiesToHoldInMemory(m_maxCanopyCandidates);
			m_canopyClusters.setPeriodicPruningRate(m_periodicPruningRate);
			m_canopyClusters.setMinimumCanopyDensity(m_minClusterDensity);
			m_canopyClusters.buildClusterer(initInstances);
			// System.err.println(m_canopyClusters);
			m_centroidCanopyAssignments = new ArrayList<long[]>();
			m_dataPointCanopyAssignments = new ArrayList<long[]>();
		}
		if (m_initializationMethod == CANOPY) {
			canopyInit(initInstances);
			m_initialStartPoints = new Instances(m_canopyClusters.getCanopies());
		} else {
			// random
			for (int j = initInstances.getNumOfInstances() - 1; j >= 0; j--) {
				instIndex = UtilClass.randomInt(0, j);
				hk = new DecisionTableHashKey(
						initInstances.instance(instIndex),
						initInstances.getNumOfAttribute(), true);
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
		}
		if (m_speedUpDistanceCompWithCanopies) {
			// assign canopies to training data
			for (int i = 0; i < instances.getNumOfInstances(); i++) {
				m_dataPointCanopyAssignments.add(m_canopyClusters
						.assignCanopies(instances.instance(i)));
			}
		}

		numClusters = clusterCentroids.getNumOfInstances();

		// removing reference
		initInstances = null;

		int i;
		boolean converged = false;
		int emptyClusterCount;
		Instances[] tempI = new Instances[numClusters];
		m_squaredErrors = new double[numClusters];
		updateCoverianceMatrices(clusterCentroids);
		while (!converged) {
			if (m_speedUpDistanceCompWithCanopies) {
				// re-assign canopies to the current cluster centers
				m_centroidCanopyAssignments.clear();
				for (int kk = 0; kk < clusterCentroids.getNumOfInstances(); kk++) {
					m_centroidCanopyAssignments.add(m_canopyClusters
							.assignCanopies(clusterCentroids.instance(kk)));
				}
			}

			emptyClusterCount = 0;
			numOfIterations++;
			converged = true;

			for (i = 0; i < instances.getNumOfInstances(); i++) {
				Instance toCluster = instances.instance(i);
				int newC = clusterProcessedInstance(toCluster,false,true,m_speedUpDistanceCompWithCanopies ? m_dataPointCanopyAssignments.get(i) : null, instances.getRanges());
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
					updateCentroid(i, tempI[i], true, true);
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
		}// while

		trainingEndTime = System.currentTimeMillis();
		// calculate errors
		if (!m_FastDistanceCalc) {
			for (i = 0; i < instances.getNumOfInstances(); i++) {
				clusterProcessedInstance(instances.instance(i), true, false,
						null, instances.getRanges());
			}
		}

		updateCoverianceMatrices(tempI, clusterCentroids);
		data.cleanData();
	}

	public int[] getClustersSizes() {
		return clusterSizes;
	}

	public Instances getCentroids() {
		return clusterCentroids;
	}

	public double[] clusterInstance(Instance instance) throws Exception {
		double minDist = Double.MAX_VALUE;
		int bestCluster = -1;
		for (int i = 0; i < numClusters; i++) {
			double dist;
			dist = mahaDistance[i].distance(instance.get(), clusterCentroids.instance(i).get());

			if (dist < minDist) {
				minDist = dist;
				bestCluster = i;
			}
		}

		return new double[] { bestCluster, minDist };

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

	public double getSquaredError() {
		return UtilClass.sum(m_squaredErrors);
	}

	public double getSquaredError(int idx) {
		return m_squaredErrors[idx];
	}

	public void setSquaredError(int idx, double sq) {
		m_squaredErrors[idx] = sq;
	}

	private Matrix parseCov(BufferedReader in) throws IOException {
		Matrix m = new Matrix(numOfAttributes, numOfAttributes);
		String line;
		for (int i = 0; i < numOfAttributes; ++i) {
			String[] values = in.readLine().split(",");
			for (int j=0; j< values.length; ++j){
				m.set(i, j, Double.parseDouble(values[j]));
			}
		}
		return m;
	}

	public double loadModel(String file) throws Exception{
		  BufferedReader in =   new BufferedReader(new FileReader(new File(file)));
		  String line;
		  String[] valueList;
		  in.readLine(); //skip the first line containing start and end time for training
		  //read # of clusters
		  numOfInstances 	= Integer.parseInt(in.readLine().substring("numOfInstances:".length()).trim());
		  numOfAttributes	= Integer.parseInt(in.readLine().substring("numOfAttributes:".length()).trim());
		  numClusters		= Integer.parseInt(in.readLine().substring("numOfClusters:".length()).trim());
		  
		  in.readLine();	//skip empty line  
		  line 				= in.readLine().substring("clustersSizes:".length()).trim();
		  valueList			= line.split(",");
		  clusterSizes		= UtilClass.toIntArray(valueList);
		  m_squaredErrors = new double[numClusters];
		  in.readLine();	//skip squaredError line
		  in.readLine();	//skip empty line
		
		  clusterCovMatrices= new Matrix[numClusters];
		  mahaDistance 		=  new Distance[numClusters];
		  for(int i = 0; i < numClusters; ++i ){
			  in.readLine();	
			  in.readLine();	//skip cluster line
			  clusterCovMatrices[i] = parseCov(in);
			  mahaDistance[i] 		= new Mahalanobis(clusterCovMatrices[i]);
		  }
		  clusterCentroids 			= new Instances(numOfAttributes);
		  
		  double[][] dataSamples 	= new double[numClusters][numOfAttributes];
		  in.readLine();
		  in.readLine();
		  for(int att = 0; att < numOfAttributes; ++att ){
			  valueList = in.readLine().substring("mean:".length()).trim().split(",");
			  for (int c = 0; c < valueList.length; ++c ){
				  dataSamples [c][att] = Double.parseDouble(valueList[c]);
			  }
		  }
		  for(int i = 0; i < numClusters; ++i ){
			  clusterCentroids.add(new DataSample(dataSamples[i]));
		  }
		  
		  in.close();
		  return -1;
	 }

	public void saveModel(String filename) throws NumberFormatException,
			IOException {
		int NumClusters = getNumClusters();

		String output = "spentTime:" + trainingStartTime + ", "
				+ trainingEndTime + "\n";
		output += "numOfInstances:" + numOfInstances + "\n";
		output += "numOfAttributes:" + numOfAttributes + "\n";
		output += "numOfClusters:" + NumClusters + "\n\n";

		String tempString = "clustersSizes:";
		int[] clusterSizes = getClusterSizes();
		for (int j = 0; j < clusterSizes.length; j++)
			tempString += clusterSizes[j] + ",";
		output += tempString.substring(0, tempString.length() - 1) + "\n";

		tempString = "squaredError:";
		for (int j = 0; j < NumClusters; j++)
			tempString += getSquaredError(j) + ",";

		output += tempString.substring(0, tempString.length() - 1) + "\n\n";

		tempString = "coverianceMatrix:\n";
		for (int j = 0; j < NumClusters; j++) {
			Matrix m = clusterCovMatrices[j];
			tempString += "cluster " + j + ":\n";
			tempString += MatrixL.toString(m) + "\n";
		}

		output += tempString + "\n";

		for (int att = 0; att < numOfAttributes; ++att) {
			Instances centroids = getCentroids();
			tempString = "mean:";
			for (int c = 0; c < NumClusters; ++c)
				tempString += centroids.instance(c).get(att) + ",";
			output += tempString.substring(0, tempString.length() - 1) + "\n";
		}
		UtilClass.WriteToFile(filename, output);
	}

	/*
	 * public double loadModel(String file) throws NumberFormatException,
	 * IOException{ BufferedReader in = new BufferedReader(new FileReader(new
	 * File(file))); //read # of clusters modelllk =setS
	 * Double.parseDouble(in.readLine().substring("llk:".length()).trim());
	 * numOfInstances =
	 * Long.parseLong(in.readLine().substring("numOfInstances:".
	 * length()).trim()); numOfAttributes =
	 * Integer.parseInt(in.readLine().substring
	 * ("numOfAttributes:".length()).trim()); numOfClusters =
	 * Integer.parseInt(in
	 * .readLine().substring("numOfClusters:".length()).trim()); in.readLine();
	 * String line = in.readLine().substring("clustersPriors:".length()).trim();
	 * String[] values = line.split(","); clustersPriors =
	 * StringConverter.toDoubleArray(values); in.readLine(); modelNormal = new
	 * double[numOfClusters][numOfAttributes][3]; for(int i = 0; i <
	 * numOfAttributes; ++i ){ loadParam(in.readLine(),0,i);
	 * loadParam(in.readLine(),1,i); loadParam(in.readLine(),2,i); } in.close();
	 * return modelllk; }
	 */
}
