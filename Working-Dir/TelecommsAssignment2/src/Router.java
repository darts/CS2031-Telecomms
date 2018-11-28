import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Router extends CommPoint {
	public static String PREFIX = "R";
	
	public static void main(String[] args) {
		try {
			new Router(args[0]);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private static int DEFAULT_PORT = 50000;
	private static int MGMT_PORT = 50001;
	public String ID;
	private Map<String[], String> sendMap; // a map of where to send packets
	private Map<String[], ArrayList<DatagramPacket>> waitingList;
	private ManagementController manager;

	public Router(String ID) throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		sendMap = new HashMap<String[], String>();
		waitingList = new HashMap<String[], ArrayList<DatagramPacket>>();
		this.ID = ID;
		manager = new ManagementController(MGMT_PORT, this);
		manager.sendHELLO();
	}
	
	public void ACKReceived(DatagramPacket thePacket) {//Treat ACK as normal packet
		DATAReceived(thePacket);
	}

	
	public synchronized boolean DATAReceived(DatagramPacket thePacket) {
		System.out.println("DATA Received.");
		String[] tgtData = Packet.getTgtInfo(thePacket);
		String next = lookUpNext(new String[] {tgtData[Packet.SENDER_ID],tgtData[Packet.TGT_ID]});
		if (next == null) {
			System.out.println("DST Unknown... Asking Controller For INFO.");
			addToWaitingList(new String[] {tgtData[Packet.SENDER_ID],tgtData[Packet.TGT_ID]}, thePacket);
			manager.getHELP(tgtData[Packet.SENDER_ID],tgtData[Packet.TGT_ID]);
			return false;
		} else {
			System.out.println("DST Known... Preparing To Forward.");
			Integer tgtPort;
			if (next.equals(tgtData[Packet.TGT_ID]))
				tgtPort = Integer.parseInt(tgtData[Packet.TGT_PORT]);
			else
				tgtPort = Router.DEFAULT_PORT;
			thePacket.setSocketAddress(new InetSocketAddress(next.toString(), tgtPort));
			forward(thePacket);
			return true;
		}
	}
	
	private void addToWaitingList(String[] key, DatagramPacket thePacket) {
		ArrayList<DatagramPacket> list = waitingList.get(key);
		if(list == null)
			list = new ArrayList<DatagramPacket>();
		else
			waitingList.remove(key);
		list.add(thePacket);
		waitingList.put(key, list);
	}
	
	public void sendWaiting(String[] key) {
		ArrayList<DatagramPacket> list = waitingList.get(key);
		if(list != null) {
			for(DatagramPacket lPacket : list)
				DATAReceived(lPacket);
		}
	}

	public void HELPReceived() {
		System.out.println("HELP Request Received... Ignoring.");
	}

	private String lookUpNext(String[] tgt) {
		return sendMap.get(tgt);
	}

	public void updateTable(String[] key, String next) {
		sendMap.put(key, next);
	}

	private void forward(DatagramPacket thePacket) {
		try {
			socket.send(thePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void HELLOReceived() {
	}

	public void UPDATEReceived(DatagramPacket thePacket) {		
	}

}
