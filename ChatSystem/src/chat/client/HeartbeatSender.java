/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import chat.constant.ChatSystemConstants;

public class HeartbeatSender implements Runnable {

	private final DatagramSocket udpSocket;
	
	private final DatagramPacket packet;
	
	private final byte[] buf;
	
	private volatile boolean running = true;
	
	public HeartbeatSender(final Socket server, final String userName) throws SocketException{
		udpSocket = new DatagramSocket();
		
		// Craft the heartbeat messages.
		buf = (ChatSystemConstants.MSG_HBT + userName).getBytes();
		
		packet = new DatagramPacket(buf,
				buf.length,
				server.getInetAddress(),
				server.getPort());
	}
	

	public void run(){
		while(running){
			try {
				
				// We send a heartbeat quicker in case that the packet gets lost.
				Thread.sleep((long) (ChatSystemConstants.HEARTBEAT_RATE*0.4));
				
				udpSocket.send(packet);
				
			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			} catch (InterruptedException ie){
				ie.printStackTrace();
				running = false;
			}
		}
	}

}
