package anomalydetection.communiction;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import anomalydetection.threadpool.Task;

// it is a thread class that is responsible for accepting new incoming connections.

public class ListeningTask extends Task{
	private ServerSocket 		serverSocket;
	private ConnectionManager 	connectionManager;

	 public ListeningTask(int port, ConnectionManager connectionManager) throws IOException{
		 this.connectionManager = connectionManager;
		 serverSocket 			= new ServerSocket(port,100);
	 }
	
	 private void listening() throws IOException{	
		 Socket connectionSocket = serverSocket.accept(); 
		 connectionManager.handleConnection(connectionSocket);
	 }
	 
	 
	 public void stop(){
		try {
			serverSocket.close();
		} catch (IOException e) {}    
	}
	 
	@Override
	public void execute() {
		try{
		// Run forever, accepting and servicing connections
		 while (true) 
			listening();
		}catch(IOException ioE){
			System.out.println("IOException in Listening "+ioE.getMessage());
		}
	}
}
