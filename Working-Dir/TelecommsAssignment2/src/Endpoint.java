import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Endpoint extends CommPoint {
	public static String PREFIX = "E";
	public static int DEFAULT_PORT = 50000;
//	public static String START = "Hello World.";
	public static String STRT_ID = "-1";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private Frame activePacket;
	private boolean connectionActive;
	private String dataToSend;
	private InetSocketAddress tgtAddr;
	private String[] commData;
	private String ID;

	public Endpoint(int eNum) throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		activePacket = null;
		connectionActive = false;
		dataToSend = null;
		ID = Endpoint.PREFIX + Integer.toString(eNum);
		new UserInterface(this);
	}

	public void ACKReceived(DatagramPacket thePacket) {

	}

	public void HELLOReceived(DatagramPacket thePacket) {
		String[] contactData = Packet.getTgtInfo(thePacket);
		if (!connectionActive) {// not talking to anyone atm
			if (contactData[Packet.PACKET_ID].equals(STRT_ID)) {
				this.tgtAddr = new InetSocketAddress(contactData[Packet.SENDER_ID],
						Integer.parseInt(contactData[Packet.SENDER_PORT]));
				sendStart(contactData[Packet.SENDER_ID]);
			} else
				System.err.println("Corrupt Packet Received...");
		} else {// are we expecting a reply from this Endpoint?
			if (expectingComms(contactData)) {
				activePacket.cancel();
				sendData();
			}
		}
	}

	public boolean DATAReceived(DatagramPacket thePacket) {
		if (connectionActive) {
			
		}
		return true;
	}

	public void UPDATEReceived(DatagramPacket thePacket) {// should not receive this packet
	}

	public void HELPReceived(DatagramPacket thePacket) {
	}
	
	private void sendData() {
		
	}
	
	private boolean expectingComms(String[] contactData) {
		if (this.commData[Packet.SENDER_ID].equals(contactData[Packet.SENDER_ID])
				&& this.commData[Packet.SENDER_PORT].equals(contactData[Packet.SENDER_PORT]))
			return true;
		return false;
	}

	public synchronized void startTransmission(String dst, String data) {
		dataToSend = data;// store what needs to be sent
		this.tgtAddr = new InetSocketAddress(dst, Endpoint.DEFAULT_PORT);
		sendStart(dst);
	}

	private void sendStart(String dst) {
		Packet sendPacket = new Packet(this.tgtAddr, Packet.HELLO, generateDataString(dst, Endpoint.STRT_ID));
		this.activePacket = new Frame(sendPacket, socket);
		this.commData = new String[] { dst, Integer.toString(DEFAULT_PORT) };
		this.activePacket.send();
		this.connectionActive = true;
	}

	// this function generates the required information for a data packet
	private String[] generateDataString(String tgtID, String seqNum) {
		return new String[] { this.ID, Integer.toString(this.port), tgtID, Integer.toString(Endpoint.DEFAULT_PORT),
				seqNum };
	}

	private class UserInterface extends Thread {
		private Endpoint parent;
		private boolean online;
		private String QUIT = "QUIT";

		public UserInterface(Endpoint parent) {
			this.online = true;
			this.parent = parent;
			this.start();
		}

		public void run() {
			Scanner userInputScanner = new Scanner(System.in);
			while (online) {
				System.out.print("Please enter a destination:");
				String dst = userInputScanner.next();
				if (dst.equals(QUIT))
					online = false;
				else {
					System.out.print("Please enter the text you would like to send:");
					String dataToSend = userInputScanner.nextLine();
					parent.startTransmission(dst, dataToSend);
				}
			}
			userInputScanner.close();
		}
	}

}
