
package anomalydetection.experiments.buildingmodels;

import java.io.IOException;
import java.net.Socket;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.Setting;
import anomalydetection.communiction.ConnectionManager;
import anomalydetection.util.NetCDFReader;
import anomalydetection.util.UtilClass;
import anomalydetection.util.WeatherData;
import anomalydetection.wireformates.DataMessage;

public class EMModelBuilder {
    private final String 			machineName		= "lattice-";
    private final int  				numOfMachines	= 78;
    private final int  				port;
    protected ConnectionManager connectionManager;
    private Socket[]			servers = new Socket[numOfMachines];

    public EMModelBuilder(int port) throws Exception {
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

     
    public void startPublish(String dir) throws Exception{
		WeatherData  weatherData  = new WeatherData();
		NetCDFReader netCDFReader =  new NetCDFReader(dir,weatherData);
		long l = 0;
		while (netCDFReader.nextWeatherData()){
			StoreBlock(weatherData.getDataSample());
			if (l%1000000 == 0){
				System.out.println("sample #: "+l +"   : "+ weatherData.getGeohash()+": "+weatherData.toString());
			}
			++l;
		}
		System.out.println("Total:"+l);
    }      
    
    public void send(int hostID, DataMessage dm) throws Exception {
    	connectionManager.sendMessage(dm, servers[hostID]);
 /*   	Socket s = connectionManager.openConnection("red-rock", port);
    	connectionManager.sendMessage(dm,s);
    	s.close();
*/    }
        
   
    public synchronized  void sendCommand(double command) throws Exception{
    	DataMessage dm = new DataMessage();
    	dm.setId("training");
    	dm.setFeatures(new double[]{command,-1,-1,-1,-1,-1,-1,-1,-1});
       	for (int i = 0; i < numOfMachines; ++i){
       		send(i, dm);
       	}      	 
    }
 
      public static void main(String[] args) throws Exception{
    	  if (args.length!=2){
    		  System.out.println("Please enter port,  and year");
    		  return;
    	  }
    	  int	 port		= Integer.parseInt(args[0]);
    	  String year	 	= args[1];
    	  String workDir 	= "/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/results/";
    	  
    		  
    	  
  	      EMModelBuilder client = new EMModelBuilder(port);
   	 	  client.connectAll();
   	 	  if (year.compareTo("-1")!=0){
   	 		UtilClass.fileAsOutputDst(workDir+year+".txt");
   	 		client.startPublish("/s/lattice-77/c/nobackup/galileo/noaa/"+year);
   	 	  }else{
   	 	   	 	client.sendCommand(Setting.START_TRAINING);
   	 	  }
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
