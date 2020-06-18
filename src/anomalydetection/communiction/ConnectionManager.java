package anomalydetection.communiction;


//Manages the connections. it hides all the connections details. All connection operation can be  only performed through this class.

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import anomalydetection.handlers.MessageHandler;
import anomalydetection.util.ByteStream;
import anomalydetection.wireformates.DataMessage;
import anomalydetection.wireformates.Message;



public class ConnectionManager {
	private ListeningTask 	listeningTask ;
	private MessageHandler	messageHandler;
	public ConnectionManager(MessageHandler	messageHandler){
		this.messageHandler 	= messageHandler;
	}
	
	public void handleConnection(Socket connection) throws IOException{
		receiveData(connection);
	}
	
	public Socket openConnection(String hostAddress, int port) throws IOException{
		Socket s = new Socket(hostAddress, port);
		return s;
	}

	public void receiveData(Socket connection) throws IOException{
		messageHandler.getThreadPoolManager().addTask(new ReceivingTask(this,connection));
	}
	
	public void handleMassage(Socket link, byte[] byteBuffer) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		DataMessage msg =  new DataMessage();
		msg.initiate(byteBuffer);
		
		msg.handle(link, messageHandler);
	}
	
	public void startListening(int listeningPort) throws IOException{
		listeningTask = new ListeningTask(listeningPort,this);
		messageHandler.getThreadPoolManager().addTask(listeningTask);
	}

	public void stopListening(){
		if (listeningTask!= null)
			listeningTask.stop();
	}
	
	protected static void sendByteData(OutputStream outStream, byte[] dataToBeSent) throws IOException {
		outStream.write(ByteStream.addPacketHeader(dataToBeSent)); // message header will be added by sending each message
		outStream.flush();
	}

	public static void sendData(OutputStream outStream, Message msg) throws IOException{
		sendByteData(outStream,msg.packMessage());
	}
	
	public static boolean isAlive(String hostAddress, int port){
		try {
			Socket socket 	= new Socket(hostAddress, port);
			socket.close();
			return true;
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
		return false;
	}

	public void sendMessage(Message msg, Socket socket){
		try{
			boolean connected = socket.isConnected() && ! socket.isClosed();
			if (!connected)
				System.out.println("connection closed");
			sendByteData(socket.getOutputStream(),msg.packMessage());
		} catch (IOException e) {System.exit(-1);/*System.out.println("exception in connectionManager"+e.getMessage());*/}
	}

	public void sendMessage(Message msg, String hostAddress, int port){
		try{
			Socket socket 	= new Socket(hostAddress, port);
			sendByteData(socket.getOutputStream(),msg.packMessage());
		} catch (IOException e) {}
	}
	

}
