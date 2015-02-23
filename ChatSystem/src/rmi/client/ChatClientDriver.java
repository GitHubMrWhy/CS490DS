package rmi.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import rmi.server.ChatServerIF;
public class ChatClientDriver {
	
	
	public static void main(String[] args) throws MalformedURLException,RemoteException,NotBoundException{
		
		String chatServerURL = "rmi://localhost/RMIChatServer";
		ChatServerIF chatServer = (ChatServerIF)Naming.lookup(chatServerURL);
		
		Scanner s=new Scanner(System.in);
		boolean check=true;
		
		while(check){
    	String input=s.nextLine().trim();
    
    	if(input.startsWith("REG_")){
    		String name = input.substring("REG_".length());
    		
    		if(!chatServer.containUser(name)){
    			//System.out.println("Creating new user");
    			try {
				
    				new Thread(new ChatClient(name,chatServer)).start();
    			} catch (RemoteException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			check = false;
    			
    		}else{
    			System.out.println("user "+name+ " exist!");
    		}
			
		
		}else{
			
			System.out.println("invalid input");
		}
    	
    	}
		
		
	}
	

}
