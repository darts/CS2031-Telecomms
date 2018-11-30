import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class CommPoint extends Listener {
	
	//for a router
	public CommPoint(DatagramSocket srcPort) {
		super(srcPort);
	}

	// determine what type of packet has been received.
	public void packetRecieved(DatagramPacket thePacket) {
		byte type = Packet.getType(thePacket);
		switch (type) {
		case Packet.ACK:
			this.ACKReceived(thePacket);
			break;
		case Packet.HELLO:
			this.HELLOReceived(thePacket);
			break;
		case Packet.DATA:
			this.DATAReceived(thePacket);
			break;
		case Packet.UPDATE:
			this.UPDATEReceived(thePacket);
			break;
		case Packet.HELP:
			this.HELPReceived(thePacket);
			break;
		}
	}

	public abstract void ACKReceived(DatagramPacket thePacket);
	
	public abstract void HELLOReceived(DatagramPacket thePacket);
	
	public abstract boolean DATAReceived(DatagramPacket thePacket);
	
	public abstract void UPDATEReceived(DatagramPacket thePacket);
	
	public abstract void HELPReceived(DatagramPacket thePacket);
}
