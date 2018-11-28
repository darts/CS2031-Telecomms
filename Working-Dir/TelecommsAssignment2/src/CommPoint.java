import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public abstract class CommPoint extends Listener {
	
	//for a router
	public CommPoint(DatagramSocket srcPort) {
		super(srcPort);
	}
	
	public CommPoint() {
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
			this.UPDATEReceived(thePacket);
			break;
		case Packet.HELP:
			this.HELPReceived();
			break;
		}
	}

	public abstract void ACKReceived();
	
	public abstract void HELLOReceived();
	
	public abstract boolean DATAReceived(DatagramPacket thePacket);
	
	public abstract void UPDATEReceived(DatagramPacket thePacket);
	
	public abstract void HELPReceived();
}
