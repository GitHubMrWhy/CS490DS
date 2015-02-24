package rmi.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import rmi.server.ChatServerIF;
import chat.constant.ChatSystemConstants;

public class DummyClient {


	/**
	 * program runs with commands -n=number_of_user -ip=(host name|ip address) -port=number [-prefix=string] [-debug]
	 * @param args
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		boolean is_debug = false;
		int server_port = ChatSystemConstants.DEFAULT_PORT;
		int n_user = 1000;
		String server_ip = "";
		String name_prefix = "";
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

				String chatServerURL = new StringBuilder("rmi://").append(server_ip).append("/RMIChatServer").toString();
				
				//String chatServerURL = "rmi://localhost/RMIChatServer";
				ChatServerIF chatServer = (ChatServerIF)Naming.lookup(chatServerURL);
				
		
		/**
		 * Register users and measure through put and latency.
		 */
		double latency = 0;

		long n_response = 0;

		long n_success = 0;

		long start_t = System.currentTimeMillis();

	
		for(int i=0; i < n_user; i++){
			long last_t = System.currentTimeMillis();

			new ChatClient(name_prefix+i,chatServer);
				n_success++;
				n_response ++;
			

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

