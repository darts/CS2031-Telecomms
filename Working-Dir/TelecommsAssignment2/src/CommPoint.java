import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public abstract class CommPoint extends Listener {
	public static int DEFAULT_PORT = 50000;
	public static int MGMT_PORT = 50001;
	public Sender theSender; // for sending stuff
	private DatagramSocket socket; // sending socket
	private String dataToSend; // what is to be sent
	private String dataReceived = "";// what has been received
	private String tgtName;// who is being transmitted to
//	private int recNum = 0;// name of active file for receiving data
//	private BufferedWriter writer;// to write received data to file

	//for a router
	public CommPoint(String tgtName, DatagramSocket srcPort) {
		super(srcPort);
//		try {
//			socket = new DatagramSocket(DEFAULT_PORT);
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}
		this.tgtName = tgtName;
		this.socket = srcPort;
	}

	public void startDataTransmission(String theData, byte type) {// send some data
		dataToSend = theData;
		byte[] theType = { type };
	}

	// determine what type of packet has been received.
	public void packetRecieved(DatagramPacket thePacket) {
		byte type = Packet.getType(thePacket);
		switch (type) {
		case Packet.ACK:
			this.ACKReceived();
			break;
		case Packet.HELLO:
			this.HELLOReceived();
			break;
		case Packet.DATA:
			this.DATAReceived(thePacket);
			break;
		case Packet.UPDATE:
			this.UPDATEReceived();
			break;
		case Packet.HELP:
			this.HELPReceived();
			break;
		}
	}

	public abstract void ACKReceived();
	
	public abstract void HELLOReceived();
	
	public abstract void DATAReceived(DatagramPacket thePacket);
	
	public abstract void UPDATEReceived();
	
	public abstract void HELPReceived();
}
