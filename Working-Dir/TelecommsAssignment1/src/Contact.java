import java.util.ArrayList;

public class Contact {
	public final String name;
	public final int tgtPort;
	public final int srcPort;
	public ArrayList<Byte> subList;
	public final boolean isPub;

	public Contact(String name, int tgtPort, int srcPort) {
		this.name = name;
		this.tgtPort = tgtPort;
		this.srcPort = srcPort;
		this.isPub = true;
	}

	public Contact(String name, int tgtPort, int srcPort, ArrayList<Byte> subList, boolean isPub) {
		this.name = name;
		this.tgtPort = tgtPort;
		this.srcPort = srcPort;
		this.subList = subList;
		this.isPub = isPub;
	}

	public void addSub(byte theSub) {
		this.subList.add(theSub);
	}

	public void unSub(byte theSub) {
		try {
			this.subList.remove(subList.indexOf(theSub));
		} catch (Exception e) {
		}
	}

	public String toString() {
		String retString = name + "|" + tgtPort + "|" + subList.toString();
		return retString;
	}
}
