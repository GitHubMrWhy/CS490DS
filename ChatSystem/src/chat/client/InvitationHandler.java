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

import chat.constant.ChatSystemConstants;

/**
 * Receive chat invitation from other clients.
 * If client is in a chat session, reject incoming invitations.
 *
 */
public class InvitationHandler implements Runnable {

	private ChatClient client;
	private ServerSocket listener;
	
	public InvitationHandler(final ChatClient client, ServerSocket listener){
		this.client = client;
		this.listener = listener;
		
	}
	
	public void run(){
		
		ChatSession session = client.getChatSession();
		
		while(true){
			try {
				Socket other =  listener.accept();
				
				final BufferedReader in = new BufferedReader(
						new InputStreamReader(other.getInputStream()));

				final PrintWriter out = new PrintWriter(other.getOutputStream(), true);
				
				String userName = in.readLine();
				
				synchronized(session){
					
					if(session.isOccupied()){
						
						// Refuse the invitation.
						out.println(ChatSystemConstants.MSG_REJ);
						
						// Close the connection.
						other.close();
					
					}else{
						// Accept the invitation.
						out.println(ChatSystemConstants.MSG_ACK + client.getUserName());
						client.display("Start chat session with "+ userName);			
						session.serve(other, userName);
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			
		}
	}
	
}
