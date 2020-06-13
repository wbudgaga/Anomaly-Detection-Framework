package anomalydetection.clustering.kmean;

import java.util.Random;

import anomalydetection.clustering.Instances;
import anomalydetection.util.UtilClass;

public class CrossValidation {
	private KMeansClustering 	clusterer;
	private int 				numOfFolders 	= 10;
	private int 				numOfclusters 	= 2;	
	private int			 		seed 			= 1;
	private Random 				randomGenerator = new Random(seed);
	
	public CrossValidation(KMeansClustering clusterer){
		this.clusterer = clusterer;
	}
	
	public double clusterInstances(Instances testData) throws Exception{
		double dist = 0;
		for (int i=0; i<testData.getNumOfInstances();++i){
			dist += clusterer.getMinDist(testData.instance(i));
		}
		return dist/testData.getNumOfInstances();
	}
	
	public int CVClusters(Instances trainingInstances) throws Exception {
		  double 	previousDist 			= Double.MAX_VALUE;	
		  double 	templl;
		  int	 	i;
		  Random 	random;
		  Instances trainCopy;
	
		  do{	
			 random 		= new Random(seed);
		     trainCopy 	= new Instances(trainingInstances);
		     templl 		= 0.0;
		     for (i = 0; i < numOfFolders; i++) {
		    	 Instances cvTrain = trainCopy.trainCV(numOfFolders, i, random);
		    	 if (numOfclusters > cvTrain.getNumOfInstances()) 
		    		 throw new Exception("The number of clusters is bigger than the number of instances");
		    	 
		    	 clusterer.setNumClusters(numOfclusters);
		    	 Instances cvTest 	= trainCopy.testCV(numOfFolders, i);
		    	 randomGenerator 	= new Random(seed);
	
		    	 UtilClass.throwAwayRandomNumbers(randomGenerator);
		    	 clusterer.buildClusterer(cvTrain);
		    	 templl +=clusterInstances(cvTest);
		     }//for
		     
		     templl /= numOfFolders;
	    	 if (templl < previousDist) {
	    		 previousDist = templl;
	    		 numOfclusters++;
	    	 }else
	    		 break;
	    	 
		  }while (true) ;
		  return numOfclusters - 1;
	}
}
