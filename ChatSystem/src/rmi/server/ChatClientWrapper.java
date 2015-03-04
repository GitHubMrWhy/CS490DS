package rmi.server;

import chat.constant.ChatSystemConstants;
import rmi.client.ChatClientIF;

public class ChatClientWrapper {
	ChatClientIF client;
	long timestamp;
	
	public ChatClientWrapper(ChatClientIF client){
		this.client = client;
		this.timestamp = System.currentTimeMillis();
	}
	
	public void refresh(){
		this.timestamp = System.currentTimeMillis();
	}
	
	public boolean isAlive(){
		
		return (timestamp+ChatSystemConstants.HEARTBEAT_RATE) >= System.currentTimeMillis();
	}
	
	public ChatClientIF getClient(){
		return client;
	}
}
