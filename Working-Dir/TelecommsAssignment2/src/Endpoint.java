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
	public static int DEFAULT_PORT = 51000;
	public static String STRT_ID = "-1";

	public static void main(String[] args) {
		try {
			new Endpoint(Integer.parseInt(args[0]));
//			new Endpoint(1);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private Frame activePacket;
	private boolean connectionActive;
	private String dataToSend;
	private String[] commData;
	private String ID;
	private boolean transmissionComplete;
	private int transmissionFileName = 0;
	private String defGateway;
	private InetSocketAddress defGatewayAddr;

	public Endpoint(int eNum) throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		activePacket = null;
		connectionActive = false;
		dataToSend = null;
		ID = Endpoint.PREFIX + Integer.toString(eNum);
		defGateway = Router.PREFIX + eNum;
		defGatewayAddr = new InetSocketAddress(defGateway, Router.DEFAULT_PORT);
//		defGateway = Router.ID;
		this.start();
		new UserInterface(this);
	}

	//Acknowledgement received
	public void ACKReceived(DatagramPacket thePacket) {
		String[] contactData = Packet.getTgtInfo(thePacket);
		//if we are expecting a message from this EndPoint and the data is valid
		if (expectingComms(contactData) && contactData[Packet.PACKET_ID].equals(Packet.DATA_ID)) {
			if (!transmissionComplete)//if we are finished communicating
				sendAck(false);//send ACK with no timeout
			reset();
		}
	}

	public void HELLOReceived(DatagramPacket thePacket) {
		System.out.println("HELLO Received");
		String[] contactData = Packet.getTgtInfo(thePacket);
		if (!connectionActive) {// not talking to anyone atm
			if (contactData[Packet.PACKET_ID].equals(STRT_ID)) {
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

	//data packet received 
	public boolean DATAReceived(DatagramPacket thePacket) {
		System.out.println("DATA Received");
		String[] contactData = Packet.getTgtInfo(thePacket);
		if (connectionActive && expectingComms(contactData)) {// are we expecting contact
			try {//write the received data to a file
				this.activePacket.cancel();
				BufferedWriter writer = new BufferedWriter(
						new FileWriter(Integer.toString(this.transmissionFileName++)));
				writer.write(Packet.getContents(thePacket) + "\n");
				writer.write(Packet.getTgtInfo(thePacket).toString());
				writer.flush();
				writer.close();
				sendAck(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	//ignore
	public void UPDATEReceived(DatagramPacket thePacket) {// should not receive this packet
	}

	//ignore
	public void HELPReceived(DatagramPacket thePacket) {// should not receive this packet
	}

	private void sendData() {
		System.out.println("Sending DATA");
		//create a packet to send
		Packet tmp = new Packet(defGatewayAddr, this.generateDataString(commData[Packet.SENDER_ID], Packet.DATA_ID),
				this.dataToSend);
		activePacket = new Frame(tmp, socket);
		activePacket.send();//send the packet
	}

	//send an ACK
	private void sendAck(boolean timerActive) {
		System.out.println("Sending ACK");
		//create packet
		Packet tmp = new Packet(defGatewayAddr, Packet.ACK,
				this.generateDataString(commData[Packet.SENDER_ID], Packet.DATA_ID));
		activePacket = new Frame(tmp, socket);
		activePacket.send();
		this.transmissionComplete = true;
		if (!timerActive) {//cancel the timer?
			activePacket.cancel();
		}
	}

	//are we expecting a message from this node
	private boolean expectingComms(String[] contactData) {
		if (this.commData[Packet.SENDER_ID].equals(contactData[Packet.SENDER_ID])
				&& this.commData[1].equals(contactData[Packet.SENDER_PORT]))
			return true;
		return false;
	}

	public synchronized void startTransmission(String dst, String data) {
		dataToSend = data;// store what needs to be sent
		sendStart(dst);
	}

	//send a start packet
	private void sendStart(String dst) {
		System.out.println("Sending STRT");
		Packet sendPacket = new Packet(defGatewayAddr, Packet.HELLO, generateDataString(dst, Endpoint.STRT_ID));
		this.activePacket = new Frame(sendPacket, socket);
		this.commData = new String[] { dst, Integer.toString(DEFAULT_PORT) };
		this.activePacket.send();
		this.connectionActive = true;
	}

	// this function generates the required information for a data packet
	private String[] generateDataString(String tgtID, String seqNum) {
		return new String[] {this.ID, tgtID, Integer.toString(this.port), Integer.toString(Endpoint.DEFAULT_PORT),
				seqNum };
	}

	private void reset() {
		activePacket.cancel();
		activePacket = null;
		connectionActive = false;
		dataToSend = null;
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
				System.out.print("Please enter a destination and message, separated by '~':");
				String input = userInputScanner.nextLine();
				if (input.equals(QUIT))
					online = false;
				else if(input.contains("~")){
					String[] parts = input.split("~");
					System.out.println("Sending:'" + parts[1] + "' to:" + parts[0]);
					parent.startTransmission(parts[0], parts[1]);
				}else {
					System.err.println("ERROR ON INPUT!!");
				}
			}
			userInputScanner.close();
		}
	}

}
