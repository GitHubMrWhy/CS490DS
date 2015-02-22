/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import chat.constant.ChatSystemConstants;
import chat.user.group.User;
import chat.user.group.UserGroup;

public class HeartbeatReceiver implements Runnable {

	private final UserGroup userGroup;

	private final DatagramSocket udpSocket;

	private final boolean isDebug;

	private boolean running; 

	public HeartbeatReceiver(final UserGroup userGroup, 
			final DatagramSocket udpSocket,
			boolean isDebug) {
		this.userGroup = userGroup;
		this.udpSocket = udpSocket;
		this.isDebug = isDebug;

		running = true;
	}

	public void run() {

		while(running){

			
			final byte[] buf = new byte[ChatSystemConstants.HEARTBEAT_LEN];

			// receive packet
			final DatagramPacket packet = new DatagramPacket(buf, buf.length);
			
			try {
				udpSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			final String msg = new String(buf);

			// Handle the packet
			if(msg.startsWith(ChatSystemConstants.MSG_HBT)){
				String name = msg.substring(ChatSystemConstants.MSG_HBT.length()).trim();

				log("Received a heartbeat from "+ name);

				final User alive_user = userGroup.get(name);

				if( null != alive_user){
					
					log("Refreshed the last heartbeat of " + name);
					alive_user.setLastHeartBeat(System.currentTimeMillis());
				}
			}
			else{
				log("Received invalid datagram " + msg);
			}
		}
	}
	
	private void log(String message){
		if(isDebug){
			System.out.println(message);
		}
	}


}
