package rmi.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import chat.constant.ChatSystemConstants;

public class SendHeartBeat extends UnicastRemoteObject implements  Runnable{

	private static final long HEARTBEAT_RATE = 10*1000;
	private static ChatClient cc;
	protected SendHeartBeat(ChatClient cc) throws RemoteException {
		this.cc=cc;
		
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
				Thread.sleep(HEARTBEAT_RATE);
				this.cc.setLastHeartBeat();
				
				
			} catch (IOException e) {
				e.printStackTrace();
				
			} catch (InterruptedException ie){
				ie.printStackTrace();
				
			}
		}
		
	}

}
