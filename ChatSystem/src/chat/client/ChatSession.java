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

import chat.gui.ChatClientGUI;

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

	private final ExecutorService receiver;

	private final ExecutorService sender;

	private String otherName;


	public ChatSession(final ChatClient client){
		isOccupied = false;
		out = null;
		this.client = client;
		receiver = Executors.newSingleThreadExecutor();
		sender = Executors.newSingleThreadExecutor();
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
			client.display(otherName + ":" + msg);
		}
	}

	/**
	 * Send msg to the client on the other end.
	 * @param msg
	 * 		msg to be sent.
	 */
	public boolean send(final String msg) {
		if(isOccupied()){
			out.println(msg);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Start to chat with the client on the other end.
	 * @param theOtherEnd
	 * @throws IOException
	 */
	public void serve(final Socket theOtherEnd, final String name) throws IOException{

		out = new PrintWriter(theOtherEnd.getOutputStream(), true);

		// Spawn a message receiver as worker thread.
		receiver.execute(new MessageReceiver(this, theOtherEnd));

		if(client.getGUI() == null){
			// Spawn a message sender that listens user input and send message
			sender.execute(new MessageSender(this));
		}

		otherName = name;

		isOccupied = true;

	}

	public void close(){
		client.display("Chat session ends.");
		isOccupied = false;
		
		ChatClientGUI gui = client.getGUI();
	
		if(gui != null){
			gui.closeConnection();
		}
		
		out = null;
		otherName = null;
	}


}
