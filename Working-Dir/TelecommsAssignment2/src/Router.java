import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router extends CommPoint {
	public static String PREFIX = "R";
	
	public static void main(String[] args) {
		try {
			new Router(Integer.parseInt(args[0]));//create a new router with the id from the command line
//			new Router(1);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
//	public static String ID = "127.0.0.1";
	public static int DEFAULT_PORT = 50000; //what port the router communicates on
	public static int MGMT_PORT = 50001; //what port this router's managementController communicates on
	public String ID; //What is the name of this router?
	public int rtNum; //What is the number of this router?
	private Map<List<String>, String> sendMap; // a map of where to send packets -> key = senderID, tgtID
	private Map<List<String>, ArrayList<DatagramPacket>> waitingList; // a map of packets waiting to be sent
	private ManagementController manager; //this router's managementController

	public Router(int rtNum) throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT)); //create a CommPoint on this port
		sendMap = new HashMap<List<String>, String>(); //initialize map 
		waitingList = new HashMap<List<String>, ArrayList<DatagramPacket>>(); //initialize map
		this.ID = PREFIX + rtNum; //create ID
		this.rtNum = rtNum;
		this.start(); //Start listener on port
		manager = new ManagementController(MGMT_PORT, this); //create management controller on port
		manager.sendHELLO(); //try to connect to controller
	}
	
	public void ACKReceived(DatagramPacket thePacket) {//Treat ACK as normal packet
		DATAReceived(thePacket);
	}

	//data packet received, send it on
	public synchronized boolean DATAReceived(DatagramPacket thePacket) {
		System.out.println("DATA Received.");
		String[] tgtData = Packet.getTgtInfo(thePacket);//get misc. data from packet
		String next = lookUpNext(new String[] {tgtData[Packet.SENDER_ID],tgtData[Packet.TGT_ID]});//find the next hop in sequence
		if (next == null) {//next hop unknown
			System.out.println("DST Unknown... Asking Controller For INFO.");
			addToWaitingList(new String[] {tgtData[Packet.SENDER_ID],tgtData[Packet.TGT_ID]}, thePacket);//add to waiting list
			manager.getHELP(tgtData[Packet.SENDER_ID],tgtData[Packet.TGT_ID]);//ask manager for help
			return false;
		} else {
			System.out.println("DST Known... Preparing To Forward.");
			Integer tgtPort;
			if (next.equals(tgtData[Packet.TGT_ID])) {//is the next hop to the endpoint?
				tgtPort = Integer.parseInt(tgtData[Packet.TGT_PORT]);
				System.out.println("Sending to Endpoint:" + next);
			}else {//next hop is also router
				tgtPort = Router.DEFAULT_PORT;
				System.out.println("Sending to Router:" + next);
			}
			thePacket.setSocketAddress(new InetSocketAddress(next.toString(), tgtPort));//update address
			forward(thePacket);//send the packet on
			return true;
		}
	}
	
	//packet is cached while waiting for destination from controller
	private void addToWaitingList(String[] key, DatagramPacket thePacket) {
		List<String> modKey = Arrays.asList(key[0], key[1]); //create a key
		ArrayList<DatagramPacket> list = waitingList.get(modKey);
		if(list == null)//is this the first packet with this destination?
			list = new ArrayList<DatagramPacket>();
		else//this is not the first packet with this destination
			waitingList.remove(modKey);
		list.add(thePacket);//add the packet to the list
		waitingList.put(modKey, list); //place the list in the map
	}
	
	//received update from controller, send any waiting packets
	public void sendWaiting(String[] key) {
		List<String> modKey = Arrays.asList(key[0], key[1]);
		ArrayList<DatagramPacket> list = waitingList.get(modKey);
		if(list != null) {//there are packets to be sent
			System.out.println("Sending " + list.size() + " waiting packets.");
			for(DatagramPacket lPacket : list) //send every packet
				DATAReceived(lPacket);
			waitingList.remove(modKey); //remove list
		}
	}

	//Treat help as normal packet
	public void HELPReceived(DatagramPacket thePacket) {
		DATAReceived(thePacket);
	}
		
	//find the next hop in the series
	private String lookUpNext(String[] tgt) {
		List<String> modTgt = Arrays.asList(tgt[0], tgt[1]);
		return sendMap.get(modTgt);
	}

	//add a new path to the map
	public void updateTable(String[] key, String next) {
		List<String> modKey = Arrays.asList(key[1], key[0]);
		System.out.println("Now Routing packets from:" + key[0] + " with dst:"
				+ key[1] + " to:" + next);
		sendMap.put(modKey, next);
	}

	//forward a packet
	private void forward(DatagramPacket thePacket) {
		try {
			socket.send(thePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//treat as a normal packet
	public void HELLOReceived(DatagramPacket thePacket) {
		DATAReceived(thePacket);
	}

	//treat as a normal packet
	public void UPDATEReceived(DatagramPacket thePacket) {	
		DATAReceived(thePacket);
	}

}
