package rmi.client;

import java.rmi.*;

public interface ChatClientIF extends Remote{
	
	public void setOther(ChatClientIF other)throws RemoteException;
	void retrieveMessage(String message) throws RemoteException;
	void closeChatServer() throws RemoteException;
	public String getUsername()throws RemoteException;
	public String getIp()throws RemoteException;
	public String getPort()throws RemoteException; 
	public String getChattingUserName() throws RemoteException;
	public void setLastHeartBeat() throws RemoteException;
	public long lastHeartBeat() throws RemoteException;
	public void setChattingUserName(String name) throws RemoteException;
}
