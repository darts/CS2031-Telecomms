import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class Controller extends CommPoint {

	public static void main(String[] args) {
		try {
			new Controller();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private static int NUM_OF_ROUTERS = 2;
	public static String ID = "CONTROLLER";
//	public static String ID = "127.0.0.1";
	public static int COMM_PORT = 50505;
	private RoutingTable routingTable;
	private Map<String[], Frame> packetMap;
	private boolean[] activeRouters = new boolean[NUM_OF_ROUTERS];

	public Controller() throws SocketException {
		super(new DatagramSocket(COMM_PORT));
		this.initTable();
		packetMap = new HashMap<String[], Frame>();
		this.start();
	}

	public void ACKReceived(DatagramPacket thePacket) {
		System.out.println("ACK received... cancelling timeout");
		String[] key = Packet.getTgtInfo(thePacket);
		packetMap.get(key).cancel();
		packetMap.remove(key);
	}

	public void HELLOReceived(DatagramPacket thePacket) {
		String RTName = Packet.getTgtInfo(thePacket.getData())[Packet.SENDER_ID];
		System.out.println("HELLO received... Adding router to active list...  " + RTName);
		int theRouter = Integer.parseInt(RTName);
		activeRouters[theRouter - 1] = true;
		//******************************************
		Frame resFrame = new Frame(
				new Packet(new InetSocketAddress(Router.PREFIX + RTName, Router.MGMT_PORT), Packet.HELLO, -1),
				this.socket);
//		Frame resFrame = new Frame(
//				new Packet(new InetSocketAddress(Controller.ID, Router.MGMT_PORT), Packet.HELLO, -1),
//				this.socket);
		//******************************************
		resFrame.send();
		resFrame.cancel();
	}

	public boolean DATAReceived(DatagramPacket thePacket) {// Controller should not receive DATA packet
		System.err.println("DATA received! Should not happen!");
		return false;
	}

	public void UPDATEReceived(DatagramPacket thePacket) {// Controller should not receive this
		System.err.println("UPDATE received! Should not happen!");
	}

	public void HELPReceived(DatagramPacket thePacket) {// A router calls for aid, controller will answer
		System.out.println("HELP REQUEST received... Sending UPDATE");
		String[] helpData = Packet.getTgtInfo(thePacket.getData());
		RoutingTable.Path thePath = routingTable.getPath(helpData[Packet.TGT_ID], helpData[Packet.SENDER_ID]);
		for (int i = 0; i < thePath.rtList.length; i++) {
			String[] dataToSend = { helpData[Packet.SENDER_ID], helpData[Packet.TGT_ID], thePath.outList[i] };
			Frame tmpFrame = new Frame(
					new Packet(new InetSocketAddress(thePath.rtList[i], Router.MGMT_PORT), Packet.UPDATE, dataToSend),
					socket);
			packetMap.put(new String[] { helpData[Packet.SENDER_ID], helpData[Packet.TGT_ID], thePath.rtList[i] },
					tmpFrame);
			tmpFrame.send();
		}
	}

	private void initTable() {
		routingTable = new RoutingTable();
		String dst = Endpoint.PREFIX + "1";
		String src = Endpoint.PREFIX + "2";
		String[] router = { Router.PREFIX + "2", Router.PREFIX + "1" };
		String[] inList = { Endpoint.PREFIX + "2", Router.PREFIX + "2" };
		String[] outList = { Router.PREFIX + "1", Endpoint.PREFIX + "1" };
		routingTable.addPath(dst, src, router, inList, outList);

		dst = Endpoint.PREFIX + "2";
		src = Endpoint.PREFIX + "1";
		router = new String[] { Router.PREFIX + "1", Router.PREFIX + "2" };
		inList = new String[] { Endpoint.PREFIX + "1", Router.PREFIX + "1" };
		outList = new String[] { Router.PREFIX + "2", Endpoint.PREFIX + "2" };
		routingTable.addPath(dst, src, router, inList, outList);
	}

}
