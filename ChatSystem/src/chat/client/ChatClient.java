/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import chat.constant.ChatSystemConstants;
import chat.gui.ChatClientGUI;

public class ChatClient {

	private ChatClientGUI gui;

	private String userName; 

	private final ServerSocket listener;

	private ChatSession chatSession;

	private final Socket server;

	private final boolean isDebug;

	private void log(String message){
		if(isDebug){
			System.out.println(message);
		}
	}

	/**
	 * Constructor call when used by console mode.
	 * @param ip_address
	 * 		IP address of the server.
	 * @param port
	 * 		Server port.
	 * @param is_debug
	 * 		If it is debugging.
	 * @throws IOException
	 */
	public ChatClient(String ipAddress, int port, boolean isDebug) throws IOException{
		listener = new ServerSocket(0);
		server = new Socket(ipAddress, port);
		this.isDebug = isDebug;
		chatSession = new ChatSession(this);

		gui = null;	

		log("Created " + listener);
	}

	/**
	 * Constructor call when used from a GUI
	 * @param ipAddress
	 * @param port
	 * @param isDebug
	 * @throws IOException
	 */
	public ChatClient(String ipAddress, int port, boolean isDebug, ChatClientGUI gui) throws IOException{
		listener = new ServerSocket(0);
		server = new Socket(ipAddress, port);
		this.isDebug = isDebug;
		chatSession = new ChatSession(this);

		gui = null;	
	}

	/**
	 * Connect to server. 
	 * @param userName
	 * @throws IOException 
	 */
	public boolean start(String userName) throws IOException{
		final boolean isConnected;

		this.userName = userName;

		log("User name:" + userName);

		final BufferedReader in = new BufferedReader(
				new InputStreamReader(server.getInputStream()));

		/**
		 * Set writer to be auto-flushed!
		 */
		final PrintWriter out = new PrintWriter(server.getOutputStream(), true);


		String reg_request = ChatSystemConstants.MSG_REG + userName + ":" + listener.getLocalPort();

		out.println(reg_request);

		log("Sent registration request [" + reg_request + "] to " + server);

		// Get the first message returned by server.
		String msg = in.readLine();

		if (msg == null){
			// Server closes the connection.
			log("Server closed connection.");
			isConnected = false;

		}		
		else if(msg.startsWith(ChatSystemConstants.MSG_REJ)){
			log("Server rejects registration request.");
			isConnected = false;
		}
		else if(msg.startsWith(ChatSystemConstants.MSG_ACK)){
			log("Registration succeeded.");

			// Activate a heart-beat sender.
			new Thread(new HeartbeatSender(server, userName)).start();

			// Activate an invitation receiver.
			new Thread(new InvitationHandler(this, this.listener)).start();

			// Sent GET request to obtain active user list
			out.println(ChatSystemConstants.MSG_GET);

			msg = in.readLine();

			// Receive the list of active users
			if(msg.startsWith(ChatSystemConstants.MSG_USG)){

				this.display("Active Users List");

				this.display(msg.substring(ChatSystemConstants.MSG_USG.length()));

				// Get and display the remaining lines.
				while( null != (msg = in.readLine())){
					this.display(msg);
				}
			}

			isConnected =  true;			
		}
		else{
			// Invalid message from server
			log("Received invalid message (" + msg + ").");
			isConnected = false;
		}

		server.close();
		
		return isConnected;
	}

	/**
	 * Connect to another client to initiate a chat session.
	 * @param client_ip
	 * @param port
	 * @return
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public boolean connect(String client_ip, int port) throws UnknownHostException, IOException{
		final boolean isConnected;

		Socket other = new Socket(client_ip, port);

		final BufferedReader in = new BufferedReader(
				new InputStreamReader(other.getInputStream()));

		final PrintWriter out = new PrintWriter(other.getOutputStream(), true);

		// Send invitation to the client
		out.println(userName);

		String msg =  in.readLine();

		if(msg.startsWith(ChatSystemConstants.MSG_ACK)){
			// Client on the other end confirmed start of chat session.
			synchronized(this.chatSession){

				if(this.chatSession.isOccupied()){
					log("Chat session is occupied.");
					isConnected = false;
				}

				else {

					log("Created a chat session with " + other);

					// Occupy the chat session.
					this.chatSession.serve(other);

					isConnected = true;
				}
			}
		}
		else if(msg.startsWith(ChatSystemConstants.MSG_REJ)){
			// Client on the other end rejects 
			log("Chat invitation rejected by " + other);
			isConnected = false;
		}
		else{
			log("Received invalid message (" + msg + ").");
			isConnected = false;
		}		

		return isConnected;
	}


	/**
	 * Display msg on the client side.
	 * @param msg
	 */
	public void display(String msg){
		if( null != gui){
			// gui.append(msg);
		}
		else{
			System.out.println(msg);
		}
	}

	public ChatSession getChatSession() {
		return chatSession;
	}


	/**
	 * program runs with commands -ip=(host name|ip address) -port=number [-debug]
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		boolean is_debug = false;
		int server_port = ChatSystemConstants.DEFAULT_PORT;
		String server_ip = "";
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
				server_port = Integer.parseInt(command.substring(6));
			}
			else if(command.startsWith("-ip=")){
				server_ip = command.substring(4);
			}
		}

		try {
			ChatClient chatClient = new ChatClient(server_ip, server_port, is_debug);

			Scanner in = new Scanner(System.in);

			System.out.print("Enter user name:");

			chatClient.start(in.next(ChatSystemConstants.NAME_PATTERN));


			System.out.print("Enter IP address and port to start a chat session:");

			String client_ip_addr = in.next("[^:]+");
			int client_port = in.nextInt();
			chatClient.connect(client_ip_addr, client_port);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
