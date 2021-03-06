/*
Copyright (c) 2013, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package anomalydetection.experiments.threshold;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Instance;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.em.EMClusterer;
import anomalydetection.clustering.kmean.SimpleKMeanClusterer_maha;
import anomalydetection.util.UtilClass;

public class UCIArrhythmia  {
    public static final int		trainingSize	= 400000000;
    public static long			count 			= 0;
    public static Instances 	trainingData = new Instances(279);
    public static Instances 	onlineList = new Instances(279);
    public static Instances 	testList = new Instances(279);
    public static Instances 	testResults = new Instances(2);

    
	public static void getList(int c) throws IOException{  
		BufferedReader in =   new BufferedReader(new FileReader(new File("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\benchmart\\anomalyDetection\\uci\\input\\arrhythmia.txt")));
		 String line = in.readLine();
		 double d[]  = new double[279];
		 
		 while (( line = in.readLine()) != null){
			 String[] s = line.split(",");
			 for(int i=0;i<s.length-1;++i)
				 d[i]= Double.parseDouble(s[i]);
			 DataSample sample = new DataSample(d);
			 sample.setID(s[279]);
			 if (sample.getID().compareTo("1")==0 )
				 trainingData.add(sample);
			 else{
				 testList.add(sample);
			 }			
		 }
		 
		 for (int i = 0 ; i<20;++i){
			 //int n = UtilClass.randomInt(0, trainingData.getNumOfInstances()-1);
			 testList.add(trainingData.instance(i));
		 }

		 in.close();
		System.out.println("trainingData:"+trainingData.getNumOfInstances());
		System.out.println("onlineList:"+onlineList.getNumOfInstances());
		System.out.println("testList:"+testList.getNumOfInstances()+"   id="+testList.instance(10).toString());
	}

	public static void buildAndCluster() throws Exception{
	   	 UtilClass.fileAsOutputDst("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\benchmart\\anomalyDetection\\uci\\output\\arrhythmiaClusteringCombinedRD.txt");
		 
	   	 EMClusterer clusterer = new EMClusterer("kmean");
	   	 getList(0);
	   	 double llk = clusterer.buildClusterer(trainingData);
	   	 System.out.println("llk:"+llk);
	   	 clusterer.EM_Report();
	   	 int i = 0;

  	     double [] logDensityForInstance={0};
		 while (i<testList.getNumOfInstances()){
			 Instance sample = testList.instance(i++);
			 
			 double c =  clusterer.evaluateInstance(sample, logDensityForInstance);		
			 
   			 System.out.println(sample.getID()+"\t"+logDensityForInstance[0]+ "\t"+c);
		}

			 /////////////////
			 
	      	 System.out.println("done!");

	}
	
	public static void buildAndClusterKMeans() throws Exception{
		UtilClass.fileAsOutputDst("C:\\tmp\\uciArrhythmia.txt");  	
	   	 getList(0);
	   	 
			SimpleKMeanClusterer_maha sm = new SimpleKMeanClusterer_maha(1);
			//sm.loadModel("c:\\tmp\\uci_shuttleModel.txt");
			sm.buildClusterer(trainingData);
			sm.saveModel("c:\\tmp\\uci_uciArrhythmiaModel.txt");
			int i = 0;

			 while (i<testList.getNumOfInstances()){
				 Instance sample = testList.instance(i++);
				 double[] c =  sm.clusterInstance(sample);		
				 
	   			 System.out.println(sample.getID()+"\t"+c[0]+ "\t"+Math.sqrt(c[1]));
			}
	      	 System.out.println("done!");
	}
	
     public static void main(String[] args) throws Exception{
    	 buildAndClusterKMeans();
    	 /*getList1();
    	 for(int i=40;i>-50;--i)
    	 checkResults(i);
    	 */
    }
}
