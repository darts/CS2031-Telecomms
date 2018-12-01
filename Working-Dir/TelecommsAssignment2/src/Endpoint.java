import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Endpoint extends CommPoint {
	public static String PREFIX = "E";
	public static int DEFAULT_PORT = 50000;
	public static String STRT_ID = "-1";

	public static void main(String[] args) {
		try {
			new Endpoint(Integer.parseInt(args[0]));
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private Frame activePacket;
	private boolean connectionActive;
	private String dataToSend;
	private InetSocketAddress tgtAddr;
	private String[] commData;
	private String ID;
	private boolean transmissionComplete;
	private int transmissionFileName = 0;

	public Endpoint(int eNum) throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		activePacket = null;
		connectionActive = false;
		dataToSend = null;
		ID = Endpoint.PREFIX + Integer.toString(eNum);
		new UserInterface(this);
	}

	public void ACKReceived(DatagramPacket thePacket) {
		String[] contactData = Packet.getTgtInfo(thePacket);
		if (expectingComms(contactData) && contactData[Packet.PACKET_ID].equals(Packet.DATA_ID)) {
			if (!transmissionComplete)
				sendAck(false);
			reset();
		}
	}

	public void HELLOReceived(DatagramPacket thePacket) {
		String[] contactData = Packet.getTgtInfo(thePacket);
		if (!connectionActive) {// not talking to anyone atm
			if (contactData[Packet.PACKET_ID].equals(STRT_ID)) {
				this.tgtAddr = new InetSocketAddress(contactData[Packet.SENDER_ID],
						Integer.parseInt(contactData[Packet.SENDER_PORT]));
				this.commData = new String[] { contactData[Packet.SENDER_ID], contactData[Packet.SENDER_PORT] };
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
		String[] contactData = Packet.getTgtInfo(thePacket);
		if (connectionActive && expectingComms(contactData)) {// are we expecting contact
			try {
				this.activePacket.cancel();
				BufferedWriter writer = new BufferedWriter(
						new FileWriter(Integer.toString(this.transmissionFileName++)));
				writer.write(Packet.getContents(thePacket));
				writer.flush();
				writer.close();
				sendAck(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public void UPDATEReceived(DatagramPacket thePacket) {// should not receive this packet
	}

	public void HELPReceived(DatagramPacket thePacket) {// should not receive this packet
	}

	private void sendData() {
		Packet tmp = new Packet(this.tgtAddr, this.generateDataString(commData[Packet.SENDER_ID], Packet.DATA_ID),
				this.dataToSend);
		activePacket = new Frame(tmp, socket);
		activePacket.send();
	}

	private void sendAck(boolean timerActive) {
		Packet tmp = new Packet(this.tgtAddr, Packet.ACK,
				this.generateDataString(commData[Packet.SENDER_ID], Packet.DATA_ID));
		activePacket = new Frame(tmp, socket);
		activePacket.send();
		this.transmissionComplete = true;
		if (!timerActive) {
			activePacket.cancel();
		}
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

	private void reset() {
		activePacket.cancel();
		activePacket = null;
		connectionActive = false;
		dataToSend = null;
		tgtAddr = null;
		commData = null;
		transmissionComplete = false;
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
