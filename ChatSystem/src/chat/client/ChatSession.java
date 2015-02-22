/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A wrapper class that solves threads contest 
 * when multiple threads initiate/retrieve current chat session.
 * A client could  only have one active chat session.
 *
 */
public class ChatSession {

	/**
	 * If the chat session is occupied by a client on the other end.
	 */
	private boolean isOccupied;
	
	private final ChatClient client;
	
	private PrintWriter out;
	
	private final ExecutorService service;
	
	
	public ChatSession(final ChatClient client){
		isOccupied = false;
		out = null;
		this.client = client;
		service = Executors.newSingleThreadExecutor();
	}
	
	public boolean isOccupied(){
		return isOccupied;
	}
	
	/**
	 * A message delivered by the message receiver.
	 * @param msg
	 * 		msg from the client on the other end.
	 */
	public void receive(final String msg) {
		if(isOccupied()){
			client.display(msg);
		}
	}
	
	/**
	 * Send msg to the client on the other end.
	 * @param msg
	 * 		msg to be sent.
	 */
	public void send(final String msg) {
		if(isOccupied()){
			out.println(msg);
		}
	}
	
	public void serve(final Socket theOtherEnd) throws IOException{
		isOccupied = true;
		
		out = new PrintWriter(theOtherEnd.getOutputStream(), true);
		
		// Spawn a message receiver as worker thread.
		service.execute(new MessageReceiver(this, theOtherEnd));
	}
	
	public void close(){
		isOccupied = false;
		out = null;
		service.shutdownNow();
	}
	
	
}
