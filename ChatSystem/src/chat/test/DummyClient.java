package chat.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import chat.constant.ChatSystemConstants;

public class DummyClient {

	private final String serverAddr;

	private final int serverPort;

	private final boolean isDebug;

	private void log(String message){
		if(isDebug){
			System.out.print("----");
			System.out.println(message);
		}
	}
	
	public DummyClient(String ipAddress, int port, boolean isDebug) throws IOException{
		this.isDebug = isDebug;
		serverAddr = ipAddress;
		serverPort = port;

	}

	public boolean register(final String name){
		boolean succeeded = false;

		Socket server;
		try {
			server = new Socket(serverAddr, serverPort);

			final BufferedReader in = new BufferedReader(
					new InputStreamReader(server.getInputStream()));

			/**
			 * Set writer to be auto-flushed!
			 */
			final PrintWriter out = new PrintWriter(server.getOutputStream(), true);


			String reg_request = ChatSystemConstants.MSG_REG + name + ":0";

			out.println(reg_request);

			String msg = in.readLine();

			if (msg == null){

			}		
			else if(msg.startsWith(ChatSystemConstants.MSG_ACK)){

				// Sent GET request to obtain active user list
				out.println(ChatSystemConstants.MSG_GET);
				log("Registered:" + name);
				succeeded = true;
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return succeeded;		

	}


	/**
	 * program runs with commands -n=number_of_user -ip=(host name|ip address) -port=number [-prefix=string] [-debug]
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		boolean is_debug = false;
		int server_port = ChatSystemConstants.DEFAULT_PORT;
		int n_user = 1000;
		String server_ip = "";
		String name_prefix = "";

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
			else if(command.startsWith("-prefix=")){
				name_prefix = command.substring(8);
			}
			else if(command.startsWith("-n=")){
				n_user = Integer.parseInt(command.substring(3));
			}
		}

		/**
		 *  Initialize chat client.
		 */
		DummyClient dummyClient = null; 

		try {
			dummyClient = new DummyClient(server_ip, server_port, is_debug);
			
			// Activate a dummy heartbeat sender
			new Thread(new DummyHeartbeatSender(server_ip, server_port, name_prefix, n_user)).start();
			

		} catch (IOException e) {
			System.out.println("Failed to initialize client.");
			System.exit(-1);
		}

		/**
		 * Register users and measure through put and latency.
		 */
		double latency = 0;

		long n_response = 0;

		long n_success = 0;

		long start_t = System.currentTimeMillis();

	
		for(int i=0; i < n_user; i++){
			long last_t = System.currentTimeMillis();

			if(dummyClient.register(name_prefix + i)){
				n_success++;
				n_response ++;
			}
			else{
				n_response ++;
			}

			long curr_t = System.currentTimeMillis();
			if(latency ==0){
				latency = curr_t - last_t;
			}
			else{
				latency = (latency + curr_t - last_t)/2;
			}
		}

		long total_t = System.currentTimeMillis() - start_t;

		System.out.println("Latency=" + latency);
		System.out.println("Response=" + n_response);
		System.out.println("Success=" + n_success);
		System.out.println("Time Elapsed (in sec)=" + total_t/1000);
		System.out.println("Throughput=" + n_response * 1.0/(total_t/1000));


	}

}
