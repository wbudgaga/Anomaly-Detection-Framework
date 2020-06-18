
package anomalydetection.test;

import anomalydetection.util.NetCDFReader;
import anomalydetection.util.UtilClass;
import anomalydetection.util.WeatherData;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DataExtracter {
	private File 			file;
	private BufferedWriter 	out;
	private String[] 		geoList={"f4du","djjs","9pkv","f2y8","dk6m","dk63","c3w6","c1f2","9wwh","9s0j"};
	
	public DataExtracter(String dstDir,String fName) throws IOException{
		file 			= new File(dstDir,fName); 
		FileWriter f 	= new FileWriter(file);
		out 			= new BufferedWriter(f);	
	}
	public void close() throws IOException {
		out.flush();
		out.close();
	}
	
	public boolean shouldSave(String geo){
		for (String g:geoList){
			if (g.equals(geo)==true)
				return true;			
		}
		return false;
	}
		

     
    public void startExtracting(String dir) throws Exception{
		WeatherData  weatherData  = new WeatherData();
		NetCDFReader netCDFReader =  new NetCDFReader(dir,weatherData);
		long l = 0;
		String geo="";
		
		while (netCDFReader.nextWeatherData()){
			geo=weatherData.getGeohash();
			if (shouldSave(geo.substring(0,4)))
				out.write(weatherData.getGeohash()+": "+weatherData.toString()+"\n");
		}
		close();
    }      
    
 
/*    public static void main(String[] args) throws Exception{
    	DataExtracter d= new DataExtracter();
    	String[] 		geoList={"absbvagdfwy","absbvagdfwy","c1f2vagdfwy","absbvagdfwy","absbvagdfwy","absbvagdfwy","absbvagdfwy","absbvagdfwy","absbvagdfwy","absbvagdfwy"};
    	//System.out.println(geoList[0].substring(0,4));
    	for (String g:geoList){
    		System.out.println(g+"  "+d.shouldSave(g.substring(0,4)));
    	}
    	
    }*/
      public static void main(String[] args) throws Exception{
    	  	if (args.length!=2){
    	  		System.out.println("Please enter port,  and year");
    	  		return;
    	  	}
    	  	String year	 	= args[1];
    	  	String workDir 	= "/s/lattice-77/c/nobackup/galileo/wbudgaga/noaa";
    	  
    		  
    	  
  	       	DataExtracter client = new DataExtracter(workDir, year+".txt");
    		client.startExtracting("/s/lattice-77/b/nobackup/galileo/noaa/"+year);
    		System.out.println("done!");
     }
}
