package anomalydetection.experiments.anomalyDetection;
import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.Setting;
import anomalydetection.threadpool.ThreadPoolManager;
import anomalydetection.util.UtilClass;

public class VarifyTestAnomalyDetection {
	private DetectorMaster 	anomalousDetector;

    public VarifyTestAnomalyDetection() throws Exception {
		try{
			anomalousDetector 	= new DetectorMaster(1); 
			anomalousDetector.start(Setting.MAIN_ALGORITHM, Setting.NUM_OF_ATTRIBUTES);
		}catch(Exception e){
			System.out.println("Exception in StorageNode: "+e.getMessage());
		}
    }

    public void startPublish(String fName, String geohash) throws Exception{
    	FileReader 	fr = new FileReader(fName);
		String line = null;
		int counter=0;
		while ((line=fr.nextLine())!=null){
			String[] data = line.split(",");
			double[] features = UtilClass.toDoubleArray(data);			
			anomalousDetector.process(geohash, new DataSample(features));
			
			++counter;
		}
		System.out.println("Total:"+counter);
    }      
  
    public static void main(String[] args) throws Exception{    		  
    	VarifyTestAnomalyDetection client = new VarifyTestAnomalyDetection();
    	String geo= "9pkv";
    	UtilClass.fileAsOutputDst("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/workdir/out/"+Setting.MAIN_ALGORITHM+"_"+geo+".txt");
    	client.startPublish("/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/workdir/both_"+geo+".txt",geo);
     }
}
