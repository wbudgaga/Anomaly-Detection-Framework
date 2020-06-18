
package anomalydetection.experiments.throuhput;

import java.io.IOException;
import java.net.Socket;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Setting;
import anomalydetection.communiction.ConnectionManager;
import anomalydetection.util.NetCDFReader;
import anomalydetection.util.UtilClass;
import anomalydetection.util.WeatherData;
import anomalydetection.wireformates.DataMessage;

public class ThroughputClient {
    private final String 			machineName		= "lattice-";
    private final int  				numOfMachines	= 78;
    private final int  				port;
    protected ConnectionManager connectionManager;
    private Socket[]			servers = new Socket[numOfMachines];

    public ThroughputClient(int port) throws Exception {
    	this.port = port;
    	connectionManager = new ConnectionManager(null);
    }
    
    public int StoreBlock(DataSample weatherData) throws Exception{
        String geohash = weatherData.getID();
        DataMessage dm = new DataMessage();
        dm.setId(geohash);
        dm.setFeatures(weatherData.get());
        int machineNum = UtilClass.getRegionNum(geohash,3,  numOfMachines);
    	send(machineNum,  dm);
    	return machineNum;
    }

    public void connectAll() throws IOException{
      	for (int i = 0; i < numOfMachines; ++i){
     		servers[i] = connectionManager.openConnection(machineName+i, port);
     	}
    }
     
    public void startPublish(String dir) {
    	try{
			WeatherData  weatherData  = new WeatherData();
			NetCDFReader netCDFReader =  new NetCDFReader(dir,weatherData);
			long l = 0;
			while (netCDFReader.nextWeatherData()){
				StoreBlock(weatherData.getDataSample());		 
				++l;
			}
			System.out.println("Total:"+l);
       	}catch(Exception e){
       		System.out.println("App exited because of the thrown exception");
    		return;
    	}

    }      
    
    public void send(int hostID, DataMessage dm) throws Exception {
    	connectionManager.sendMessage(dm, servers[hostID]);
    }       
    public void sendanomalous() throws Exception {  	
    	DataMessage dm = new DataMessage();
    	dm.setId("anomalousObservation");
    	dm.setFeatures(new double[]{Setting.PUBLISH_OBS,15.0,1080.0, 498.2982177734375,  101746.0,  0.07, 0.0,  90.0,   7.34});
       	for (int i = 0; i < numOfMachines; ++i){
       		send(i, dm);
       	}
    }       

    
    public synchronized  void sendCommand(double command) throws Exception{
	    	DataMessage dm = new DataMessage();
	    	dm.setId("training");
	    	dm.setFeatures(new double[]{command,-1,-1,-1,-1,-1,-1,-1,-1});
	       	for (int i = 0; i < numOfMachines; ++i){
	       		send(i, dm);
	       	}
     }
 
      public static void main(String[] args) throws Exception{
    	  int	 port			= Integer.parseInt(args[0]);
    	  String dir 			= args[1]; 
    	  String machineName	= args[2];
    	  //UtilClass.fileAsOutputDst("/tmp/galileoData/"+machineName+".txt");
  	      ThroughputClient client = new ThroughputClient(port);
   	 	  client.connectAll();
   	 	//client.sendCommand(Setting.START_TIMER);
   	 	  client.startPublish(dir);
   	 	//client.sendanomalous();
    	  System.out.println("done!");
    }
}
