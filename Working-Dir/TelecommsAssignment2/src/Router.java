import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Map;

public class Router extends CommPoint {
	private static int DEFAULT_PORT = 50000;
	private static int MGMT_PORT = 50001;
	private int ID;// this router's unique ID
	private Map<Integer, Integer> sendMap; // a map of where to send packets

	public void ACKReceived() {//Should never receive an ACK
		System.err.println("ACK Recieved... Ignoring.");
	}

	public void HELLOReceived() {
		this.theSender.ackRecieved();
	}

	public void DATAReceived(DatagramPacket thePacket) {
		Integer[] tgtData = Packet.getTgtInfo(thePacket);
		Integer next = lookUpNext(tgtData[Packet.TGT_ID]);
		if(next == null) {
			//ask for help
		}
		Integer tgtPort;
		if(next.equals(tgtData[Packet.TGT_ID]))
			tgtPort = tgtData[Packet.TGT_PORT];
		else
			tgtPort = Router.DEFAULT_PORT;
		thePacket.setSocketAddress(new InetSocketAddress(next.toString(), tgtPort));
		forward(thePacket);
	}

	public void UPDATEReceived(DatagramPacket thePacket) {
		
	}

	public void HELPReceived() {

	}
	
	private Integer lookUpNext(Integer tgt) {
		return sendMap.get(tgt);
	}
	
	private void updateTable(Integer dst, Integer next) {
		sendMap.put(dst, next);
	}
	
	private void forward(DatagramPacket thePacket) {
		try {
			socket.send(thePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
