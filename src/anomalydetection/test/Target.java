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

package anomalydetection.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Instances;
import anomalydetection.clustering.kmean.CrossValidation;
import anomalydetection.clustering.kmean.SimpleKMeanClusterer_maha;
import anomalydetection.util.MatrixL;

public class Target {
	public static void test(SimpleKMeanClusterer_maha sm, Instances ins) throws Exception {
		sm.buildClusterer(ins);
		sm.saveModel("c:\\tmp\\testModel.txt");
	}

/*	public static void clusterInstance(SimpleKMeanClusterer_maha sm, Instance in)throws Exception {
		int clusterID = sm.clusterInstance(in);
		System.out.println("clustering instance(" + in.get(0) + ","+ in.get(1) + ")");
		for (int i = 0; i < in.numAttributes(); ++i) {
			double nDist = (in.value(i) - assignedCluseterCentroid.value(i))
					/ assignedCluseterSTD.value(i);
			if (nDist >= 1) {
				System.out.println("abnormal");
				return;
			}
		}
		System.out.println("normal");
	}
*/
	
	public static Instances getList(int c) throws IOException{  
		BufferedReader in =   new BufferedReader(new FileReader(new File("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\exp\\data\\clusteringData\\target\\target1.txt")));
		 String line;
		 Instances list = new Instances(2);
		 double d[]  = new double[2];
		 in.readLine();
		 while (( line = in.readLine()) != null){
			 String[] s = line.split(",");
			 for(int i=0;i<s.length;++i)
				 d[i]= Double.parseDouble(s[i]);
			 DataSample sample = new DataSample(d);
			 list.add(sample);
		 }
		 in.close();
		System.out.println("initialList:"+list.getNumOfInstances());

		return list;
	}
	
	public double px(double[] rslt, double dist){
		//Math.exp(-0.5 *d);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		Instances ins = getList(0);
		SimpleKMeanClusterer_maha sm = new SimpleKMeanClusterer_maha(4);
		//CrossValidation cv = new CrossValidation(sm);
		//System.out.println(cv.CVClusters(ins));
		//test(sm, ins);
		DataSample DS = new DataSample(new double[]{0.5,0.5});
		sm.loadModel("c:\\tmp\\testModel.txt");
		double[] rslt = sm.clusterInstance(DS);
		MatrixL m = sm.getClusterCovMatrices((int)rslt[0]);
		double det = MatrixL.det(m);
		
		double m_normConst = (1.0/Math.sqrt(Math.pow(2 * Math.PI,9.0)*det))*Math.exp(-0.5 *rslt[1]);
				
		
		System.out.println(rslt[0]+"   "+Math.sqrt(rslt[1])+"===>"+m_normConst);
		/*
		 * for (int i=2; i<12;++i){ sm.setNumClusters(i);
		 * sm.buildClusterer(ins); Instances c = sm.getClusterSizes();
		 * System.out.println(i+"    ===>  "+sm.getSquaredError()); }
		 */
		// sm.clusterInstance(instance);
		/*
		 * getList1(); for(int i=40;i>-50;--i) checkResults(i);
		 */
	}
}
