
package anomalydetection.clustering;

import java.util.LinkedList;
import java.util.Queue;

import anomalydetection.threadpool.Task;

public class ObservationClusteringTask extends Task{
	private Process		mainProcess;
	private Queue<Instance> pendingInstances 	= new LinkedList<Instance>();
	
	public  void setMainProcess(Process mainProcess){
		this.mainProcess 	=  mainProcess;
	}

	@Override
	public void execute() {
		try {
			cluster();
			mainProcess = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void cluster(){
		if (isTherePendingInstance()){
			mainProcess.cluster(dequeueInstance());
			mainProcess.addProcessedSample();
		}
	}
	
	public boolean enqueueInstance(Instance instance){
		synchronized (pendingInstances){
			return pendingInstances.offer(instance);
		}
	}
	
	protected Instance dequeueInstance( ){
		synchronized (pendingInstances){
			return pendingInstances.poll();
		}
	}
	
	public boolean isTherePendingInstance(){
		synchronized (pendingInstances){
		return pendingInstances.size() > 0;
		}
	}
}
