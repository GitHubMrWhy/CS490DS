package rmi.server;

import java.rmi.*;

import rmi.client.*;

public interface ChatServerIF extends Remote {
	
	void registerChatClient(ChatClientIF chatClient) throws RemoteException;
	void boardcastMessage(String message) throws RemoteException;
	void heartbeat(String chatClientName) throws RemoteException;
	void listAllUser(ChatClientIF chatClient) throws RemoteException;
	
	void chatting(ChatClientIF userA,String userNameB,String message) throws RemoteException;
	boolean containUser(String name) throws RemoteException;
	boolean inSession(String name) throws RemoteException;
	void setSessionWith(ChatClientIF userA,String userNameB) throws RemoteException;
	
}
 