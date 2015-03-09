package rmi.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import rmi.server.ChatServerIF;
import chat.constant.ChatSystemConstants;

public class SendHeartBeat extends UnicastRemoteObject implements  Runnable{

	String name;
	
	ChatServerIF ss;
	
	protected SendHeartBeat(String name, ChatServerIF ss) throws RemoteException {
		this.ss=ss;
		this.name = name;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void run() {
		// TODO Auto-generated method stub
		
		while(true){
			try {
				
				// We send a heartbeat quicker in case that the packet gets lost.
				Thread.sleep(ChatSystemConstants.HEARTBEAT_RATE);
				this.ss.heartbeat(name);
				
				
			} catch (IOException e) {
				break;
				
			} catch (InterruptedException ie){
				break;
				
			}
		}
		
	}

}
