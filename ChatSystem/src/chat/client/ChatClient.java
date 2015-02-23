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

	private final ChatClientGUI gui;

	private String userName; 

	private final ServerSocket listener;

	private ChatSession chatSession;

	private final String serverAddr;

	private final int serverPort;

	private final boolean isDebug;

	private void log(String message){
		if(isDebug){
			System.out.print("----");
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

		this.isDebug = isDebug;
		chatSession = new ChatSession(this);
		serverAddr = ipAddress;
		serverPort = port;

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
		serverAddr = ipAddress;
		serverPort = port;		
		this.isDebug = isDebug;
		chatSession = new ChatSession(this);

		this.gui = gui;	
	}

	/**
	 * Connect to server. 
	 * @param userName
	 * @throws IOException 
	 */
	public boolean register(String userName) {
		boolean succeeded = false;

		this.userName = userName;

		log("User name:" + userName);

		final Socket server;

		try {
			server = new Socket(serverAddr, serverPort);


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
				display("Server closed connection.");

			}		
			else if(msg.startsWith(ChatSystemConstants.MSG_REJ)){
				display("Server rejects registration request.");

			}
			else if(msg.startsWith(ChatSystemConstants.MSG_ACK)){
				display("Registration succeeded.");

				// Activate a heart-beat sender.
				new Thread(new HeartbeatSender(server, userName)).start();

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

				succeeded =  true;			
			}
			else{
				// Invalid message from server
				log("Received invalid message (" + msg + ").");
				display("Failed to register.");
			}

			server.close();

		} catch (UnknownHostException e) {
			display("Unknown server.");
			log(e.getMessage());
		} catch (IOException e) {
			display("Failed to connect to server.");
			log(e.getMessage());
		}

		return succeeded;
	}

	/**
	 * Connect to another client to initiate a chat session.
	 * @param client_ip
	 * @param port
	 * @return
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public boolean connect(String client_ip, int port){
		boolean isConnected = false;

		Socket other;
		try {
			other = new Socket(client_ip, port);

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
						display("Chat session is occupied");
						isConnected = false;
					}

					else {

						String otherName = msg.substring(ChatSystemConstants.MSG_ACK.length());
						
						// Occupy the chat session.
						this.chatSession.serve(other, otherName);

						display("Created a chat session with " + other);
			
						isConnected = true;
					}
				}
			}
			else if(msg.startsWith(ChatSystemConstants.MSG_REJ)){
				// Client on the other end rejects 
				display("Chat invitation is rejected.");
				log("Chat invitation is rejected by " + other);
				isConnected = false;
			}
			else{
				log("Received invalid message (" + msg + ").");
				display("Chat invitation is rejected.");
				isConnected = false;
			}		

		} catch (UnknownHostException e) {
			display("Unknown client address.");
			log(e.getMessage());
		} catch (IOException e) {
			display("Failed to connect to the client.");
			log(e.getMessage());
		}


		return isConnected;
	}


	/**
	 * Display msg on the client side.
	 * @param msg
	 */
	public void display(String msg){
		if( null != gui){
			gui.append(msg);
		}
		else{
			System.out.println(msg);
		}
	}

	public ChatSession getChatSession() {
		return chatSession;
	}

	public String getUserName(){
		return userName;
	}

	public ServerSocket getListener(){
		return listener;
	}

	public ChatClientGUI getGUI() {
		return gui;
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

		/**
		 *  Initialize chat client.
		 */
		ChatClient chatClient = null; 

		try {
			chatClient = new ChatClient(server_ip, server_port, is_debug);

		} catch (IOException e) {
			System.out.println("Failed to initialize client.");
			System.exit(-1);
		}


		final Scanner in = new Scanner(System.in);

		/**
		 * Register a user.
		 */
		while(true) {
			System.out.print("Enter user name:");

			String name = in.next(ChatSystemConstants.NAME_PATTERN);
			
			// Consume white space
			in.nextLine();


			if(chatClient.register(name)){
				break;
			}

		}

		/**
		 * Ask if user want to initiate a chat session.
		 */
		while (true) {
			System.out.print("Initiate a chat session? (y/n)");


			if(in.nextLine().equalsIgnoreCase("n")){
				System.out.println("Waiting for incoming chat invitation.\n");
				break;
			}

			System.out.print("Enter IP address and port of the user you want to contact (ip:port):\n");

			String addr = in.nextLine();

			int port_index = addr.indexOf(':');
			String client_ip_addr = addr.substring(0, port_index);
			int client_port = Integer.parseInt(addr.substring(port_index + 1));

			if(chatClient.connect(client_ip_addr, client_port)){
				// Success
				break;
			}

		}

		/**
		 * Waiting for chat invitation.
		 */
		// Spin an invitation handler in the main thread.
		new InvitationHandler(chatClient, chatClient.getListener()).run();



	}


}
