import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Packet {
	public static final int MAX_LENGTH_BITS = 65536;// Max num. of bits (2 bytes -> short)
	public static final int MAX_LENGTH_BYTES = MAX_LENGTH_BITS / 8;

	public static final Charset DEF_ENCODING = StandardCharsets.UTF_16;

	public static final byte ACK = 0; //ack a packet
	public static final byte NAK = 1; //nak a packet
	public static final byte SUB = 2; //subscription packet
	public static final byte DATA = 3; //data packet
	public static final byte USUB = 4; //unsubscribe packet
	public static final byte STRT = 5; //transmission start packet
	public static final byte STRT_ACK = 6; //ack a transmission start
	public static final byte END = 7; //end of data packets
	public static final byte END_ACK = 8; //ack transmission end

	public byte packType;// 1 byte
	public byte seqNum;// 1 byte
	// public byte seqTotal;
	public String content = null;
	public byte[] contentArr = null;
//	public int hash;//4 bytes

	private static final int NUM_OF_ADDITIONAL_BYTES = 2;

	public Packet(byte type, byte sequNum, String data) {
		packType = type;// 1 byte to identify packet type
		seqNum = sequNum;// 1 byte to identify sequence number
//		seqTotal = sequTotal;// 1 byte to identify number of items in sequence
		content = data;
		if (content.equals(null))
			content = "";
	}

	public Packet(byte type, byte sequNum, byte[] data) {
		packType = type;// 1 byte to identify packet type
		seqNum = sequNum;// 1 byte to identify sequence number
		contentArr = data;
		if (contentArr == null)
			contentArr = new byte[0];
//		hash = data.hashCode();
	}

	public DatagramPacket toDatagramPacket() {
		DatagramPacket thePacket = null;
		if (contentArr == null)
			contentArr = content.getBytes(DEF_ENCODING);// convert string to byte array
		byte[] data = new byte[contentArr.length + NUM_OF_ADDITIONAL_BYTES];
		for (int i = 0; i < contentArr.length; i++) // write the contents
			data[i + NUM_OF_ADDITIONAL_BYTES] = contentArr[i];
		data[0] = packType;
		data[1] = seqNum;
//		data[2] = seqTotal;		
		thePacket = new DatagramPacket(data, data.length);

		return thePacket;
	}

	public static Packet toPac(byte[] data) {// by default this converts the content to a string
		byte type = data[0];
		byte seqNum = data[1];
//		byte seqTotal = data[2];
		byte[] contentArray = new byte[data.length - 3];
		for (int i = 0; i < contentArray.length; i++)
			contentArray[i] = data[i + 3];
		String content = new String(contentArray, DEF_ENCODING);
		Packet thePacket = new Packet(type, seqNum, content);

		return thePacket;
	}

}
