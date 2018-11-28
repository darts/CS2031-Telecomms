import java.net.DatagramPacket;

public class Controller extends CommPoint{

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static String ID = "CONTROLLER";
	public static int COMM_PORT = 50505;

	public void ACKReceived(DatagramPacket thePacket) {
		
	}

	public void HELLOReceived() {
		
	}
	
	public boolean DATAReceived(DatagramPacket thePacket) {//Controller should not receive DATA packet
		return false;
	}

	public void UPDATEReceived(DatagramPacket thePacket) {
		
	}
	@Override
	public void HELPReceived() {
		
	}
	
	

}
