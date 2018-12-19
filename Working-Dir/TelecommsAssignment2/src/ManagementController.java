import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementController extends CommPoint {
	public static List<String> HELLO_KEY = Arrays.asList("H", "H");
	private Map<List<String>, Frame> MGMTMap;
	private Router parent;
	private InetSocketAddress MGMTAddr = new InetSocketAddress(Controller.ID, Controller.COMM_PORT);

	public ManagementController(int portNum, Router parent) throws SocketException {
		super(new DatagramSocket(portNum));
		this.parent = parent;
		MGMTMap = new HashMap<List<String>, Frame>();
		this.start();
	}

	public void sendHELLO() {
		System.out.println("Sending HELLO packet...");
		Packet toSend = new Packet(MGMTAddr, Packet.HELLO, parent.rtNum);
		MGMTMap.put(HELLO_KEY, new Frame(toSend, socket));
		MGMTMap.get(HELLO_KEY).send();
	}

	public void HELLOReceived(DatagramPacket thePacket) {
		System.out.println("HELLO Received... Connected To Controller.");
		MGMTMap.get(HELLO_KEY).cancel();
	}

	public void getHELP(String senderID, String targetID) {
		System.out.println("Asking Controller for HELP...");
		String[] helpData = { targetID, senderID };
		List<String> modKey = Arrays.asList(helpData[0], helpData[1]);
		MGMTMap.put(modKey, new Frame(new Packet(MGMTAddr, Packet.HELP, helpData), socket));
		MGMTMap.get(modKey).send();
	}

	public void UPDATEReceived(DatagramPacket thePacket) {
		System.out.println("UPDATE received... Adding to routing table");
		String[] upDateData = Packet.getTgtInfo(thePacket);
		String[] key = { upDateData[Packet.TGT_ID], upDateData[Packet.SENDER_ID] };
		System.out.println("Now Routing packets from:" + upDateData[Packet.TGT_ID] + " with dst:"
				+ upDateData[Packet.SENDER_ID] + " to:" + upDateData[Packet.NEXT_ADDR]);
		List<String> modKey = Arrays.asList(key[0], key[1]);
		try {
			MGMTMap.get(modKey).cancel();
			MGMTMap.remove(modKey);
		} catch (Exception e) {
			System.out.println("Did not request help");
		}
		parent.updateTable(key, upDateData[Packet.NEXT_ADDR]);
		sendACK(upDateData);
		parent.sendWaiting(key);
	}

	private void sendACK(String[] whatToAck) {
		System.out.println("Sending ACK...");
		whatToAck[Packet.ROUTER_ID_LOC] = parent.ID;
		Frame ACKFrame = new Frame(new Packet(MGMTAddr, Packet.ACK, whatToAck), socket);
		ACKFrame.send();
	}

	public void ACKReceived(DatagramPacket thePacket) {
		System.err.println("ACK received! Should not happen!");
	}

	public boolean DATAReceived(DatagramPacket thePacket) {
		System.err.println("DATA received! Should not happen!");
		return false;
	}

	public void HELPReceived(DatagramPacket thePacket) {
		System.err.println("HELP REQUEST received! Should not happen!");
	}

}
