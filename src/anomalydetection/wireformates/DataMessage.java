package anomalydetection.wireformates;

import java.net.Socket;

import anomalydetection.handlers.MessageHandler;
import anomalydetection.util.ByteStream;

public class DataMessage extends Message{
	private String 		id;
    private double[] 	features ;
		
	public DataMessage() {
		super(MessageType.DATA_MESSAGE);
	}
			
	private void unpackMessage(byte[] byteStream){
		setId(unpackStringField(byteStream));
		int len = unPackNumberOfVariables(byteStream);
		features = new double[len];
		for (int i=0; i<len; ++i){
			features[i] = unpackDoubleField(byteStream);
		}
	}
		
	private byte[] packNumberOfVariables(){
		return ByteStream.intToByteArray(features.length);
	}
	private int unPackNumberOfVariables(byte[] b){
		return unpackIntField(b);
	}
	
	protected byte[] packMessageBody(){
		byte[] dataIDBytes 		= ByteStream.packString(id);
		byte[] numberOfValues 	= packNumberOfVariables();
		byte[] messageBody = ByteStream.join(dataIDBytes, numberOfValues);
		for (int i=0; i<features.length; ++i){
			messageBody = ByteStream.join(messageBody, ByteStream.doubleToByteArray(features[i]));
		}		
		return messageBody;
	}
		
	@Override
	public void initiate(byte[] byteStream) {
		unpackMessage(byteStream);
	}

	@Override
	public String getMessageType() {
		return null;
	}
	@Override
	public void handle(Socket link,	MessageHandler handler) {
		handler.handle(link, this);	
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double[] getFeatures() {
		return features;
	}

	public void setFeatures(double[] features) {
		this.features = features;
	}
	public static void main (String[]s){
		DataMessage dm = new DataMessage();
		double[] d ={1, 63.14,80.22,0.73};
		dm.setFeatures(d);
		dm.setId("Waalid72");
		byte[] bt =  dm.packMessage();
		DataMessage dm1 = new DataMessage();
		dm1.initiate(bt);
	}
	
}
