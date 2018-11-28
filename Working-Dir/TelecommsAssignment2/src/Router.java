import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Router extends CommPoint {
	public static void main(String[] args) {
		try {
			new Router();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private static int DEFAULT_PORT = 50000;
	private static int MGMT_PORT = 50001;
	private Map<Integer, Integer> sendMap; // a map of where to send packets
	private Frame MGMTPacket;
	private DatagramSocket MGMTSocket = new DatagramSocket(MGMT_PORT);
	private InetSocketAddress MGMTAddr = new InetSocketAddress(Controller.ID, Controller.COMM_PORT);
	private Map<Integer, ArrayList<DatagramPacket>> waitingList;

	public Router() throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		sendMap = new HashMap<Integer, Integer>();
		waitingList = new HashMap<Integer, ArrayList<DatagramPacket>>();
		sendHELLO();
	}
	
	public void ACKReceived(DatagramPacket thePacket) {//Treat ACK as normal packet
		DATAReceived(thePacket);
	}

	private void sendHELLO() {
		System.out.println("Sending HELLO packet...");
		MGMTPacket = new Frame(new Packet(MGMTAddr, Packet.HELLO), MGMTSocket);
		MGMTPacket.send();
	}

	public void HELLOReceived() {
		System.out.println("HELLO Received... Connected To Controller.");
		MGMTPacket.cancel();
	}

	public synchronized boolean DATAReceived(DatagramPacket thePacket) {
		System.out.println("DATA Received.");
		Integer[] tgtData = Packet.getTgtInfo(thePacket);
		Integer next = lookUpNext(tgtData[Packet.TGT_ID]);
		if (next == null) {
			System.out.println("DST Unknown... Asking Controller For INFO.");
			addToWaitingList(tgtData[Packet.TGT_ID], thePacket);
			getHELP();
			return false;
		} else {
			System.out.println("DST Known... Preparing To Forward.");
			Integer tgtPort;
			if (next.equals(tgtData[Packet.TGT_ID]))
				tgtPort = tgtData[Packet.TGT_PORT];
			else
				tgtPort = Router.DEFAULT_PORT;
			thePacket.setSocketAddress(new InetSocketAddress(next.toString(), tgtPort));
			forward(thePacket);
			return true;
		}
	}

	public void getHELP() {
		MGMTPacket = new Frame(new Packet(MGMTAddr, Packet.HELLO), MGMTSocket);
		MGMTPacket.send();
	}

	public void UPDATEReceived(DatagramPacket thePacket) {
		try {
			MGMTPacket.cancel();
		} catch (Exception e) {
		}
		Integer[] upDateData = Packet.getTgtInfo(thePacket);
		updateTable(upDateData[Packet.SENDER_ID], upDateData[Packet.TGT_ID]);
		sendWaiting(upDateData[Packet.TGT_ID]);
	}
	
	private void addToWaitingList(Integer key, DatagramPacket thePacket) {
		ArrayList<DatagramPacket> list = waitingList.get(key);
		if(list == null)
			list = new ArrayList<DatagramPacket>();
		else
			waitingList.remove(key);
		list.add(thePacket);
		waitingList.put(key, list);
	}
	
	public void sendWaiting(Integer key) {
		ArrayList<DatagramPacket> list = waitingList.get(key);
		if(list != null) {
			for(DatagramPacket lPacket : list)
				DATAReceived(lPacket);
		}
	}

	public void HELPReceived() {
		System.out.println("HELP Request Received... Ignoring.");
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
