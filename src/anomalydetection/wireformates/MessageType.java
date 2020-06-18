package anomalydetection.wireformates;


// contains the messages types and their equivalent classes' names
// it is very important to keep the order the same in both lists 
public interface MessageType {
	public static final byte SUCCEESS 						= 1;
	public static final byte FAILURE 						= 2;
	
	public static final int DATA_MESSAGE 				= 0;

		
	public static enum ClassName{
		RegisterRequest,
		RegisterResponse,
		PeerInfo,
		Predecessor,
		Successor,
		Lookup,
		Forward,
		QueryResult,
		GetPredecessor,
		RandomPeerRequest,
		StoreFileRequest,
		Get,
		GetResponse;

		 public static String get(int i){
			 return values()[i].toString();
		 }
	}
}
