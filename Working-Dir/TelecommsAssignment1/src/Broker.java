import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Broker {

	private static final int DEFAULT_SRC_PORT = 50000;
	private static final int DEFAULT_DST_PORT = 50001;
	private static final int DEFAULT_SLEEP_TIME = 3;// how long between runs

	public static void main(String[] args) {
		Contact[] subList = initSubList(); // Get some test contacts
		CommPoint[] commList = new CommPoint[subList.length]; // List of CommPoints for the contacts
		for (int i = 0; i < subList.length; i++) {// create a CommPoint for each contact
			try {
				Contact contact = subList[i];// Get the deets
				// Create a new CommPoint and add it to the list
				commList[i] = (new CommPoint(contact.name, contact.tgtPort, contact.srcPort,
						new DatagramSocket(contact.srcPort), contact.isPub));
			} catch (SocketException e) {// Catch that error like pneumonia in winter
				e.printStackTrace();
			}
		}
		while (true) {
			System.out.println("Attempting to forward data.");
			for (int j = 0; j < commList.length; j++) {// go through each CommPoint
				CommPoint comm = commList[j];
				if (comm.isPub) { // if it's a publisher
					String data = comm.getData(); // get any received data
					if (data != null) { // has data to be sent
						System.out.println("	Data To Be Forwarded.");
						byte topic = comm.getTopic();
						for (int i = 0; i < subList.length; i++) {// go through subscribers
							Contact sub = subList[i];
							if (sub.subList.contains(topic)) {// if a subscriber is subbed to a topic -> forward data
								commList[i].startDataTransmission(data, topic);
								System.out.println("		Forwarding Data");
							}
						}
					}
				}
				ArrayList<Byte> nSubs = comm.getSubs();// get new subscriptions
				for (Byte theSub : nSubs) {// add them to the subs
					if (!subList[j].subList.contains(theSub))// if not already subbed
						subList[j].subList.add(theSub);
				}
				ArrayList<Byte> nUSubs = comm.getUSubs();// get new un-subscriptions
				for (Byte theUSub : nUSubs) {// remove them from the subs
					if (subList[j].subList.contains(theUSub))// if subbed
						subList[j].subList.remove(theUSub);
				}
			}
			try {// wait a few seconds
				TimeUnit.SECONDS.sleep(Broker.DEFAULT_SLEEP_TIME);// take a nap
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// Create list of contacts for testing.
	private static Contact[] initSubList() {
		int NUM_OF_CONTACTS = 3;
		Contact[] subList = new Contact[NUM_OF_CONTACTS];
		subList[0] = (new Contact("publisher", DEFAULT_DST_PORT, DEFAULT_SRC_PORT, new ArrayList<Byte>(), true));
		subList[1] = (new Contact("sub1", DEFAULT_DST_PORT, DEFAULT_SRC_PORT + 1,
				new ArrayList<Byte>(Arrays.asList(UserInterface.T_ALGORITHMS, UserInterface.T_COMPUTERS)), false));
		subList[2] = (new Contact("sub2", DEFAULT_DST_PORT, DEFAULT_SRC_PORT + 2,
				new ArrayList<Byte>(Arrays.asList(UserInterface.T_TELECOMMS, UserInterface.T_COMPUTERS)), false));
		return subList;
	}

}
