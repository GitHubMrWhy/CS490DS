package rmi.client;


import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import chat.constant.ChatSystemConstants;
import rmi.server.*;



public class ChatClient extends UnicastRemoteObject implements ChatClientIF, Runnable{
	private static ChatServerIF chatServer;
	private String name = null;
	private String chattingUserName = null;
	private static long lastHeartBeat=System.currentTimeMillis();
	private static final long HEARTBEAT_RATE = 10*1000;

	public ChatClient(String name,ChatServerIF chatServer) throws RemoteException {
		this.name =name;
		this.chatServer = chatServer;
		chatServer.registerChatClient(this);
		new SendHeartBeat(this);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void retrieveMessage(String message) throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println(message);

	}


	public void run() {
		Scanner s=new Scanner(System.in);
		try {
			new SendHeartBeat(this);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		while (true){
			
			String input=null;
			input=s.nextLine().trim();	
			if(chattingUserName!=null){
				//if chat session estabilish start chatting	
							
					System.out.println("Me: "+input);
					try {
						chatServer.chatting(this,chattingUserName,input);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}							
			}else{
				

				if(input.startsWith("GET_USER")){
					try {
						chatServer.listAllUser(this);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}else if(input.startsWith("CHAT_")) {
					String userNameB = input.substring("CHAT_".length());
					try {
						if(!userNameB.equals(this.name)){
							if(chatServer.containUser(userNameB)){
								if(!chatServer.inSession(userNameB)){
									System.out.println("Chat session with "+ userNameB +" established");
									chatServer.setSessionWith(this,userNameB);
									chattingUserName=userNameB;
									
								}else{
									System.out.println(userNameB+" is chatting with others:Chat session fail to establish");
								}
							}else{
								System.out.println(userNameB+" Not Exist:Chat session fail to establish");

							}
						}else{
							System.out.println("You can not chat with yourself:Chat session fail to establish");
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}else{

					System.out.println("invalid input");
				}
				
				
				
				
			}
		}

	}

	public Runnable start() {
		// TODO Auto-generated method stub
		return null;
	}
	public void closeChatServer() throws RemoteException {
		// TODO Auto-generated method stub

	}
	public String getIp() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	public String getPort() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	public String getUsername() throws RemoteException {
		return name;
	}


	public String getChattingUserName() throws RemoteException {
		// TODO Auto-generated method stub
		return chattingUserName;
	}


	public void setChattingUserName(String name) throws RemoteException {
		// TODO Auto-generated method stub
		chattingUserName=name;
	}

	public long lastHeartBeat() throws RemoteException {
		// TODO Auto-generated method stub
		return this.lastHeartBeat;
		
	}


	public void setLastHeartBeat() throws RemoteException {
		// TODO Auto-generated method stub
		this.lastHeartBeat=this.lastHeartBeat+HEARTBEAT_RATE;
	}



}
