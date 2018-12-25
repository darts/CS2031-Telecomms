import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class Packet {
	public static final int MAX_LENGTH_BITS = 65536;// Max num. of bits
	public static final int MAX_LENGTH_BYTES = MAX_LENGTH_BITS / 8;

	public static final byte ACK = 0; // ack a packet
	public static final byte HELLO = 1; // a hello packet
	public static final byte HELP = 2; // router doesn't know where to send packet
	public static final byte UPDATE = 3;// sent by controller to router with new path info
	public static final byte DATA = 4; // sent by client

	public static final String DATA_ID = "1";

	public byte[] contentArr = null; // char[] representation of content
	private InetSocketAddress targetAddr; // who to send to
	private Content content;

	// create a packet with int content (for HELLO)
	public Packet(InetSocketAddress targetAddr, byte type, int rtNum) {
		this.targetAddr = targetAddr;
		content = new Content(type, new String[] { Integer.toString(rtNum) });
		try {
			contentArr = Serializer.serialize(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// create a packet with a set of strings (for UPDATE, HELP, ACK)
	public Packet(InetSocketAddress targetAddr, byte type, String[] data) {
		this.targetAddr = targetAddr;
		content = new Content(type, data);
		try {
			contentArr = Serializer.serialize(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// create a packet with string content (for DATA)
	public Packet(InetSocketAddress targetAddr, String[] tgtInfo, String data) {
		this.targetAddr = targetAddr;
		content = new Content(DATA, tgtInfo, data);
		try {
			contentArr = Serializer.serialize(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// convert to a datagramPacket and set socket addr.
	public DatagramPacket toDatagramPacket() {
		return new DatagramPacket(contentArr, contentArr.length, targetAddr);// initialize that packet
	}

	// Get the contents from a datagramPacket
	public static String getContents(DatagramPacket recPack) {
		return Packet.getContents(recPack.getData());
	}

	// get the contents from a byte[]
	public static String getContents(byte[] data) {
		try {
			return Serializer.deserialize(data).data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// get packet type
	public static byte getType(DatagramPacket recPack) {
		return Packet.getType(recPack.getData());
	}

	// get packet type
	public static byte getType(byte[] data) {
		try {
			return Serializer.deserialize(data).type;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	//get tgtInfo from packet
	public static String[] getTgtInfo(DatagramPacket recPack) {
		return Packet.getTgtInfo(recPack.getData());
	}

	//get tgtInfo from packet
	public static String[] getTgtInfo(byte[] data) {
		try {
			return Serializer.deserialize(data).tgtInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int SENDER_ID = 0; //who sent the packet
	public static int TGT_ID = 1; //who the packet is going to
	public static int SENDER_PORT = 2; // what port the packet was sent from
	public static int NEXT_ADDR = 2; //next hop address
	public static int ROUTER_ID_LOC = 2; //ID of the router sending this message
	public static int TGT_PORT = 3; //target port
	public static int PACKET_ID = 4; //packet number
}
