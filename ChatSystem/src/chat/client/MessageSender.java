/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.util.Scanner;

public class MessageSender implements Runnable{

	private final ChatSession session;
		
	public MessageSender(final ChatSession session){
		this.session = session;
	}

	public void run() {
		
		Scanner input = new Scanner(System.in);
		
		while(true){
			String msg = input.nextLine();
			if(!session.send(msg)){		
				// Current session is closed;
				break;
			}	
		}	
	}	
}
