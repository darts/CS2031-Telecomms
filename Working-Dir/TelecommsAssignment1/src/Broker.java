import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class Broker {

	private static final int DEFAULT_SRC_PORT = 50000;
	private static final int DEFAULT_DST_PORT = 50001;

	private ArrayList<Contact> subList; // Get some test subscribers
	private ArrayList<Contact> pubList; // get some test publishers
	private ArrayList<CommPoint> commList = new ArrayList<CommPoint>(); // List of CommPoints for the contacts

	public static void main(String[] args) {
		Broker theBroker = new Broker();
	}

	public Broker() {
		initLists();
	}

	public void initLists() {
		initSubs();
		initPubs();
	}

	public void initSubs() {//add the subs from sublist to active nodes
		subList = new ArrayList<Contact>();
		subList.add(new Contact("sub1", DEFAULT_DST_PORT, DEFAULT_SRC_PORT + 1,
				new ArrayList<Byte>(Arrays.asList(UserInterface.T_ALGORITHMS, UserInterface.T_COMPUTERS))));
		subList.add(new Contact("sub2", DEFAULT_DST_PORT, DEFAULT_SRC_PORT + 2,
				new ArrayList<Byte>(Arrays.asList(UserInterface.T_TELECOMMS, UserInterface.T_COMPUTERS))));
		for (Contact theContact : subList) {//create a CommPoint for each subscriber
			try {
				CommPoint theComm = new CommPoint(theContact.name, theContact.tgtPort, theContact.srcPort,
						new DatagramSocket(theContact.srcPort), theContact);
				theContact.setComm(theComm);
				commList.add(theComm);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	}

	public void initPubs() {//add the publishers to active nodes
		pubList = new ArrayList<Contact>();
		pubList.add(new Contact("publisher", DEFAULT_DST_PORT, DEFAULT_SRC_PORT, new ArrayList<Byte>()));
		for (Contact theContact : pubList) {//create a CommPoint for each publisher
			try {
				CommPoint theComm = new CommPoint(theContact.name, theContact.tgtPort, theContact.srcPort,
						new DatagramSocket(theContact.srcPort), this);
				theContact.setComm(theComm);
				commList.add(theComm);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	}

	//forward data to subscribers, can only be called by one thread at a time
	public synchronized void forwardToSubs(String data, byte topic) {
		for (Contact theContact : subList) {
			if(theContact.subList.contains(topic))
				theContact.theComm.startDataTransmission(data, topic);
		}
	}
}
