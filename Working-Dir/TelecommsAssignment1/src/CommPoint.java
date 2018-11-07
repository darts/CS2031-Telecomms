import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CommPoint extends Listener implements ReceiverInterface {
	private Sender theSender;
	private DatagramSocket socket;
	private boolean dataToSendBool = false;
	private String dataToSend;
	private boolean subToSend = false;
	private String dataReceived = "";
	private String[] window;
	private byte windowMin;
	private byte windowMax;
	private boolean windowValid;
	private byte topic;

	public CommPoint(String tgtName, int tgtPort, int srcPort) {
		try {
			this.socket = new DatagramSocket(srcPort);
			this.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		theSender = new Sender(tgtName, tgtPort, srcPort);
	}

	public void startDataTransmission(String theData, byte type) {
		dataToSend = theData;
		dataToSendBool = true;
		byte[] theType = {type};
		theSender.sendSTRT(theType);
	}

	@Override
	public void packetRecieved(DatagramPacket thePacket) {
		byte type = Packet.getType(thePacket);
		switch (type) {
		case Packet.ACK:
			this.ACKReceived(Packet.getSeqNum(thePacket.getData()));
			break;
		case Packet.NAK:
			this.NAKReceived(Packet.getSeqNum(thePacket.getData()));
			break;
		case Packet.DATA:
			this.DATAReceived(thePacket.getData());
			break;
		case Packet.SUB:
			this.SUBReceived();
			break;
		case Packet.USUB:
			this.USUBReceived();
			break;
		case Packet.STRT:
			this.STRTReceived(thePacket.getData());
			break;
		case Packet.STRT_ACK:
			this.STRT_ACKReceived();
			break;
		case Packet.END:
			this.ENDReceived(thePacket.getData());
			break;
		case Packet.END_ACK:
			this.END_ACKReceived();
		}
	}

	public void ACKReceived(byte seqNum) {
		theSender.ackRecieved(seqNum);
		System.out.println("ACK Received: " + seqNum);
	}

	public void NAKReceived(byte seqNum) {
		theSender.nakRecieved(seqNum);
		System.out.println("NAK Received: " + seqNum + "   Resending...");
	}

	public void STRTReceived(byte[] data) {
		this.topic = Packet.getTopic(data);
		System.out.println("STRT Received Topic: " + UserInterface.parseTopic(topic) + "   Sending STRT_ACK");
		theSender.sendSTRT_ACK();
		this.dataReceived = "";
		this.window = new String[Sender.WINDOW_MAX];
		this.windowMax = Sender.WINDOW_MAX - 1;
		this.windowMin = 0;
		this.windowValid = true;
	}

	public void STRT_ACKReceived() {
		System.out.println("STRT_ACK Received     Beginning comms...");
		theSender.endSTRT();
		if (dataToSendBool)
			theSender.sendData(dataToSend);
	}

	public void ENDReceived(byte[] data) {
		if(!nakMissingPackets(Packet.getSeqNum(data))) {//all packets accounted for
			theSender.sendEND_ACK();
			System.out.println("END Received      Sending END_ACK");
		}
	}

	public void END_ACKReceived() {
		theSender.endEND();
		System.out.println("END_ACK Received      Connection Closed");
	}

	public void DATAReceived(byte[] data) {
		handleWindow(data);
		
	}

	public void SUBReceived() {
		// TODO Auto-generated method stub

	}

	public void USUBReceived() {
		// TODO Auto-generated method stub

	}

	private void handleWindow(byte[] data) {
		byte seqNum = Packet.getSeqNum(data);
		if (seqNum < windowMax && seqNum > windowMin) {// packet is in window range
			if (seqNum == windowMin) {// Packet is as anticipated
				theSender.sendACK(seqNum);
				dataReceived += Packet.getContents(data);
				windowMax++;
				windowMin++;
				flushWindow(seqNum);
				System.out.println("DATA Received      Sending ACK");
			} else {
				window[seqNum] = Packet.getContents(data);
				nakMissingPackets(seqNum); // nak absent packets
				System.out.println("DATA Received   OUT OF ORDER!!  Sending NAK");
			}
		}
	}

	private boolean nakMissingPackets(byte seqNum) {
		boolean retBool = false;
		for (byte i = seqNum; i >= windowMin; i--) {// resend missing packets
			if (window[i] == null) {
				theSender.sendNAK(i);
				retBool = true;
			}
		}
		return retBool;
	}
	
	private void flushWindow(byte seqNum) {//remove any packets in the window
		int i = windowMin;
		String content = window[i];
		while(content != null && i < windowMax) {
			dataReceived += content;
			window[i++] = null;
			theSender.sendACK(seqNum);
		}
	}
}
