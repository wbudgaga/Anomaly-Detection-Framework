
package anomalydetection.clustering;

import java.util.LinkedList;

import anomalydetection.clustering.Instance;

public class ClusteringTask extends ObservationClusteringTask{

	@Override
	public void execute() {
		cluster();
	}
	
	public void cluster() {
		while (isTherePendingInstance()){
			super.cluster();
		}
	}
}
