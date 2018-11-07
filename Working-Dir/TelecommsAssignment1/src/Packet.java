import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Packet {
	public static final int MAX_LENGTH_BITS = 65536;// Max num. of bits (2 bytes -> short)
	public static final int MAX_LENGTH_BYTES = MAX_LENGTH_BITS / 8;

	public static final Charset DEF_ENCODING = StandardCharsets.UTF_16; // for converting strings to Char[]

	public static final byte ACK = 0; // ack a packet
	public static final byte NAK = 1; // nak a packet
	public static final byte SUB = 2; // subscription packet
	public static final byte DATA = 3; // data packet
	public static final byte USUB = 4; // unsubscribe packet
	public static final byte STRT = 5; // transmission start packet
	public static final byte STRT_ACK = 6; // ack a transmission start
	public static final byte END = 7; // end of data packets
	public static final byte END_ACK = 8; // ack transmission end

	public byte packType;// 1 byte to identify type
	public byte seqNum;// 1 byte to identify number in sequence
	public String content = null; // string representation of content
	public byte[] contentArr = null; // char[] representation of content

	private static final int NUM_OF_ADDITIONAL_BYTES = 2; // 1 for packType, 1 for seqNum

	private InetSocketAddress targetAddr;

	// create a packet with a string
	public Packet(InetSocketAddress targetAddr, byte type, byte sequNum, String data) {
		this.targetAddr = targetAddr;
		packType = type;// 1 byte to identify packet type
		seqNum = sequNum;// 1 byte to identify sequence number
		content = data; //
		if (content.equals(null)) // prevent null-pointer goodness
			content = "";
	}

	// create a packet with a char[]
	public Packet(InetSocketAddress targetAddr, byte type, byte sequNum, byte[] data) {
		this.targetAddr = targetAddr;
		packType = type;// 1 byte to identify packet type
		seqNum = sequNum;// 1 byte to identify sequence number
		contentArr = data;
		if (contentArr == null) // more null-pointer avoidance
			contentArr = new byte[0];
	}
	
	public Packet(InetSocketAddress targetAddr,byte type, byte sequNum) {
		this.targetAddr = targetAddr;
		packType = type;// 1 byte to identify packet type
		seqNum = sequNum;// 1 byte to identify sequence number
		contentArr = new byte[0];
	}

	// convert to a datagramPacket and set socket addr.
	public DatagramPacket toDatagramPacket() {
		DatagramPacket thePacket = null;
		if (contentArr == null) // if packet was initialized with string
			contentArr = content.getBytes(DEF_ENCODING);// convert string to byte array
		byte[] data = new byte[contentArr.length + NUM_OF_ADDITIONAL_BYTES];
		for (int i = 0; i < contentArr.length; i++) // write the contents
			data[i + NUM_OF_ADDITIONAL_BYTES] = contentArr[i];
		data[0] = packType;
		data[1] = seqNum;
		thePacket = new DatagramPacket(data, data.length, targetAddr);// initialize that packet yo
		return thePacket;
	}

	// This is only used for decoding contents
	// ***********************************************
	// IT DOES NOT GIVE THE ADDRESS OF THE DESTINATION
	// ***********************************************
	public static Packet toPac(DatagramPacket recPack) {// by default this converts the content to a string
		byte[] data = recPack.getData();// get the contents of the packet
		return new Packet(Packet.getType(data), Packet.getSeqNum(data), Packet.getContents(data));
	}

	//Get the contents from a datagramPacket
	public static String getContents(DatagramPacket recPack) {
		byte[] data = recPack.getData();// get the contents of the packet
		return getContents(data);
	}

	//get the contents from a byte[]
	public static String getContents(byte[] data) {
		byte[] contentArray = new byte[data.length - NUM_OF_ADDITIONAL_BYTES];
		for (int i = 0; i < contentArray.length; i++)
			contentArray[i] = data[i + NUM_OF_ADDITIONAL_BYTES];
		String content = new String(contentArray, DEF_ENCODING);
		return content;
	}

	//get packet type
	public static byte getType(DatagramPacket recPack) {
		return getType(recPack.getData());
	}

	//get packet type
	public static byte getType(byte[] data) {
		return data[0];
	}

	//get sequence number
	public static byte getSeqNum(DatagramPacket recPack) {
		return getSeqNum(recPack.getData());
	}

	//get sequence number
	public static byte getSeqNum(byte[] data) {
		return data[1];
	}

	private Packet(byte type, byte sequNum, String data) {
		this(null, type, sequNum, data);
	}
	
	public static byte getTopic(byte[] data) {
		return data[NUM_OF_ADDITIONAL_BYTES];
	}
}
