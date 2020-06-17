package anomalydetection.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPoint;


//Comparing each signal segment with all others segments and find the one that has the biggest different.  
public class NetCDFReader {
	protected ArrayList<String> 		gribFiles 		= new ArrayList<String> ();
	protected WeatherData				weatherData;
	protected String[]					featureSet;
	protected ArrayList<IndexIterator>	iterators	= new ArrayList<IndexIterator> ();
	protected int						curFileIdx	= -1;
	protected double 					dayNumber;
	protected double 					dayMinutes;
	protected GridCoordSystem 			gridCoordSystem;
	protected int						numOfTries;
	
	public NetCDFReader(String dataFolder, WeatherData	weatherData){
		this.weatherData = weatherData;
		featureSet = weatherData.getFeatureList();
		readFilesFromDir(dataFolder);
	}
	
	public NetCDFReader(String []dataFolder, WeatherData	weatherData){
		this.weatherData = weatherData;
		featureSet = weatherData.getFeatureList();
		for (String folder: dataFolder) {
			readFilesFromDir(folder);
		}
	}

	private void readFilesFromDir(String dataFolder){
		File dir = new File(dataFolder);
		for (File f : dir.listFiles()) {
			// Make sure we only try to read grib files
	        String fileName = f.getName();
	        String ext = fileName.substring(fileName.lastIndexOf('.') + 1,fileName.length());
	        if (ext.equals("bz2") || ext.equals("grb")) {
	        	gribFiles.add(f.getAbsolutePath());
	        }
		}
		System.out.println("# of files has been parsed: "+ gribFiles.size());
	}
	
	public void sort(){
		String[] filesArray = new String[gribFiles.size()];
		int i = 0;
		for(String item:gribFiles){
			filesArray[i++]=item;
		}
			
		Arrays.sort(filesArray);
		gribFiles = new ArrayList<String> (Arrays.asList(filesArray));
		
		System.out.println("# first item: "+ gribFiles.get(0)+"  last item:"+gribFiles.get(gribFiles.size()-1));
	}
	
	protected boolean prepareVariables(NetcdfFile currFile) throws IOException{
		Variable var = null;
		long size = 0;
		iterators.clear();
		for(int i=2;i<featureSet.length;++i){
			var= currFile.findVariable(featureSet[i]);
	
			if (var== null || (size != 0 && var.getSize() != size) ){
				System.out.println("The feature "+ featureSet[i]+" is either not found or has not the same size like other features");
				return false;
			}
			size = var.getSize();
			iterators.add(var.read().getIndexIterator());
		}
		createGridCoordSystem(currFile);
        return true;
	}
	
	protected void createGridCoordSystem(NetcdfFile currFile) throws IOException{
		NetcdfDataset 	dataset = new NetcdfDataset(currFile);
	    GridDataset 	grid 	= new GridDataset(dataset);
        GridDatatype 	datatype= grid.findGridDatatype("Temperature_surface");
        gridCoordSystem 		= datatype.getCoordinateSystem();
	} 
	
	private double[] getNextSample() throws IOException{
		double[] dataSample = new double[featureSet.length];
		dataSample[0] = dayNumber;
		dataSample[1] = dayMinutes;

		for(int i=2;i<featureSet.length;++i){
			IndexIterator iterator = iterators.get(i - 2);
			dataSample[i]= iterator.getDoubleNext();
		}
		int[] indices =  iterators.get(0).getCurrentCounter();// (time,[another variable],y,x), t= indices[0] x= indices[indices.length-1] and y= indices[indices.length-2]
		LatLonPoint latLonPoint = gridCoordSystem.getLatLon(indices[indices.length-1], indices[indices.length-2]);
		weatherData.setSpatial((float)latLonPoint.getLatitude(),(float)latLonPoint.getLongitude());
		return 	dataSample;
  	}

	protected boolean prepareNextFile() throws IOException{
		NetcdfFile file = null;
		while (curFileIdx < gribFiles.size()){
			if (curFileIdx>0){
				String fName =gribFiles.get(curFileIdx-1);
				int idx = fName.lastIndexOf("."); 
				fName = fName.substring(0, idx-1)+".*";
				fName =  fName.replace("/","%2F");
				UtilClass.deleteFile("/s/chopin/b/grad/wbudgaga/.unidata/cache/", fName);
			}
			if((file = nextFile())==null) continue;
			boolean variablesPrepared = prepareVariables(file);
			file.close();
			if (variablesPrepared)
				return true;
		}
		return false;
	}
	
	public double[] nextSample() throws IOException{
		if (iterators.size()== 0 || !iterators.get(0).hasNext() ){
			if (!prepareNextFile())
				return null;
		}
		return getNextSample();
	}
	
	public boolean nextWeatherData() throws IOException{
		double[] sample = nextSample();
		if (sample == null)
			return false;
		weatherData.setFeaturesValues(sample);
		return true;
	}

	private NetcdfFile getFile(int fIndex){
		String fName = gribFiles.get(fIndex);
		try{
			setTime(fName);
		//	System.out.println("Reading: "+fName+" ==> ("+ dayNumber+") ("+dayMinutes+")");
			return NetcdfFile.open(fName);
		}catch(IOException e){System.out.println("Could not read from "+fName+" details:"+e.getMessage() );}
		
		return null;
	}
	
	protected NetcdfFile nextFile(){
		if (++curFileIdx < gribFiles.size()){
			return getFile(curFileIdx);
		}
		return null;
	}
	
	public String[] testFiles() throws IOException{
		NetcdfFile file;
		String fileName;
		String[] filesDates = new String[gribFiles.size()];
		while (curFileIdx<gribFiles.size()-1){
			if ((file=nextFile())!=null){
				if (!prepareVariables(file)){
					file.close();
					file=null;
					filesDates[curFileIdx]="        ";
					continue;
				}
			  fileName = gribFiles.get(curFileIdx);
			  
				String[]filenameParts 	= fileName.substring(fileName.lastIndexOf("/")+1).split("_");
				filesDates[curFileIdx] = filenameParts[2];//+"_"+filenameParts[3];

			  file.close();
			}else 
				filesDates[curFileIdx]="        ";
			//System.out.println(filenameParts[2]+"_"+filenameParts[3]);
			
		}
		return filesDates;
	}
   
	private void setTime(String fileName){
		String[]filenameParts 	= fileName.substring(fileName.lastIndexOf("/")+1).split("_");
		int 	year 			= Integer.parseInt(filenameParts[2].substring(0, 4));
		int 	month 			= Integer.parseInt(filenameParts[2].substring(4, 6));
		int 	day 			= Integer.parseInt(filenameParts[2].substring(6,8));
		dayNumber = UtilDate.getDayNumber(month,day);
		
		int 	hours 			= Integer.parseInt(filenameParts[3].substring(0,2));
		int 	minute 			= Integer.parseInt(filenameParts[3].substring(2,4));
		dayMinutes = UtilDate.getDayMinutes(hours, minute);
		//weatherData.setTemporalRange(UtilDate.getTime(year,month,day,hours), UtilDate.getTime(year,month,day,hours));
	}

}