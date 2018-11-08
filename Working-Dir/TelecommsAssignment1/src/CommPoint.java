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
	private Frame[] window;
	private byte windowMin;
	private byte windowMax;
	private boolean windowValid;
	private byte topic;

	public CommPoint(String tgtName, int tgtPort, DatagramSocket srcPort) {
		super(srcPort);
		try {
			this.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		theSender = new Sender(tgtName, tgtPort, srcPort);
	}

	public void startDataTransmission(String theData, byte type) {
		dataToSend = theData;
		dataToSendBool = true;
		byte[] theType = { type };
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
		System.out.println("ACK Received: " + seqNum);
		theSender.ackRecieved(seqNum);
	}

	public void NAKReceived(byte seqNum) {
		theSender.nakRecieved(seqNum);
		System.out.println("NAK Received: " + seqNum + "  ->Resending...");
	}

	public void STRTReceived(byte[] data) {
		this.topic = Packet.getTopic(data);
		System.out.println("\nSTRT Received Topic: " + UserInterface.parseTopic(topic) + "  ->Sending STRT_ACK");
		theSender.sendSTRT_ACK();
		this.dataReceived = "";
		this.window = new Frame[Sender.WINDOW_MAX];
		this.windowMax = Sender.WINDOW_MAX - 1;
		this.windowMin = 0;
		this.windowValid = true;
	}

	public void STRT_ACKReceived() {
		System.out.println("STRT_ACK Received     ->Beginning comms...");
		theSender.endSTRT();
		if (dataToSendBool)
			theSender.sendData(dataToSend);
	}

	public void ENDReceived(byte[] data) {
		if (!nakMissingPackets(Packet.getSeqNum(data))) {// all packets accounted for
			theSender.sendEND_ACK();
			System.out.println("END Received      ->Sending END_ACK");
			System.out.println("Data Received: " + this.dataReceived);
		}
	}

	public void END_ACKReceived() {
		theSender.endEND();
		System.out.println("END_ACK Received      ->Connection Closed");
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
		if (seqNum < windowMax && seqNum >= windowMin) {// packet is in window range
			if (seqNum == windowMin) {// Packet is as anticipated
				theSender.sendACK(seqNum);
				System.out.println(dataReceived.length() + " length1");
				dataReceived = dataReceived + Packet.getContents(data);
				System.out.println(dataReceived.length() + " length2");
				advanceWindow();
				flushWindow(seqNum);
				System.out.println("DATA Received      ->Sending ACK");
			} else {
				window[seqNum] = new Frame(Packet.getContents(data));
				placePlaceHolders(seqNum);
				nakMissingPackets(seqNum); // nak absent packets
				System.out.println("DATA Received   OUT OF ORDER!!  ->Sending NAK");
			}
		}
	}

	private boolean nakMissingPackets(byte seqNum) {
		boolean retBool = false;
		byte[] winFrames = CommPoint.getWindowLocs(windowMax, windowMin, Sender.DEF_WINDOW_WIDTH, Sender.WINDOW_MAX);
		for (byte i : winFrames) {// resend missing packets
			Frame theFrame = window[i];
			if (theFrame != null && theFrame.isPlaceHolder)
				theSender.sendNAK(i);
		}
		return retBool;
	}

	private void placePlaceHolders(byte seqNum) {
		byte[] winFrames = CommPoint.getWindowLocs(windowMax, windowMin, Sender.DEF_WINDOW_WIDTH, Sender.WINDOW_MAX);
		boolean found = false;
		int loc = -1;
		for (int i = 0; i < winFrames.length && !found; i++) {
			if (winFrames[i] == seqNum) {
				loc = i;
				found = true;
			}
		}
		for (int i = loc; i >= 0; i--) {
			if(window[i] == null)
				window[i] = new Frame();
		}
	}

	public static byte[] getWindowLocs(byte winMax, byte winMin, int winWidth, int size) {
		byte[] resArr = new byte[winWidth];
		int j = 0;
		if (winMax > winMin) {
			for (byte i = winMin; i <= winMax; i++) {
				resArr[j] = i;
			}
			return resArr;
		}
		for (byte i = winMin; i < size; i++) {
			resArr[j] = i;
		}
		for (byte i = 0; i <= winMax; i++) {
			resArr[j] = i;
		}
		return resArr;
	}

	private void advanceWindow() {
		if (windowMin == Sender.WINDOW_MAX - 1) {
			windowMin = 0;
		} else {
			windowMin++;
		}
		if (windowMax == Sender.WINDOW_MAX - 1) {
			windowMax = 0;
		} else {
			windowMax++;
		}
	}

	private void flushWindow(byte seqNum) {// remove any packets in the window
		byte[] winFrames = CommPoint.getWindowLocs(windowMax, windowMin, Sender.DEF_WINDOW_WIDTH, Sender.WINDOW_MAX);
		for (byte i = (byte)(winFrames.length - 1); i >= 0; i--) {
			if(window[winFrames[i]] != null) {
				dataReceived += window[winFrames[i]].data;
				theSender.sendACK(seqNum);
			}
		}
	}
}
