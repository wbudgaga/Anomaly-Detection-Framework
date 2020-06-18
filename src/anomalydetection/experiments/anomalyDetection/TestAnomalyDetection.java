
package anomalydetection.experiments.anomalyDetection;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.Setting;
import anomalydetection.util.NetCDFReader;
import anomalydetection.util.UtilClass;
import anomalydetection.util.WeatherData;

public class TestAnomalyDetection {
	private DetectorMaster 	anomalousDetector;
	public  long l = 0;

    public TestAnomalyDetection() throws Exception {
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
		
		while (netCDFReader.nextWeatherData()){
			DataSample ds = weatherData.getDataSample();
			String geohash= ds.getID();
			if (geohash.startsWith("9pkv")==false)
				continue;
			anomalousDetector.process(geohash, ds);
			if (l%1000 == 0){
				System.out.println("sample #: "+l +"   : "+ weatherData.getGeohash()+": "+weatherData.toString());
			}
			++l;
		}
		System.out.println("Total:"+l);
    }      
    
         
   
    public synchronized  void sendCommand(double command) throws Exception{
    	anomalousDetector.process("9pkv", new DataSample(new double[]{command,-1,-1,-1,-1,-1,-1,-1,-1}));     	 
    }
 
    public static void main(String[] args) throws Exception{    		  
    	TestAnomalyDetection client = new TestAnomalyDetection();
    	UtilClass.fileAsOutputDst("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/kmOutput.txt");
   	 	client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2005");
		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2006");
		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2007");
		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2008");
		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/2009");
    	System.out.println("done!");
		client.sendCommand(Setting.START_TRAINING);
    }
}
