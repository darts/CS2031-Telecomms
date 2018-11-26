import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class CommPoint extends Listener {
	private Sender theSender; // for sending stuff
	private DatagramSocket socket; // sending socket
	private boolean dataToSendBool = false; // still data to send
	private String dataToSend; // what is to be sent
	private String dataReceived = "";// what has been received
	private String tgtName;// who is being transmitted to
	private int tgtPort;// what port is being transmitted to
	private int recNum = 0;// name of active file for receiving data
	private BufferedWriter writer;// to write received data to file

	//for a router
	public CommPoint(String tgtName, int tgtPort, DatagramSocket srcPort) {
		super(srcPort);
		this.tgtName = tgtName;
		this.tgtPort = tgtPort;
		this.socket = srcPort;
	}

	public void startDataTransmission(String theData, byte type) {// send some data
		dataToSend = theData;
		dataToSendBool = true;
		byte[] theType = { type };
	}

	// determine what type of packet has been received.
	public void packetRecieved(DatagramPacket thePacket) {
		byte type = Packet.getType(thePacket);
		switch (type) {
		case Packet.ACK:
			this.ACKReceived();
		case Packet.HELLO:
			this.HELLOReceived();
			
		}
	}

	public void ACKReceived() {
		theSender.ackRecieved();// tell the sender to cancel timeouts
	}
}
