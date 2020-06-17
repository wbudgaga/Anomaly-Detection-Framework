package anomalydetection.handlers;

import java.net.Socket;

import anomalydetection.peers.Server;
import anomalydetection.threadpool.ThreadPoolManager;
import anomalydetection.wireformates.DataMessage;

public  class MessageHandler {
	protected Server node;
	public MessageHandler(Server node){
		this.node = node;
	}
	public void handle(Socket link, DataMessage msg) {
		try {
			node.process(msg.getId(), msg.getFeatures());
		} catch (Exception e) {
			System.out.println("exception happened in handler: "+e.getMessage());
		}
	}
	public ThreadPoolManager getThreadPoolManager(){
		return node.getThreadPoolManager();
	}
	
}
