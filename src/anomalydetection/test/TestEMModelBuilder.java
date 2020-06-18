
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

public class TestEMModelBuilder {
	private DetectorMaster 	anomalousDetector;


    public TestEMModelBuilder(int port) throws Exception {
		try{
			anomalousDetector 	= new DetectorMaster(Setting.THREADPOOL_SIZE); 
			anomalousDetector.start(Setting.MAIN_ALGORITHM, Setting.NUM_OF_ATTRIBUTES);
		}catch(Exception e){
			System.out.println("Exception in StorageNode: "+e.getMessage());
		}

    }

     
    public void startPublish(String dir) throws Exception{
		WeatherData  weatherData  = new WeatherData();
		NetCDFReader netCDFReader =  new NetCDFReader(dir,weatherData);
		long l = 0;
		while (netCDFReader.nextWeatherData()){
			anomalousDetector.process("abcdebflmn", weatherData.getDataSample());
			if (l%100 == 0){
				System.out.println("sample #: "+l +"   : "+ weatherData.getGeohash()+": "+weatherData.toString());
			}
			if (l == 1000)
				break;
			++l;
		}
		System.out.println("Total:"+l);
    }      
    
         
   
    public synchronized  void sendCommand(double command) throws Exception{
    	anomalousDetector.process("abc", new DataSample(new double[]{command,-1,-1,-1,-1,-1,-1,-1,-1}));     	 
    }
 
      public static void main(String[] args) throws Exception{    		  
    	  
  	      TestEMModelBuilder client = new TestEMModelBuilder(2);
   	 	  client.startPublish("C:\\tmp\\data");
   	 	  client.sendCommand(Setting.START_TRAINING);
   	 	  
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
