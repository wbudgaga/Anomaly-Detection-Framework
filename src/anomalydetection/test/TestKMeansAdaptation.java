
package anomalydetection.test;

import java.io.IOException;
import java.net.Socket;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.Setting;
import anomalydetection.communiction.ConnectionManager;
import anomalydetection.handlers.MessageHandler;
import anomalydetection.util.NetCDFReader;
import anomalydetection.util.UtilClass;
import anomalydetection.util.WeatherData;
import anomalydetection.wireformates.DataMessage;

public class TestKMeansAdaptation {
	private DetectorMaster 	anomalousDetector;


    public TestKMeansAdaptation(int port) throws Exception {
		try{
			anomalousDetector 	= new DetectorMaster(10); 
			anomalousDetector.start(Setting.MAIN_ALGORITHM, Setting.NUM_OF_ATTRIBUTES);
		}catch(Exception e){
			System.out.println("Exception in StorageNode: "+e.getMessage());
		}

    }

    public void addSmallRatio(DataSample ds){
   
    	double[] f = ds.get();
    	for (int i=0; i<f.length;++i){
    		double r = UtilClass.randomInt(1, 10)/1000.0;
    		ds.set(i, f[i]+r);
    	}
    }
    
    public void startPublish(String dir) throws Exception{
		WeatherData  weatherData  = new WeatherData();
		NetCDFReader netCDFReader =  new NetCDFReader(dir,weatherData);
		long l = 0;
		while (netCDFReader.nextWeatherData()){
			DataSample ds = weatherData.getDataSample();
			addSmallRatio(ds);
			anomalousDetector.process("abcdebflmn", ds);
			if (l%100 == 0){
				System.out.println("sample #: "+l +"   : "+ weatherData.getGeohash()+": "+weatherData.toString());
			}
			if (l == 10000)
				break;
			++l;
		}
		System.out.println("Total:"+l);
    }      
    
   
    public synchronized  void clusterData(double command,int n) throws Exception{
    	for (int i=0;i<n;++i)
    		anomalousDetector.process("8zn0qnb752", new DataSample(new double[]{366.0,1080.0,200.3518981933594,101298.0,  0.0700225830078125 , 0.0 , 0.0 , 90.0 ,11.76}));     	 
    }

   
    public synchronized  void sendCommand(double command) throws Exception{
    	anomalousDetector.process("abc", new DataSample(new double[]{command,-1,-1,-1,-1,-1,-1,-1,-1}));     	 
    }
 
      public static void main(String[] args) throws Exception{    		  
    	  
  	      TestKMeansAdaptation client = new TestKMeansAdaptation(2);
  	      Thread.sleep(10000);
  	      client.clusterData(-2.0,205);
   	 	  //client.startPublish("C:\\tmp\\data");
   	 	  //client.sendCommand(Setting.START_TRAINING);
   	 	  
   		  //client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2010");
  // 		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2006");
   	//	client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2007");
  /* 		Thread.sleep(10000);
   		 client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2006");
   		Thread.sleep(10000);
*/    	//	 client.startPublish("/tmp/noaa");
   		//Thread.sleep(10000);
   	//	 client.sendCommand(Setting.START_TRAINING);
    	 System.out.println("done!");
    }
}
