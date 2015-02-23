/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * A worker thread associated to an active chat session.
 *
 */
public class MessageReceiver implements Runnable {

	private final BufferedReader in;
	
	private ChatSession session;
	
	public MessageReceiver(final ChatSession session, final Socket sender) throws IOException{
		this.session = session;
		in = new BufferedReader(
				new InputStreamReader(sender.getInputStream()));
		
	}
	public void run() {
		String msg;
		try {
			while(null != (msg = in.readLine())){
				session.receive(msg);
			}
		} catch (IOException e) {	
			
		}	
		
		synchronized(session){
			session.close();
		}
	}
}
