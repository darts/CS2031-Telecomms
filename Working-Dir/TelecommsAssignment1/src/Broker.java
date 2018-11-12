import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Broker {

	private static final int DEFAULT_SRC_PORT = 50000;
	private static final int DEFAULT_DST_PORT = 50001;

	public static void main(String[] args) {
		Contact[] subList = initSubList();
		CommPoint[] commList = new CommPoint[subList.length];
		for (int i = 0; i < subList.length; i++) {
			try {
				Contact contact = subList[i];
				commList[i] = (new CommPoint(contact.name, contact.tgtPort, contact.srcPort,
						new DatagramSocket(contact.srcPort), contact.isPub));
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		while (true) {
			System.out.println("Attempting to forward data.");
			for (CommPoint comm : commList) {
				if (comm.isPub) {
					String data = comm.getData();
					if (data != null) {
						System.out.println("Data To Be Forwarded.");
						byte topic = comm.getTopic();
						for (int i = 0; i < subList.length; i++) {
							Contact sub = subList[i];
							if (sub.subList.contains(topic)) {
								commList[i].startDataTransmission(data, topic);
								System.out.println("Forwarding Data");
							}
						}
					}
				}
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

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
