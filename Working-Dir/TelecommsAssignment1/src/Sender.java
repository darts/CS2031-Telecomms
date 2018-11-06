import java.net.DatagramSocket;
import java.util.Arrays;

public class Sender extends Thread{
	public static final int DEF_WINDOW_WIDTH = 5;
	private final int WINDOW_MAX = (DEF_WINDOW_WIDTH * 2);
	DatagramSocket theSocket;
	public Frame[] frameArray = new Frame[WINDOW_MAX];

	public Sender(DatagramSocket socket) {
		theSocket = socket;
	}
	
	private int activePackets = 0;

	public boolean sendData(String data, String dstNode, int dstPort) {
		byte[][] stringsToSend = splitStr(data);
		int packetsToSend = stringsToSend.length;
		byte pacNum = 0;
		int packetsSent = 0;
		while (packetsToSend > 0) {
			while(activePackets < DEF_WINDOW_WIDTH) {
				sendPacket(new Packet(Packet.DATA, pacNum, stringsToSend[packetsSent]));
				pacNum = ((pacNum + 1) > WINDOW_MAX)? 0 : pacNum++;
				packetsToSend--;
				packetsSent++;
			}
		}
		return false;
	}

	private void sendPacket(Packet thePack) {
		frameArray[thePack.seqNum] = new Frame(thePack, theSocket);
		frameArray[thePack.seqNum].send();
		activePackets++;
		
	}

	public void ackRecieved(int index) {
		if (index < frameArray.length && index >= 0) {
			frameArray[index].cancel();
			frameArray[index] = null;
			activePackets--;
		}
	}
	
	public void nakRecieved(int index) {
		if (index < frameArray.length && index >= 0) {
			frameArray[index].resend();
		}
	}

	private byte[][] splitStr(String theString) {
		byte[] strByteArr = theString.getBytes(Packet.DEF_ENCODING);
		int noOfStrings = strByteArr.length / Packet.MAX_LENGTH_BYTES;
		if (strByteArr.length % Packet.MAX_LENGTH_BYTES != 0)
			noOfStrings++;
		byte[][] resByte = new byte[noOfStrings][];
		int start = 0;
		int end = (strByteArr.length < Packet.MAX_LENGTH_BYTES) ? strByteArr.length : Packet.MAX_LENGTH_BYTES;
		for (byte[] rsString : resByte) {
			rsString = Arrays.copyOfRange(strByteArr, start, end);
			start += Packet.MAX_LENGTH_BYTES;
			end = ((end + Packet.MAX_LENGTH_BYTES) > theString.length()) ? theString.length()
					: (end + Packet.MAX_LENGTH_BYTES);
		}
		return resByte;
	}
}
