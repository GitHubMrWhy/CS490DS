/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import chat.constant.ChatSystemConstants;
import chat.user.group.User;
import chat.user.group.UserGroup;

/**
 * Handler for the in-coming messages from client.
 */
public class MessageHandler {
	
	private boolean isDebug;
	
	private void log(String message){
		if(isDebug){
			System.out.println(message);
		}
	}
	
	public void handle(Socket client, UserGroup userGroup, boolean isDebug) throws IOException{
		
		this.isDebug = isDebug;
		
		final BufferedReader in = new BufferedReader(
				new InputStreamReader(client.getInputStream()));
		
		final PrintWriter out = new PrintWriter(client.getOutputStream());
		
		String msg;
		
		while(null != (msg = in.readLine())) {
			
			if(msg.startsWith(ChatSystemConstants.MSG_REG)){
				
				log("Received registration request.");
				
				final int port_sindex = msg.indexOf(':');
				final String name = msg.substring(ChatSystemConstants.MSG_REG.length(), port_sindex);
				final int client_port = Integer.parseInt(msg.substring(port_sindex+1));
				
				// Check if the user already exists
				if(userGroup.contains(name)){
					out.println(ChatSystemConstants.MSG_REJ);
				}
				else{
					final User new_user = 
							new User(name, 
									client.getInetAddress().getHostAddress(),
									client_port);
					userGroup.add(new_user);
					
					out.println(ChatSystemConstants.MSG_ACK);
					log("Added new user:" + new_user.toString());
				}
			}
			else if(msg.startsWith(ChatSystemConstants.MSG_HBT)){
				final String name = msg.substring(ChatSystemConstants.MSG_HBT.length());
				
				log("Received heart beat from "+name);
				
				final User alive_user = userGroup.get(name);
				
				if( null != alive_user){
					alive_user.setLastHeartBeat(System.currentTimeMillis());
				}
				
			}
			else if(msg.startsWith(ChatSystemConstants.MSG_GET)){
				
				log("Received GET request from client");
				
				final StringBuilder sb = new StringBuilder();
				for(User usr : userGroup.getActiveUsers()){
					sb.append(usr);
					sb.append('\n');
				}
				
				out.print(ChatSystemConstants.MSG_USG);
				out.print(sb);
			
				
			}
			
		}
	
		
		
	}
}
