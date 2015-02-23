package chat.gui;

import javax.swing.*;

import chat.client.ChatClient;
import chat.client.InvitationHandler;
import chat.constant.ChatSystemConstants;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/*
 * The Client with its GUI
 */
public class ChatClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// will first hold "Username:", later on "Enter message"
	private JLabel label;
	// to hold the Username and later on the messages
	private JTextField tf;

	// to hold the client address an the port number
	private JTextField tfClient, tfPort;
	// to Logout and get the list of the users
	private JButton login, logout, connect;
	// for the chat room
	private JTextArea ta;
	// if it is for connection
	private boolean connected;
	// the Client object
	private ChatClient client;
	// the default port number
	private int defaultPort;
	private String defaultHost;

	// Constructor connection receiving a socket number
	ChatClientGUI(String host, int port, boolean isDebug) {

		super("Chat Client");
		defaultPort = port;
		defaultHost = host;

		// The NorthPanel with:
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		// the server name anmd the port number
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// the two JTextField with default value for server address and port number
		tfClient = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Client Address:  "));
		serverAndPort.add(tfClient);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		// adds the Server an port field to the GUI
		northPanel.add(serverAndPort);

		// the Label and the TextField
		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		// The CenterPanel which is the chat room
		ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);		// you have to login before being able to logout
		connect = new JButton("Connect to");
		connect.addActionListener(this);
		connect.setEnabled(false);		// you have to login before being able to Who is in

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(connect);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

		// try creating a new Client with GUI
		try {
			client = new ChatClient(defaultHost, defaultPort, isDebug, this);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// called by the Client to append text in the TextArea 
	public void append(String str) {
		ta.append(str);
		ta.append("\n");
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	// called by the GUI is the connection failed
	// we reset our buttons, label, textfield
	 void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		connect.setEnabled(false);
		label.setText("Enter your username below");
		tf.setText("Anonymous");
		// reset port number and host name as a construction time
		tfPort.setText("");
		tfClient.setText("");
		// let the user change them
		tfClient.setEditable(true);
		tfPort.setEditable(true);
		// don't react to a <CR> after the username
		tf.removeActionListener(this);
		connected = false;
	}
	
	public void closeConnection(){
		connected = false;
		label.setText("Enter the client host and port above");
	}

	/*
	 * Button or JTextField clicked
	 */
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		if(o == logout) {
			System.exit(0);
		}

		// ok it is coming from the JTextField
		if(connected) {
			// just have to send the message	
			client.getChatSession().send(tf.getText());
			tf.setText("");
			return;
		}

		if(o == connect){

			tfClient.setEditable(false);
			tfPort.setEditable(false);
			// empty serverAddress ignore it
			String clientHost = tfClient.getText().trim();

			if(clientHost.length() == 0)
				return;

			// empty or invalid port numer, ignore it
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   // nothing I can do if port number is not valid
			}

			if(!client.connect(clientHost, port)){
				return;
			}
			
			tf.setText("");
			label.setText("Enter your message below");
			connected = true;

		}

		if(o == login) {
			// ok it is a connection request
			String username = tf.getText().trim();
			// empty username ignore it
			if(username.length() == 0)
				return;


			// test if we can start the Client
			if(!client.register(username))
				return;

			tf.setText("");
			label.setText("Enter the client host and port above.");

			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			logout.setEnabled(true);

			connect.setEnabled(true);
			// disable the Server and Port JTextField

			// Action listener for when the user enter a message
			tf.addActionListener(this);

			// Spin an invitation handler in the main thread.
			new Thread(new InvitationHandler(client, client.getListener())).start();
		}
	}


	public void setConnected() {
		tf.setText("");
		label.setText("Enter your message below");
		connected = true;
	}

	/**
	 * program runs with commands -ip=(host name|ip address) -port=number [-debug]
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		boolean is_debug = false;
		int server_port = ChatSystemConstants.DEFAULT_PORT;
		String server_ip = "localhost";

		/**
		 * Parse user commands.
		 */
		// Use set to handle commands entered without order.
		Set<String> commands = new HashSet<String>();
		for(int i=0; i<args.length; i++){
			commands.add(args[i]);
		}


		for(String command : commands){
			if(command.startsWith("-debug")){
				is_debug = true;
			}
			else if(command.startsWith("-port=")){
				server_port = Integer.parseInt(command.substring(6));
			}
			else if(command.startsWith("-ip=")){
				server_ip = command.substring(4);
			}
		}

		new ChatClientGUI(server_ip, server_port, is_debug);


	}

}

