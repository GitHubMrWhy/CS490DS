package chat.constant;

/**
 * Define constants used across the chat system.
 */
public class ChatSystemConstants {
	
	// Default port for server 
	public static int DEFAULT_PORT = 8888;
	
	// Heart beat rate in second used to maintain user group.
	public static int HEARTBEAT_RATE = 600;
	
	/**
	 * Each message communicated between server and client
	 * must be preceded by a 3-byte command.
	 * 
	 * Commands determine what action to be taken. 
	 */
	
	/**
	 * Prefix of acknowledgment sent by server to confirm user registration.
	 */
	public static String MSG_ACK = "ACK";
	
	/**
	 * Prefix of heart beat sent by client to confirm liveness.
	 */
	public static String MSG_HBT = "HBT";

	/**
	 * Prefix of Registration request sent by client.
	 */
	public static String MSG_REG = "REG";
	
	/**
	 * Prefix of Get command sent by client to retrieve user group information.
	 */
	public static String MSG_GET = "GET";
	
	/**
	 * Prefix of Rejecting client request sent by server
	 */
	public static String MSG_REJ = "REJ";
	
	/**
	 * Prefix of user group information sent by server. 
	 * This message is special since it contains multiple lines.
	 * Here we assume the server won't send other message after
	 * sending group information.
	 */
	public static String MSG_USG = "USG";
	
	
	
	
	
}
