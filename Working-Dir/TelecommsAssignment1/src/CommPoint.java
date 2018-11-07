import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CommPoint extends Listener implements ReceiverInterface{
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
	
	public CommPoint(String tgtName, int tgtPort, int srcPort) {
		try {
			this.socket = new DatagramSocket(srcPort);
			this.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		theSender = new Sender(tgtName, tgtPort, srcPort);
	}
	
	public void startDataTransmission(String theData) {
		dataToSend = theData;
		dataToSendBool = true;
		theSender.sendSTRT();
	}

	@Override
	public void packetRecieved(DatagramPacket thePacket) {
		byte type = Packet.getType(thePacket);
		switch(type) {
		case Packet.ACK: this.ACKReceived();
		break;
		case Packet.NAK: this.NAKReceived();
		break;
		case Packet.DATA: this.DATAReceived(thePacket.getData());
		break;
		case Packet.SUB: this.SUBReceived();
		break;
		case Packet.USUB: this.USUBReceived();
		break;
		case Packet.STRT: this.STRTReceived();
		break;
		case Packet.STRT_ACK: this.STRT_ACKReceived();
		break;
		case Packet.END: this.ENDReceived();
		break;
		case Packet.END_ACK: this.END_ACKReceived();
		}
	}

	public void ACKReceived() {
		// TODO Auto-generated method stub
		
	}

	public void NAKReceived() {
		// TODO Auto-generated method stub
		
	}

	public void STRTReceived() {
		theSender.sendSTRT_ACK();
		this.dataReceived = "";
		this.window = new String[Sender.WINDOW_MAX];
		this.windowMax = Sender.WINDOW_MAX - 1;
		this.windowMin = 0;
		this.windowValid = true;
	}

	public void STRT_ACKReceived() {
		theSender.endSTRT();
		if(dataToSendBool)
			theSender.sendData(dataToSend);
	}

	public void ENDReceived() {
		// TODO Auto-generated method stub
		
	}

	public void END_ACKReceived() {
		// TODO Auto-generated method stub
		
	}

	public void DATAReceived(byte[] data) {
		this.dataReceived += Packet.getContents(data);
	}

	public void SUBReceived() {
		// TODO Auto-generated method stub
		
	}

	public void USUBReceived() {
		// TODO Auto-generated method stub
		
	}
	
	private void handleWindow(byte[] data) {
		byte seqNum = Packet.getSeqNum(data);
		if(seqNum < windowMax && seqNum > windowMin) {//packet is in window range
			if(seqNum == windowMin) {//Packet is as anticipated
				dataReceived += Packet.getContents(data);
				windowMax++;
				windowMin++;
			}else {
				window[seqNum] = Packet.getContents(data);
				nakMissingPackets(seqNum); //nak absent packets
			}
		}
	}
	
	private void nakMissingPackets(byte seqNum) {
		for(byte i = seqNum; i >= windowMin; i--) {//resend missing packets
			if(window[i] == null)
				theSender.sendNAK(i);
		}
	}
	
}
