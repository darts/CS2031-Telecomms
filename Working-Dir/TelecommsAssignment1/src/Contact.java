import java.util.ArrayList;

public class Contact {
	public final String name; // the name
	public final int tgtPort; // port to send to
	public final int srcPort; // port sending from
	public ArrayList<Byte> subList; // list of subscriptions
	public CommPoint theComm;// what commPoint this is transmitting on

	public Contact(String name, int tgtPort, int srcPort) {
		this.name = name;
		this.tgtPort = tgtPort;
		this.srcPort = srcPort;
	}

	public Contact(String name, int tgtPort, int srcPort, ArrayList<Byte> subList) {
		this.name = name;
		this.tgtPort = tgtPort;
		this.srcPort = srcPort;
		this.subList = subList;
	}

	public void setComm(CommPoint theComm) {
		this.theComm = theComm;
	}

	public void addSub(byte theSub) {// add a subscription
		this.subList.add(theSub);
	}

	public void unSub(byte theSub) {// remove a subscription
		try {
			this.subList.remove(subList.indexOf(theSub));
		} catch (Exception e) {
		}
	}

	public String toString() {// get a string representation of object
		String retString = name + "|" + tgtPort + "|" + subList.toString();
		return retString;
	}
}
