package rmi.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import chat.user.group.User;
import rmi.client.*;



public class ChatServer extends UnicastRemoteObject implements ChatServerIF,Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean flag;
	private static final int HEARTBEAT_RATE = 10*1000;


	private static ConcurrentHashMap<String,ChatClientIF> ClientList=new ConcurrentHashMap<String,ChatClientIF>();

	protected ChatServer() throws RemoteException {
		//chatClients = new ArrayList<ChatClientIF>();
//		this.ClientList = new ConcurrentHashMap<String,ChatClientIF>();
	}

	public  void registerChatClient(ChatClientIF chatClient)
			throws RemoteException {
		//System.out.println("registering name: "+chatClient.getUsername());
		if (this.ClientList.containsKey(chatClient.getUsername())) {
			System.out.println("user "+chatClient.getUsername()+ " exist!");
			chatClient.retrieveMessage("user "+chatClient.getUsername()+ " exist!");
			//do something with value
		}else{

			this.ClientList.put(chatClient.getUsername(),chatClient);
			System.out.println("registering "+chatClient.getUsername()+" successful");
			chatClient.retrieveMessage("registering "+chatClient.getUsername()+" successful");
		}
	}

	public  void boardcastMessage(String message) throws RemoteException {
		int i=0;
		//while (i< chatClients.size()){
		//chatClients.get(i++).retrieveMessage(message);
		//}
	}

	public  void heartbeat(String chatClientName) throws RemoteException {
		// TODO Auto-generated method stub

	}
	
	public  void listAllUser(ChatClientIF chatClient) throws RemoteException {
		System.out.println(chatClient.getUsername()+" send request to list all users");
		for (String name: this.ClientList.keySet()) {
			//System.out.println(name);

			chatClient.retrieveMessage(name);

			// do stuff
		}

	}

	public  boolean containUser(String name) throws RemoteException {
		if(this.ClientList.containsKey(name)){
			System.out.println("user "+name+ " exist!");
			return true;
		}else{
			return false;
		}
	}

	public  void chatting(ChatClientIF userA, String userNameB, String message)
			throws RemoteException {
		// TODO Auto-generated method stub

		ChatClientIF userB = this.ClientList.get(userNameB);

		userB.retrieveMessage(userA.getUsername()+":"+message);

	}

	public  boolean inSession(String name) throws RemoteException {
		// TODO Auto-generated method stub
		ChatClientIF user = this.ClientList.get(name);
		if(user.getChattingUserName()==null){

			return false;
		}else{
			return true;
		}
	}

	public  void setSessionWith(ChatClientIF userA, String userNameB)
			throws RemoteException {
		ChatClientIF userB = this.ClientList.get(userNameB);
		userB.setChattingUserName(userA.getUsername());
		System.out.println(userA.getUsername()+ " start to chat with "+userNameB);
		userB.retrieveMessage("You are now chatting with "+userA.getUsername());


	}

	public  void run() {
		
		
		
		
		while(true){		
			try {
				System.out.println("Counting");


				Iterator<Entry<String, ChatClientIF>> iter = this.ClientList.entrySet().iterator(); 
				while (iter.hasNext()) {
					Map.Entry<String, ChatClientIF> entry = iter.next();

					System.out.println(entry.getKey());
					ChatClientIF cc=entry.getValue();
					System.out.println(cc.lastHeartBeat());
				}


				System.out.println(this.ClientList.size());
				
				Thread.sleep(HEARTBEAT_RATE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}





}
