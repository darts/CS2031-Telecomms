import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Endpoint extends CommPoint{
	public static String PREFIX = "E";
	public static int DEFAULT_PORT = 50000;
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public Endpoint() throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
	}
	
	public void ACKReceived(DatagramPacket thePacket) {
		
	}
	
	public void HELLOReceived(DatagramPacket thePacket) {
		
	}
	
	public boolean DATAReceived(DatagramPacket thePacket) {
		return false;
	}
	
	public void UPDATEReceived(DatagramPacket thePacket) {
		
	}
	
	public void HELPReceived(DatagramPacket thePacket) {
		
	}
	
	private class UserInterface{
		
	}

}
