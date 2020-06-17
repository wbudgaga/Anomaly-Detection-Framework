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

package anomalydetection.util;

import java.util.Random;

import anomalydetection.clustering.DataSample;
import anomalydetection.dataset.SpatialProperties;
import anomalydetection.dataset.TemporalProperties;

public class WeatherData {
	private final int		GEOHASH_LEN	= 12;
	private String			geohash;
	private double[] 		features;
	private final String[] 	featureSet = {	"Day_Number", "Day_Minutes", "Temperature_surface", "Pressure_surface", "Geopotential_height_surface",
											"Snow_cover_surface", "Snow_depth_surface", "Relative_humidity_height_above_ground",
											"Surface_wind_gust_surface"};


	private static Random randomGenerator = new Random(System.nanoTime());

	public String[] getFeatureList(){
    	return featureSet;
    }
        
    public void setSpatial(float latitude, float longitude) {
    	geohash	 		= GeoHash.encode(latitude, longitude, GEOHASH_LEN);
    }
 
    public void setFeaturesValues(double[] data){      	
     	features = data;
     }

    public double[] getFeaturesValues(){   	
    	return features;
     }

    public DataSample getDataSample(){
    	return new DataSample(geohash,getFeaturesValues());
    }
    

    public static int randomInt(int start, int end) {
        return randomGenerator.nextInt(end - start + 1) + start;
    }
    //just for testing
    public String toString(){
    	String s="";
		for(int i=0;i<featureSet.length;++i)
			s += features[i]+"   ";
			
		return s;
    }

	public String getGeohash() {
		return geohash;
	}

}
