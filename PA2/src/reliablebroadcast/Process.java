package reliablebroadcast;

public class Process {
	String IP;
	int port ;
	String ID;
	public Node(String IP,int port,Sring ID) {
		this.IP = IP;
		this.port=port; 
		this.ID = ID;
		
	}
	public String getIP () {
		return IP ;
	}
	
	public int getPort () {
		return port ;
	}
	

	public String getID() {
		return ID;
	}
}
