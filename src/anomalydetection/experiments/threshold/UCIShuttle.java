

package anomalydetection.experiments.threshold;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.kmean.SimpleKMeanClusterer_maha;
import anomalydetection.util.UtilClass;

public class UCIShuttle  {
    public static Instances 	trainingData = new Instances(8);
    public static Instances 	testList = new Instances(8);

    
 	public static void getList(int c) throws IOException{  
		BufferedReader in =   new BufferedReader(new FileReader(new File("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\benchmart\\anomalyDetection\\uci\\input\\shuttle.trn")));
		 String line;
		 double d[]  = new double[8];
		 
		 while (( line = in.readLine()) != null){
			 String[] s = line.split(",");
			 for(int i=0;i<s.length-2;++i)
				 d[i]= Double.parseDouble(s[i]);
			 DataSample sample = new DataSample(d);
			 sample.setID(s[9]);
			 if (sample.getID().compareTo("1")==0)
				 trainingData.add(sample);
			 else
				 testList.add(sample);
		 }
		 
		 for (int i = 0 ; i<2400;++i){
			 //int n = UtilClass.randomInt(0, trainingData.getNumOfInstances()-1);
			 testList.add(trainingData.instance(i));
		 }

		 in.close();
		System.out.println("initialList:"+trainingData.getNumOfInstances());
	
		System.out.println("testList:"+testList.getNumOfInstances()+"   id="+testList.instance(10).toString());
	}

	public static void buildAndCluster() throws Exception{
		UtilClass.fileAsOutputDst("C:\\tmp\\shuttle.txt");
	   	
	   	 getList(0);
	   	 
			SimpleKMeanClusterer_maha sm = new SimpleKMeanClusterer_maha(2);
			//sm.loadModel("c:\\tmp\\uci_shuttleModel.txt");
			double y= System.currentTimeMillis();
			sm.buildClusterer(trainingData);
			sm.saveModel("c:\\tmp\\uci_shuttleModel.txt");
			System.out.println("t=     "+(System.currentTimeMillis() - y));
			int i = 0;

			 while (i<testList.getNumOfInstances()){
				 Instance sample = testList.instance(i++);
				 double[] c =  sm.clusterInstance(sample);		
				 
	   			 System.out.println(sample.getID()+"\t"+c[0]+ "\t"+Math.sqrt(c[1]));
			}

				 /////////////////
				 
		      	 System.out.println("done!");

	}
	
	
     public static void main(String[] args) throws Exception{
    	 buildAndCluster();
    	 /*getList1();
    	 for(int i=40;i>-50;--i)
    	 checkResults(i);
    	 */
    }
}
