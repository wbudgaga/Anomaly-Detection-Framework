package anomalydetection.clustering;

public interface Setting{
	public final String 	LOADMODEL_AT		= "em";// "kmMaha";//"none"; //"2014_2_4"; //none:not load. Or yyyy_m_d: load model at specified date
	public final boolean	SAVE_NEW_MODEL		= true ;
	public final boolean	ONLINE_ADAPTABLE	= false;//false;//true; 
	public final String 	MAIN_ALGORITHM		= "EM";//"KMEANS";//"EM";
	public final String 	EM_INIT_CLUSTERER	= "kmean";
	public final String 	WORK_DIR			= "/s/chopin/b/grad/budgaga/noaa/galileoWorkDir/expTestAD/workdir/"; //"c:\\tmp\\galileoTest\\"; ///tmp/galileo/";
	public final String 	LOG_DIR				= WORK_DIR+"logs/";
	public final String 	MODEL_DIR			= WORK_DIR+"models/"; //+"models/";
	public final String 	CURRENT_MODEL_DIR	= MODEL_DIR	+ MAIN_ALGORITHM; //"2015_3_5";
	public final int		TRAINING_SIZE 		= -1;//3000;
	public final int		SRV_THREADPOOL_SIZE	= 1;
	public final int		THREADPOOL_SIZE  	= 1;
	public final int		NUM_OF_DETECTORS 	= 100;
	public final int		NUM_OF_CLUSTERS 	= 40;
	public final int		NUM_OF_ATTRIBUTES	= 9;
	public final int		CLUSTERING_RATE  	= -1;
	public final int		SAVE_RATE  			= -1;//SAVE_RATE  =t means the model will be saved after each t processed observations
	public final String 	TP_DIR				= WORK_DIR+"TP/";
	
	//commands between client & detectorMaster
	public final int		START_TRAINING		= -1;
	public final int		START_TIMER			= -2;
	public final int		PRINT_PROCESSED		= -3;
	public final int		STOP_DETECTORS		= -4;
	public final int		PUBLISH_OBS			= -5;
	//-------------------------------------------------
	public final int		TIMER_INTERVAL		= 1000 * 10; // 1000 millisecond=1 second
}