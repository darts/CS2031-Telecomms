import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.util.Arrays;

public class Packet {
	public static final int MAX_LENGTH = 65536;//Max num. of bits (2 bytes -> short)
	
	public static final byte ACK = 0;
	public static final byte NAK = 1;
	public static final byte SUB = 2;
	public static final byte DATA = 3;
	public static final byte USUB = 4;

	public byte packType;
	public byte seqNum;
	public byte seqTotal;
	public String content;

	public Packet(byte type, byte sequNum, byte sequTotal, String data) {
		packType = type;// 1 byte to identify packet type
		seqNum = sequNum;// 1 byte to identify sequence number
		seqTotal = sequTotal;// 1 byte to identify number of items in sequence
		content = data;
		if(content.equals(null))
			content = "";
	}

	public DatagramPacket toDatagramPacket() {
		DatagramPacket thePacket = null;

		ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();// to read non-obj

		bOutStream.write(packType);// write the packet type
		bOutStream.write(seqNum);// write the sequence number
		bOutStream.write(seqTotal);// write the sequence total
		char[] contentChars = content.toCharArray();// convert string to char array
		for (char theChar : contentChars)// write the contents
			bOutStream.write(theChar);

		byte[] data = bOutStream.toByteArray(); //convert data to byte array
		thePacket = new DatagramPacket(data, data.length);

		return thePacket;
	}
	
	public static Packet toPac(byte[] data) {
		byte type = data[0];
		byte seqNum = data[1];
		byte seqTotal = data[2];
		byte[] contentArray = new byte[data.length - 3];
		for(int i = 0; i < contentArray.length; i++)
			contentArray[i] = data[i + 3];
		String content = Arrays.toString(contentArray);
		Packet thePacket = new Packet(type, seqNum, seqTotal, content);
		
		return thePacket;
	}

}
