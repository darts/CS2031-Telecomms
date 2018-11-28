import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class Controller extends CommPoint{

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static String ID = "CONTROLLER";
	public static int COMM_PORT = 50505;
	private RoutingTable routingTable;
	private Map<String[], Frame> packetMap;
	
	public Controller() throws SocketException {
		super(new DatagramSocket(COMM_PORT));
		initTable();
		packetMap = new HashMap<String[], Frame>();
	}

	public void ACKReceived(DatagramPacket thePacket) {
		
	}

	public void HELLOReceived() {
		
	}
	
	public boolean DATAReceived(DatagramPacket thePacket) {//Controller should not receive DATA packet
		return false;
	}

	public void UPDATEReceived(DatagramPacket thePacket) {
		
	}
	
	public void HELPReceived() {
		
	}
	
	private void initTable() {
		routingTable = new RoutingTable();
		String dst = Endpoint.PREFIX + "1";
		String src = Endpoint.PREFIX + "2";
		String[] router = {Router.PREFIX + "2", Router.PREFIX + "1"};
		String[] inList = {Endpoint.PREFIX + "2", Router.PREFIX + "2"};
		String[] outList = {Router.PREFIX + "1", Endpoint.PREFIX + "1"};
		routingTable.addPath(dst, src, router, inList, outList);
		
		dst = Endpoint.PREFIX + "2";
		src = Endpoint.PREFIX + "1";
		router = new String[] {Router.PREFIX + "1", Router.PREFIX + "2"};
		inList = new String[] {Endpoint.PREFIX + "1", Router.PREFIX + "1"};
		outList = new String[] {Router.PREFIX + "2", Endpoint.PREFIX + "2"};
		routingTable.addPath(dst, src, router, inList, outList);
	}
	

}
