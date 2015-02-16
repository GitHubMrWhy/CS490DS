/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.UserGroup;
import chat.constant.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * A single-threaded implementation of chat server.
 */
public class SingleThreadedChatServer {

	
	private int port; 
	
	private ServerSocket listener;
	
	private UserGroup userGroup;
	
	private MessageHandler msgHandler;
	
	/**
	 * Number of connection has established.
	 */
	private long clientNumber = 0;
	
	/**
	 * If it is debugging.
	 */
	private boolean isDebug;
	

	public SingleThreadedChatServer(int port, boolean isDebug){
		this.port = port;	
		this.isDebug = isDebug;
	}
	
	/**
	 * Caller must call init() before calling run().
	 * @throws IOException
	 */
	public void init() throws IOException {
		userGroup = new UGConcurrentHashMapImpl();
		msgHandler = new MessageHandler();
		listener = new ServerSocket(port);
		log("Socket created " + listener.toString());
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
				// Handle messages from client.
				msgHandler.handle(client, userGroup, isDebug);
				
				log("Close the # " + clientNumber + " connection.");
				client.close();
			
			}
		} finally {
			listener.close();
		}
	}
	

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}
	
	/**
	 * program runs with optional commands [-port=number] [-debug]
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
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
		st_server.init();
		st_server.run();
	
	}


}
