import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
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
	private int ID;// this router's unique ID
	private Map<Integer, Integer> sendMap; // a map of where to send packets
	private Frame MGMTPacket;
	private DatagramSocket MGMTSocket = new DatagramSocket(MGMT_PORT);
	private InetSocketAddress MGMTAddr = new InetSocketAddress(Controller.ID, Controller.COMM_PORT);
	private ArrayList<DatagramPacket> waitingList = new ArrayList<DatagramPacket>();

	public Router() throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		sendHELLO();
	}

	public void ACKReceived() {// Should never receive an ACK
		System.err.println("ACK Recieved... Ignoring.");
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
			waitingList.add(thePacket);
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
		sendWaiting();
	}
	
	public void sendWaiting() {
		for(DatagramPacket thePacket : waitingList) {
			if(DATAReceived(thePacket))
		}
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
