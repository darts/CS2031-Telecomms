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
			new Router(Integer.parseInt(args[0]));
//			new Router(1);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
//	public static String ID = "127.0.0.1";
	public static int DEFAULT_PORT = 50000;
	public static int MGMT_PORT = 50001;
	public String ID;
	public int rtNum;
	private Map<List<String>, String> sendMap; // a map of where to send packets
	private Map<List<String>, ArrayList<DatagramPacket>> waitingList;
	private ManagementController manager;

	public Router(int rtNum) throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		sendMap = new HashMap<List<String>, String>();
		waitingList = new HashMap<List<String>, ArrayList<DatagramPacket>>();
		this.ID = PREFIX + rtNum;
		this.rtNum = rtNum;
		this.start();
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
		List<String> modKey = Arrays.asList(key[0], key[1]);
		ArrayList<DatagramPacket> list = waitingList.get(modKey);
		if(list == null)
			list = new ArrayList<DatagramPacket>();
		else
			waitingList.remove(modKey);
		list.add(thePacket);
		waitingList.put(modKey, list);
	}
	
	public void sendWaiting(String[] key) {
		List<String> modKey = Arrays.asList(key[0], key[1]);
		ArrayList<DatagramPacket> list = waitingList.get(modKey);
		if(list != null) {
			for(DatagramPacket lPacket : list)
				DATAReceived(lPacket);
		}
	}

	public void HELPReceived(DatagramPacket thePacket) {
		System.out.println("HELP Request Received... Ignoring.");
	}

	private String lookUpNext(String[] tgt) {
		List<String> modTgt = Arrays.asList(tgt[0], tgt[1]);
		return sendMap.get(modTgt);
	}

	public void updateTable(String[] key, String next) {
		List<String> modKey = Arrays.asList(key[0], key[1]);
		sendMap.put(modKey, next);
	}

	private void forward(DatagramPacket thePacket) {
		try {
			socket.send(thePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void HELLOReceived(DatagramPacket thePacket) {
		DATAReceived(thePacket);
	}

	public void UPDATEReceived(DatagramPacket thePacket) {		
	}

}
