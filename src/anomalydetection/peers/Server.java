package anomalydetection.peers;

import java.io.IOException;

import anomalydetection.clustering.DataSample;
import anomalydetection.clustering.DetectorMaster;
import anomalydetection.clustering.Setting;
import anomalydetection.communiction.ConnectionManager;
import anomalydetection.handlers.MessageHandler;
import anomalydetection.threadpool.ThreadPoolManager;
import anomalydetection.util.UtilClass;
import anomalydetection.util.UtilDate;

public class Server{
	protected ConnectionManager connectionManager;
	protected MessageHandler 	handler;
	private ThreadPoolManager threadPoolManager;
	private int port;
	private int threadpoolSize;
	private DetectorMaster 	anomalousDetector;
	
	public Server(int port){
		threadpoolSize = Setting.THREADPOOL_SIZE;
		init(port);
	}

	public Server(int port, int threadpoolSize){
		this.threadpoolSize = threadpoolSize;
		init(port);
	}

	private void init(int port){
		this.port = port;
		handler = new MessageHandler(this); 
		connectionManager = new ConnectionManager(handler);
		UtilClass.fileAsOutputDst(Setting.TP_DIR+"out_"+Setting.MAIN_ALGORITHM+"_"+UtilDate.getDateString()+".txt");
		createAnomalyDetector();
	}

	private void createAnomalyDetector(){
		try{
			anomalousDetector 	= new DetectorMaster(threadpoolSize); 
			anomalousDetector.start(Setting.MAIN_ALGORITHM, Setting.NUM_OF_ATTRIBUTES);
		}catch(Exception e){
			System.out.println("Exception in StorageNode: "+e.getMessage());
		}
	}
	
	public void process(String geohash, double[] features) throws Exception{
       anomalousDetector.process(geohash,new DataSample(features));	
	}
	
	protected boolean startListening(int connThreadpoolSize){
		threadPoolManager = new ThreadPoolManager(connThreadpoolSize);
		threadPoolManager.start();
		try {
			connectionManager.startListening(port);
			System.out.println("Listening on port "+ port);
		} catch (IOException e) {
			System.err.println("It couldn't listen on the port "+port);
			threadPoolManager.stop();
			return false;
		}
		return true;
	}
	
	public ThreadPoolManager getThreadPoolManager() {
		return threadPoolManager;
	}

	public static void main(String args[]) throws InstantiationException, IllegalAccessException, IOException {
		Server discovery;
		      
		if (args.length < 1) {
			System.err.println("Discovery Node:  Usage:");
			System.err.println("         java dht.chordpeer2peernetwork.nodes.Discovery portnr [threadPoolSize]");
		    return;
		}
		try{
			int port = Integer.parseInt(args[0]);
			if (args.length >1)
				discovery = new Server(port,Integer.parseInt(args[1]));
			else
				discovery = new Server(port);
			discovery.startListening(Setting.SRV_THREADPOOL_SIZE);
		}catch(NumberFormatException e){
			System.err.println("Discovery Node: the values of portnum must be integer");
		}
	}
}
