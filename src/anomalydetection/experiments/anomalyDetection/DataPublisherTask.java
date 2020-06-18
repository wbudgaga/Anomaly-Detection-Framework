package anomalydetection.experiments.anomalyDetection;

import java.io.FileNotFoundException;
import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.threadpool.Task;
import anomalydetection.util.UtilClass;

public class DataPublisherTask extends Task{
	public  long counter = 0;
	private DetectorMaster 	anomalousDetector;
	private FileReader 		fr;
	
	public DataPublisherTask(String fName, DetectorMaster 	anomalousDetector) throws FileNotFoundException{
		this.anomalousDetector 	= anomalousDetector;
		fr =  new FileReader(fName);
	}
	
    public void startPublish() throws Exception{
		String line = null;
		while ((line=fr.nextLine())!=null){
			String[] data = line.split(":");
			double[] features = UtilClass.toDoubleArray(data[1].split("   "));			
			anomalousDetector.process(data[0], new DataSample(features));
			++counter;
		}
		System.out.println("Total:"+counter);
    }      

	@Override
	public void execute() {
		try {
			startPublish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
