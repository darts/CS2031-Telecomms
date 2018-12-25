import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementController extends CommPoint {
	public static List<String> HELLO_KEY = Arrays.asList("H", "H"); //the key of hello packet
	private Map<List<String>, Frame> MGMTMap; //map of active packets
	private Router parent; //what router does this belong to?
	private InetSocketAddress MGMTAddr = new InetSocketAddress(Controller.ID, Controller.COMM_PORT);//address of the controller

	public ManagementController(int portNum, Router parent) throws SocketException {
		super(new DatagramSocket(portNum));//create a new CommPoint on this port
		this.parent = parent;
		MGMTMap = new HashMap<List<String>, Frame>();//init. packet map
		this.start(); //start listening to the medium
	}

	//send a hello packet to let the controller know we are online
	public void sendHELLO() {
		System.out.println("Sending HELLO packet...");
		Packet toSend = new Packet(MGMTAddr, Packet.HELLO, parent.rtNum);
		MGMTMap.put(HELLO_KEY, new Frame(toSend, socket));//store the frame
		MGMTMap.get(HELLO_KEY).send();//send the frame
	}

	//controller has acknowledged us being online
	public void HELLOReceived(DatagramPacket thePacket) {
		System.out.println("HELLO Received... Connected To Controller.");
		MGMTMap.get(HELLO_KEY).cancel();//cancel timeout
	}

	//ask controller for routing information
	public void getHELP(String senderID, String targetID) {
		System.out.println("Asking Controller for HELP...");
		String[] helpData = { targetID, senderID };
		List<String> modKey = Arrays.asList(helpData[0], helpData[1]);
		MGMTMap.put(modKey, new Frame(new Packet(MGMTAddr, Packet.HELP, helpData), socket));//add frame to map
		MGMTMap.get(modKey).send();//send the packet
	}
	
	//controller has sent update, add to routing table
	public void UPDATEReceived(DatagramPacket thePacket) {
		System.out.println("UPDATE received... Adding to routing table");
		String[] upDateData = Packet.getTgtInfo(thePacket);
		String[] key = { upDateData[Packet.SENDER_ID], upDateData[Packet.TGT_ID] };
		System.out.println("Now Routing packets from:" + upDateData[Packet.TGT_ID] + " with dst:"
				+ upDateData[Packet.SENDER_ID] + " to:" + upDateData[Packet.NEXT_ADDR]);
		List<String> modKey = Arrays.asList(key[1], key[0]);
		try {//check to see if we requested this help
			MGMTMap.get(modKey).cancel();
			MGMTMap.remove(modKey);
		} catch (Exception e) {
			System.out.println("Did not request help");
		}
		parent.updateTable(key, upDateData[Packet.NEXT_ADDR]);
		sendACK(upDateData);
		parent.sendWaiting(key);
	}

	//Acknowledge update
	private void sendACK(String[] whatToAck) {
		System.out.println("Sending ACK...");
		whatToAck[Packet.ROUTER_ID_LOC] = parent.ID;
		Frame ACKFrame = new Frame(new Packet(MGMTAddr, Packet.ACK, whatToAck), socket);
		ACKFrame.send();
		ACKFrame.cancel();
	}

	//Should not receive this type of packet
	public void ACKReceived(DatagramPacket thePacket) {
		System.err.println("ACK received! Should not happen!");
	}

	//Should not receive this type of packet
	public boolean DATAReceived(DatagramPacket thePacket) {
		System.err.println("DATA received! Should not happen!");
		return false;
	}

	//Should not receive this type of packet
	public void HELPReceived(DatagramPacket thePacket) {
		System.err.println("HELP REQUEST received! Should not happen!");
	}

}
