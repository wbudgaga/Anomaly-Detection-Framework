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

import galileo.clustering.DataSample;
import galileo.clustering.Instance;
import galileo.clustering.Instances;
import galileo.clustering.em.EMClusterer;
import galileo.util.FileNames;
import galileo.util.UtilClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Target  {
    public static Instances 	initialList = new Instances(2);
    public static Instances 	testList = new Instances(2);
    public static Instances 	testResults = new Instances(2);
    protected int a;
    
	public static void getList1() throws IOException{  
		BufferedReader in =   new BufferedReader(new FileReader(new File("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\exp\\data\\clusteringData\\target\\targetClustering.txt")));
		 String line = in.readLine();
		 double d[]  = new double[2];
		 while (( line = in.readLine()) != null){
			 String[] s = line.split(",");
			 for(int i=0;i<s.length;++i)
				 d[i]= Double.parseDouble(s[i]);
			 DataSample sample = new DataSample(d);
			 testResults.add(sample);			
		 }
		 in.close();
	}

    public static void checkResults(double th){
    	int i = 0;
    	int awrong=0, acorrect =0;
    	int wrong=0, correct =0;
    	while (i < testResults.getNumOfInstances()){
    		Instance sample =  testResults.instance(i++);
    		if (sample.get(0)==4){
    			if (sample.get(1)<th)
    				++acorrect;
    			else 
    				++awrong;
    		}else{
       			if (sample.get(1)<th)
    				++wrong;
    			else 
    				++correct;
    		}
    	}
    	System.out.println(th+":  acorrect:"+acorrect+"  awrong:"+awrong+"    correct:"+correct+"   wrong"+wrong);
    }
    
	public static void getList(int c) throws IOException{  
		BufferedReader in =   new BufferedReader(new FileReader(new File("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\exp\\data\\clusteringData\\target\\target.lrn")));
		BufferedReader in2 =   new BufferedReader(new FileReader(new File("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\exp\\data\\clusteringData\\target\\target.cls")));
		 String line;
		 double d[]  = new double[2];
		 in.readLine(); in2.readLine();
		 while (( line = in.readLine()) != null){
			 String[] s = line.split(",");
			 for(int i=1;i<s.length;++i)
				 d[i-1]= Double.parseDouble(s[i]);
			 DataSample sample = new DataSample(d);
			 
			 sample.setID(in2.readLine().split(",")[1]);
			 if (sample.getID().compareTo("1")==0 || sample.getID().compareTo("2")==0)
				 initialList.add(sample);
//			 else{
	//			 testList.add(sample);
		//	 }		 , 
		 }
		 d[0]=0; d[1]=-1.0;
		 testList.add(new DataSample(d));
		 in.close();
		System.out.println("initialList:"+initialList.getNumOfInstances());
	
	//	System.out.println("testList:"+testList.getNumOfInstances()+"   id="+testList.instance(10).toString());
	}

	public static void buildAndCluster() throws Exception{
	   //	 FileNames.fileAsOutputDst("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\exp\\data\\clusteringData\\target\\targetClustering.txt");
		 
	   	 EMClusterer clusterer = new EMClusterer("kmean");
	   	 getList(0);
	   	 double llk = clusterer.buildClusterer(initialList);
	   	 System.out.println("llk:"+llk);
	   	 clusterer.EM_Report();
	   	 int i = 0;
/*			 while (i<onlineList.getNumOfInstances()){
		    		 double llk1 = clusterer.clusterAndUpdate(onlineList.instance(i++),llk);				
					 llk = llk1;
			}
			System.out.println("# of samples #: "+i+",  LLK: "+ llk);
			clusterer.EM_Report();
*/	
	  	     i = 0;
	  	     double [] logDensityForInstance={0};
			 while (i<testList.getNumOfInstances()){
				 Instance sample = testList.instance(i++);
				 double c =  clusterer.evaluateInstance(sample, logDensityForInstance);				
				System.out.println(sample.toString()+"\n==>"+c+"++>"+logDensityForInstance[0]);
			}
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
