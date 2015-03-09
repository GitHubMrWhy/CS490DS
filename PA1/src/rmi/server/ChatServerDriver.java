package rmi.server;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.Naming;

import rmi.client.ChatClient;

public class ChatServerDriver {

		public static void  main (String[] args) throws RemoteException,MalformedURLException{
			
			Naming.rebind("RMIChatServer", new ChatServer());
			System.out.println("Server Run!");
			new Thread(new ChatServer()).start();
		}
	
}
