public class Sender {
	public Sender() {
		
	}
	
	public boolean sendData(String data, String dstNode,int dstPort) {
		return false;
	}
	
	private String[] splitStr(String theString) {
		int noOfStrings = (theString.length() * 8) / Packet.MAX_LENGTH;
		if((theString.length() * 8) % Packet.MAX_LENGTH != 0)
			noOfStrings++;
		String[] resString = new String[noOfStrings];
	}
}
