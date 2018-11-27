import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	public byte[] contentArr = null; // char[] representation of content
	private InetSocketAddress targetAddr; // who to send to
	private Content content;

	// create a packet with no content (for HELLO, ACK, HELP)
	public Packet(InetSocketAddress targetAddr, byte type) {
		this.targetAddr = targetAddr;
		content = new Content(type);
		contentArr = serialize(content);
	}

	// create a packet with a set of integers (for UPDATE)
	public Packet(InetSocketAddress targetAddr, byte type, int[] data) {
		this.targetAddr = targetAddr;
		content = new Content(type, data);
		contentArr = serialize(content);
	}

	// create a packet with string content (for DATA)
	public Packet(InetSocketAddress targetAddr, int tgt, String data) {
		this.targetAddr = targetAddr;
		content = new Content(DATA, tgt, data);
		contentArr = serialize(content);
	}

	// turn an object into a byte array
	private static byte[] serialize(Content data) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(outStream);
			objOut.writeObject(data);
			objOut.flush();
			return outStream.toByteArray();
		} catch (IOException e) {
		}
		return null;
	}
	
	//turn a byte array into a Content object
	private static Content deSerialize(byte[] data) {
		ByteArrayInputStream inStream = new ByteArrayInputStream(data);
		try {
			ObjectInputStream objIn = new ObjectInputStream(inStream);
			return (Content)objIn.readObject();
		} catch (Exception e) {
		}
		return null;
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
		return Packet.deSerialize(data).data;
	}
	
	// get packet type
	public static byte getType(DatagramPacket recPack) {
		return Packet.getType(recPack.getData());
	}

	// get packet type
	public static byte getType(byte[] data) {
		return Packet.deSerialize(data).type;
	}

	public static int[] getDataArr(DatagramPacket recPack) {
		return Packet.getDataArr(recPack.getData());
	}
	
	// get int[]
	public static int[] getDataArr(byte[] data) {
		return Packet.deSerialize(data).points;
	}
	
	public static Integer[] getTgtInfo(DatagramPacket recPack) {
		return Packet.getTgtInfo(recPack.getData());
	}
	
	public static Integer[] getTgtInfo(byte[] data) {
		return Packet.deSerialize(data).tgtInfo;
	}

	public static int SENDER_ID = 0;
	public static int SENDER_PORT = 1;
	public static int TGT_ID = 2;
	public static int TGT_PORT = 3;
	private class Content {
		byte type;
		Integer[] tgtInfo;//0 = senderID, 1 = senderPort, 2 = tgtID, 3 = tgtPort
		String data;
		int[] points;

		public Content(byte type) {
			this.type = type;
			this.data = null;
			this.points = null;
		}

		public Content(byte type, int tgt, String data) {
			this(type);
			this.data = data;
		}

		public Content(byte type, int[] points) {
			this(type);
			this.points = points;
		}
	}
}
