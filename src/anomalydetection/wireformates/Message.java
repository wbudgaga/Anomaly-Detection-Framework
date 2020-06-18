package anomalydetection.wireformates;



import java.net.Socket;

import anomalydetection.handlers.MessageHandler;
import anomalydetection.util.ByteStream;

// it has the methods that are used by all message classes. It has the basic operations to convert between primitive types and byte strem
public abstract class Message implements MessageType{
	private int id;
	
	protected int 	currentIndex;
	
	public  Message(int id){
		currentIndex = 4;
		this.id = id;
	}
	
	public int getMessageID() {
		return id;
	}
		
	protected byte[] readNextBytes(byte[] byteStream,int length){
		byte [] bytes = ByteStream.getBytes(byteStream,currentIndex,length);
		currentIndex +=length;
		return bytes;
	}

	protected String unpackStringField(byte[] byteStream){
		byte[] stringBytes = readObjectBytes(byteStream);
		return ByteStream.byteArrayToString(stringBytes);
	}
	
	protected double unpackDoubleField(byte[] byteStream){
		byte[] doubleBytes = readNextBytes(byteStream,8);
		return ByteStream.byteArrayToDouble(doubleBytes);
	}

	protected int unpackIntField(byte[] byteStream){
		byte[] intBytes = readNextBytes(byteStream,4);
		return ByteStream.byteArrayToInt(intBytes);
	}

	protected byte unpackByteField(byte[] byteStream){
		byte[] byteFiled = readNextBytes(byteStream,1);
		return byteFiled[0];
	}
	
	protected byte[] packMessageID(){
		return ByteStream.intToByteArray(getMessageID());
	}
	

	public static final int unpackMessageID(byte[]  byteStream){
		byte[] messageIdBytes = ByteStream.getBytes(byteStream,0,4);
		return ByteStream.byteArrayToInt(messageIdBytes);
	}

	protected byte[] readObjectBytes(byte[] byteStream){
		byte[] objectLengthInBytes 	= readNextBytes(byteStream,4);
		int objectLength 			= ByteStream.byteArrayToInt(objectLengthInBytes);
		return readNextBytes(byteStream,objectLength);
	}

	public  byte[] packMessage(){
		return ByteStream.join(packMessageID(), packMessageBody());
	}
	
	public abstract void   initiate(byte[]  byteStream);
	public abstract void   handle(Socket link, MessageHandler handler);
	protected abstract byte[] packMessageBody();
	public abstract String getMessageType();
}
