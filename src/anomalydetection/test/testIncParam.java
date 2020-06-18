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
import anomalydetection.util.StatisticUtil;
import anomalydetection.util.UtilClass;

public class testIncParam {
	private double mean1;
	private double var1;
	private double mean2;
	private double var2;
	private double cov;
	private int 	n;
	
	
	public  testIncParam(double[] att1, double[] att2) {
		n 		= att1.length;
		mean1 	= UtilClass.mean(att1);
		var1 	= StatisticUtil.var(att1, mean1);
		mean2 	= UtilClass.mean(att2);
		var2 	= StatisticUtil.var(att2, mean2);
		cov 	= MatrixL.cov(att1, att2);
	}

/*	public void update(double v){
		++n;
		double oldMean = mean ;
		mean = (v+(oldMean * (n-1)))/n;		
		var  = ((n-2)*var + (n-1)*(oldMean-mean) *(oldMean-mean) +(v-mean)*(v-mean)) /(n-1)  ;		
	}
*/	
	public void update(double v1, double v2){
		++n;
		double oldMean1 = mean1 ;
		double oldMean2 = mean2 ;
		mean1 = (v1+(oldMean1 * (n-1)))/n;
		mean2 = (v2+(oldMean2 * (n-1)))/n;		
		
		var1  = ((n-2)*var1 + (n-1)*(oldMean1-mean1) *(oldMean1-mean1) +(v1-mean1)*(v1-mean1)) /(n-1)  ;
		var2  = ((n-2)*var2 + (n-1)*(oldMean2-mean2) *(oldMean2-mean2) +(v2-mean2)*(v2-mean2)) /(n-1)  ;
		
		cov  = ((n-2)*cov + (n-1)*(oldMean1-mean1) *(oldMean2-mean2) +(v1-mean1)*(v2-mean2)) /(n-1)  ;
		
	}


	public void print(){
		System.out.println("n="+n+"\tmean1="+mean1+"\tvar1="+var1+"\tmean2="+mean2+"\tvar2="+var2+"\tcov"+cov);
	}
	public static void main(String[] args) throws Exception {
		double[] x = new double[]{1,2,3,4,5};
		double[] y = new double[]{0.1,2.3,1.3,2.4,2};
		double[] x1 = new double[]{1,2,3,4,5,2};
		double[] y1 = new double[]{0.1,2.3,1.3,2.4,2,1};
		
		testIncParam t = new testIncParam(x,y);
		t.print();
		t.update(2,1);
/*		t.update(2);
		t.update(3);
		t.update(1);
*/		testIncParam t1 = new testIncParam(x1,y1);
		t.print();
		t1.print();
	}
}
