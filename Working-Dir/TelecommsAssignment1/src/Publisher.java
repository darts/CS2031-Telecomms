import java.net.DatagramSocket;
import java.net.SocketException;

public class Publisher {

	private static final int DEFAULT_SRC_PORT = 50000;
	private static final int DEFAULT_DST_PORT = 50001;
	private static final String DEFAULT_DST_NODE = "subscriber";
	
	public static void main(String[] args) {
		DatagramSocket theSocket = null;
		try {
			theSocket = new DatagramSocket(DEFAULT_SRC_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new UserInterface(DEFAULT_DST_NODE, DEFAULT_DST_PORT, theSocket);
	}

}
