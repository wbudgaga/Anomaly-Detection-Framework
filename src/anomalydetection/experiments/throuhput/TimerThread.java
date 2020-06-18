package anomalydetection.experiments.throuhput;

import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.Setting;


public class TimerThread extends Thread{
	private DetectorMaster 		detectorMaster;
	private volatile boolean 	keepRunning = true;
	public TimerThread(DetectorMaster 	detectorMaster){
		this.detectorMaster = detectorMaster;
	}
	
	protected void stopRunning(){
		this.keepRunning	=	false;
	}
	
	@Override
	public void run() {
		while (keepRunning){
			try {
				sleep(Setting.TIMER_INTERVAL);
				detectorMaster.printThrouhputReport();
			} catch (InterruptedException e) {
			}
		}
	}
}
