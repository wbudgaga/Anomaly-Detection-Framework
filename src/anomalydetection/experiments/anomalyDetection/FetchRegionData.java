
package anomalydetection.experiments.anomalyDetection;

import anomalydetection.clustering.DataSample;
import anomalydetection.util.NetCDFReader;
import anomalydetection.util.UtilClass;
import anomalydetection.util.WeatherData;

public class FetchRegionData {
    public void startPublish(String dir) throws Exception{
		WeatherData  weatherData  = new WeatherData();
		NetCDFReader netCDFReader =  new NetCDFReader(dir,weatherData);
		
		while (netCDFReader.nextWeatherData()){
			DataSample ds = weatherData.getDataSample();
			String geohash= ds.getID();
			if (geohash.startsWith("9wwh")==false)
				continue;
			System.out.println(weatherData.getGeohash()+": "+weatherData.toString());
		}
    }      
     
    public static void main(String[] args) throws Exception{    		  
    	FetchRegionData client = new FetchRegionData();
    	String y = args[0]; 
    	UtilClass.fileAsOutputDst("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/P_9wwh_"+y+".txt");
    	
//   	 	client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2005");
		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/20"+y);
/*		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2007");
		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2008");
		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2009");
*/    	System.out.println("done!");
    }
}
