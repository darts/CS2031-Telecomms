import java.io.IOException;
import java.net.DatagramSocket;

public class Sender {
	DatagramSocket theSocket;
	public Sender(DatagramSocket socket) {
		theSocket = socket;
	}
	
	public boolean sendData(String data, String dstNode,int dstPort) {
		String[] stringsToSend = splitStr(data);
		for(int i = 0; i < stringsToSend.length; i++) {
//			sendPacket(new Packet())
		}
		return false;
	}
	
	private boolean sendPacket(Packet thePack) {
		try {
			theSocket.send(thePack.toDatagramPacket());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String[] splitStr(String theString) {
		int noOfStrings = (theString.length() * 8) / Packet.MAX_LENGTH_BYTES;
		if((theString.length() * 8) % Packet.MAX_LENGTH_BYTES != 0)
			noOfStrings++;
		String[] resString = new String[noOfStrings];
		int start = 0;
		int end = (theString.length() < Packet.MAX_LENGTH_BYTES) ? theString.length():Packet.MAX_LENGTH_BYTES;
		for(String rsString : resString) {
			rsString = theString.substring(start, end);
			//System.out.println(rsString);
			start += Packet.MAX_LENGTH_BYTES;
			end = ((end+Packet.MAX_LENGTH_BYTES) > theString.length()) ? theString.length() : (end + Packet.MAX_LENGTH_BYTES);
		}
		return resString;
	}
}
