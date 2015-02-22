/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import chat.user.group.UGSimpleImpl;
import chat.user.group.UserGroup;
import chat.constant.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * A single-threaded implementation of chat server.
 */
public class SingleThreadedChatServer {
	
	private final ServerSocket listener;
	
	private final UserGroup userGroup;
	
	/**
	 * Number of connection has established.
	 */
	private long clientNumber = 0;
	
	/**
	 * If it is debugging.
	 */
	private final boolean isDebug;
	
	/**
	 * 
	 * @param port
	 * 		- Port number used to create server socket.
	 * @param isDebug
	 * 		- if it is debugging.
	 * @throws IOException
	 */
	public SingleThreadedChatServer(int port, boolean isDebug) throws IOException{	
		this.isDebug = isDebug;
		userGroup = new UGSimpleImpl();
		listener = new ServerSocket(port);
		
		final DatagramSocket udpSocket = new DatagramSocket(port);
		new Thread(new HeartbeatReceiver(userGroup, udpSocket, isDebug)).start();
		
		
		log("Created " + listener.toString());
	}
	
	private void log(String message){
		if(isDebug){
			System.out.println(message);
		}
	}
	
	public void run() throws Exception{
		try{
			while(true){
				Socket client = listener.accept();
				log("New connection # " + clientNumber +" at " + client);
				
				clientNumber++;
				
				// Handle message from client.
				new MessageHandler(client, userGroup, isDebug).run();
				
				log("Close the # " + clientNumber + " connection.");
				
			
			}
		} finally {
			listener.close();
		}
	}
	
	/**
	 * program runs with optional commands [-port=number] [-debug]
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		boolean is_debug = false;
		int port = ChatSystemConstants.DEFAULT_PORT;
	
		/**
		 * Parse user commands.
		 */
		// Use set to handle commands entered without order.
		Set<String> commands = new HashSet<String>();
		for(int i=0; i<args.length; i++){
			commands.add(args[i]);
		}
		
		
		for(String command : commands){
			if(command.startsWith("-debug")){
				is_debug = true;
			}
			else if(command.startsWith("-port=")){
				port = Integer.parseInt(command.substring(6));
			}		
		}
		
		/**
		 * Spin the server.
		 */
		SingleThreadedChatServer st_server = new SingleThreadedChatServer(port, is_debug);
		st_server.run();
	
	}


}
