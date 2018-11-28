import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class ManagementController extends CommPoint{
	public static String[] HELLO_KEY = {"H", "H"};
	private Map<String[], Frame> MGMTMap;
	private Router parent;
	private InetSocketAddress MGMTAddr = new InetSocketAddress(Controller.ID, Controller.COMM_PORT);
	public ManagementController(int portNum, Router parent) throws SocketException {
		super(new DatagramSocket(portNum));
		this.parent = parent;
		MGMTMap = new HashMap<String[], Frame>();
	}
	
	public void sendHELLO() {
		System.out.println("Sending HELLO packet...");
		MGMTMap.put(HELLO_KEY, new Frame(new Packet(MGMTAddr, Packet.HELLO), socket));
		MGMTMap.get(HELLO_KEY).send();
	}

	public void HELLOReceived() {
		System.out.println("HELLO Received... Connected To Controller.");
		MGMTMap.get(HELLO_KEY).cancel();
	}
	
	public void getHELP(String senderID, String targetID) {
		String[] helpData = {senderID, targetID};
		MGMTMap.put(helpData, new Frame(new Packet(MGMTAddr, Packet.HELP, helpData), socket));
		MGMTMap.get(helpData).send();
	}
	
	public void UPDATEReceived(DatagramPacket thePacket) {
		String[] upDateData = Packet.getTgtInfo(thePacket);
		String[] key = {upDateData[Packet.SENDER_ID], upDateData[Packet.TGT_ID]};
		try {
			MGMTMap.get(key).cancel();
			MGMTMap.remove(key);
		} catch (Exception e) {
		}
		parent.updateTable(key, upDateData[Packet.NEXT_ADDR]);
		sendACK(upDateData);
		parent.sendWaiting(key);
	}
	
	private void sendACK(String[] whatToAck) {
		whatToAck[Packet.ROUTER_ID_LOC] = parent.ID;
		Frame ACKFrame = new Frame(new Packet(MGMTAddr, Packet.ACK, whatToAck), socket);
		ACKFrame.send();
	}
	
	public void ACKReceived(DatagramPacket thePacket) {
	}

	
	public boolean DATAReceived(DatagramPacket thePacket) {
		return false;
	}
	
	public void HELPReceived() {		
	}

}
