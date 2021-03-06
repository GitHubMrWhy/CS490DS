/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import chat.constant.ChatSystemConstants;

public class DummyHeartbeatSender implements Runnable {

	private final DatagramSocket udpSocket;


	private byte[] buf;

	private volatile boolean running = true;

	private final String prefix;

	private final long n_user;

	private final InetAddress serverIp;
	
	private final int serverPort;

	public DummyHeartbeatSender(final String serverIp, final int serverPort
			, final String prefix, final long n_user) throws SocketException, UnknownHostException{
		
		udpSocket = new DatagramSocket();

		this.serverIp = InetAddress.getByName(serverIp);
		this.serverPort = serverPort;
		this.n_user = n_user;
		this.prefix = prefix;

	}


	public void run(){
		while(running){
			try {

				// We send a heartbeat quicker in case that the packet gets lost.
				Thread.sleep((long) (ChatSystemConstants.HEARTBEAT_RATE*0.6));

				for(int i = 0; i < n_user; i++){
					// Craft the heartbeat messages.
					buf = (ChatSystemConstants.MSG_HBT + prefix + n_user).getBytes();

					DatagramPacket packet = new DatagramPacket(buf,
							buf.length,
							serverIp,
							serverPort);
					udpSocket.send(packet);
				}

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
