import java.net.DatagramPacket;
import java.util.Map;

public class Router extends CommPoint {
	private int ID;// this router's unique ID
	private Map<Integer, Integer> sendMap; // a map of where to send packets

	public void ACKReceived() {//Should never receive an ACK
		System.err.println("ACK Recieved... Ignoring.");
	}

	public void HELLOReceived() {
		this.theSender.ackRecieved();
	}

	public void DATAReceived(DatagramPacket thePacket) {
		Integer next = lookUpNext(Packet.getTgt(thePacket));
		if(next == null) {
			//ask for help
		}
	}

	public void UPDATEReceived() {

	}

	public void HELPReceived() {

	}
	
	private Integer lookUpNext(Integer tgt) {
		return sendMap.get(tgt);
	}
	
	private void updateTable(Integer dst, Integer next) {
		sendMap.put(dst, next);
	}

}
