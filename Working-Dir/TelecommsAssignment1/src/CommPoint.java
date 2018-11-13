import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class CommPoint extends Listener implements ReceiverInterface {
	private Sender theSender; // for sending stuff
	private DatagramSocket socket; // sending socket
	private boolean dataToSendBool = false; // still data to send
	private String dataToSend; // what is to be sent
	private String dataReceived = "";// what has been received
	private Frame[] window;// the window of active frames
	private byte windowMin;// current min in window
	private byte windowMax;// current max in window
	private byte topic;// what topic is active
	private String tgtName;// who is being transmitted to
	private int tgtPort;// what port is being transmitted to
	private int recNum = 0;// name of active file for receiving data
	private BufferedWriter writer;// to write received data to file
	private boolean receiveComplete = false;// is data still being received
	public boolean isPub;// Am I a publisher? Am I? Mom?
	private String lastRec;// last received string
	private byte lastTopic;// last received topic
	private ArrayList<Byte> subList = new ArrayList<Byte>();// list of new subscriptions
	private ArrayList<Byte> uSubList = new ArrayList<Byte>();// list of new un-subscriptions

	public CommPoint(String tgtName, int tgtPort, DatagramSocket srcPort) {
		super(srcPort);// create a listener
		this.tgtName = tgtName;
		this.tgtPort = tgtPort;
		this.socket = srcPort;
		init();// reset all variables
		try {
			this.start();// spool up a new thread
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CommPoint(String tgtName, int tgtPort, int sPort, DatagramSocket srcPort, boolean isPub) {
		this(tgtName, tgtPort, srcPort);// call super-man/constructor
		this.isPub = isPub;
		theSender.sendMGMT(sPort);// tell contact what port to talk to
	}

	public void startDataTransmission(String theData, byte type) {// send some data
		init();// reset variables
		dataToSend = theData;
		dataToSendBool = true;
		byte[] theType = { type };
		theSender.sendSTRT(theType);// send a start packet
	}

	// determine what type of packet has been received.
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
			this.SUBReceived(Packet.getTopic(thePacket.getData()));
			break;
		case Packet.USUB:
			this.USUBReceived(Packet.getTopic(thePacket.getData()));
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
			break;
		case Packet.MGMT:
			this.MGMTReceived(thePacket.getData());
			break;
		case Packet.MGMT_ACK:
			this.MGMT_ACKReceived();
			break;
		case Packet.SUB_ACK:
			this.SUB_ACKReceived();
			break;
		}
	}

	public void ACKReceived(byte seqNum) {// got an ACK
		System.out.println("ACK Received: " + seqNum);
		System.out.flush();
		theSender.ackRecieved(seqNum);// tell the sender to cancel timeouts
	}

	public void NAKReceived(byte seqNum) {// got a NAK
		theSender.nakRecieved(seqNum);// tell the sender to re-send the missing packet
		System.out.println("NAK Received: " + seqNum + "  ->Resending...");
	}

	public void STRTReceived(byte[] data) {// got a STRT message
		init();// reset variables
		try {// start file writer for received data
			writer = new BufferedWriter(new FileWriter(Integer.toString(recNum++)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.topic = Packet.getTopic(data);// set the conversation topic
		try {
			writer.write(UserInterface.parseTopic(topic) + "\n");// write the topic to the start of the file
		} catch (IOException e) {
			e.printStackTrace();
		}
		;
		System.out.println("\nSTRT Received Topic: " + UserInterface.parseTopic(topic) + "  ->Sending STRT_ACK");
		theSender.sendSTRT_ACK();// tell the sender to ACK the STRT request
	}

	public void STRT_ACKReceived() { // got a STRT ACK -> they can hear us
		System.out.println("STRT_ACK Received     ->Beginning comms...");
		theSender.endSTRT(); // end the STRT timeout timer
		if (dataToSendBool)// is there something to send?
			theSender.sendData(dataToSend);// send it
	}

	public void ENDReceived(byte[] data) { // all data should be received
		if (!nakMissingPackets(Packet.getSeqNum(data))) {// all packets accounted for
			theSender.sendEND_ACK();// confirm transmission over
			System.out.println("END Received      ->Sending END_ACK");
			System.out.println("Data Received: " + this.dataReceived);
			init();// reset variables
			try {// flush any remaining data to the drive
				writer.flush();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.receiveComplete = true;// game over, man
		}

	}

	public void END_ACKReceived() {// transmission end confirmed
		theSender.endEND();// tell the sender to cancel the END timeout
		System.out.println("END_ACK Received      ->Connection Closed");
	}

	public String getData() {// if something has been received -> pass it on
		if (this.receiveComplete == true && !this.lastRec.equals("")) {
			this.receiveComplete = false;
			return this.lastRec;
		}
		return null;// Got nothing, boss
	}

	public byte getTopic() {// what topic has been received
		return this.lastTopic;
	}

	public void DATAReceived(byte[] data) {// got a DATA packet
		handleWindow(data);// pass it to the window manager
	}

	public void SUBReceived(byte data) {// SUB packet received
		if (!this.subList.contains(data))// if not already subbed (duplicate)
			this.subList.add(data);// sub
		theSender.sendEND_SUB();// confirm transmission received
	}

	public void USUBReceived(byte data) {// USUB packet received
		if (!this.uSubList.contains(data))// if not already unsubbed (duplicate)
			this.uSubList.add(data);// unsub
		theSender.sendEND_SUB();// confirm transmission recieved
	}

	private void handleWindow(byte[] data) {// manage the sliding window of incoming packets
		byte seqNum = Packet.getSeqNum(data);// find the sequence number
		System.err.flush();
		if (isInRange(seqNum)) {// packet is in window range
			if (seqNum == windowMin) {// Packet is as anticipated
				theSender.sendACK(seqNum); // ACK the data
				String dataRec = Packet.getContents(data);
				dataReceived = dataReceived + dataRec;// concat the data
				advanceWindow();// shift window (moving along swiftly)
				flushWindow(seqNum);// flush other packets
				try {// write rec data to file
					writer.write(dataRec);
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("DATA Received " + seqNum + "     ->Sending ACK");
			} else {
				window[seqNum] = new Frame(Packet.getContents(data));
				placePlaceHolders(seqNum); // place blank frames to hold location
				nakMissingPackets(seqNum); // nak absent packets
				System.out.println("DATA Received   OUT OF ORDER!!  ->Sending NAK");
			}
		}
	}

	private boolean isInRange(byte seqNum) {// in in range of the window
		byte[] winLocs = getWindowLocs(windowMax, windowMin, Sender.DEF_WINDOW_WIDTH, Sender.WINDOW_MAX);
		for (int i = 0; i < winLocs.length; i++) {
			if (winLocs[i] == seqNum)
				return true;
		}
		return false;
	}

	private boolean nakMissingPackets(byte seqNum) {// send a NAK for any missing packets
		boolean retBool = false;
		byte[] winFrames = CommPoint.getWindowLocs(windowMax, windowMin, Sender.DEF_WINDOW_WIDTH, Sender.WINDOW_MAX);
		for (byte i : winFrames) {// resend missing packets
			Frame theFrame = window[i];
			if (theFrame != null && theFrame.isPlaceHolder)
				theSender.sendNAK(i);
		}
		return retBool;
	}

	private void placePlaceHolders(byte seqNum) {// placeholders for missing packets
		byte[] winFrames = CommPoint.getWindowLocs(windowMax, windowMin, Sender.DEF_WINDOW_WIDTH, Sender.WINDOW_MAX);
		boolean found = false;
		int loc = -1;
		for (int i = 0; i < winFrames.length && !found; i++) {// from min -> last packet received
			if (winFrames[i] == seqNum) {
				loc = i;
				found = true;
			}
		}
		for (int i = loc; i >= 0; i--) {// add placeholder frames where needed
			if (window[i] == null)
				window[i] = new Frame();
		}
	}

	// find the active window frames. eg 7,8,9,0,1
	public static byte[] getWindowLocs(byte winMax, byte winMin, int winWidth, int size) {
		byte[] resArr = new byte[winWidth];
		int j = 0;
		if (winMax > winMin) {
			for (byte i = winMin; i < winMax; i++) {
				resArr[j++] = i;
			}
			return resArr;
		}
		for (byte i = winMin; i < size; i++) {
			resArr[j++] = i;
		}
		for (byte i = 0; i < winMax; i++) {
			resArr[j++] = i;
		}
		return resArr;
	}

	private void advanceWindow() {// move window along by 1 frame
		if ((windowMin + 1) == Sender.WINDOW_MAX) { // move along unless at the top
			windowMin = 0;
		} else {
			windowMin++;
		}
		if ((windowMax + 1) == Sender.WINDOW_MAX) { // move along unless at the top
			windowMax = 0;
		} else {
			windowMax++;
		}
	}

	private void flushWindow(byte seqNum) {// remove any packets in the window
		byte[] winFrames = CommPoint.getWindowLocs(windowMax, windowMin, Sender.DEF_WINDOW_WIDTH, Sender.WINDOW_MAX);
		for (byte i = (byte) (winFrames.length - 1); i >= 0; i--) {
			if (window[winFrames[i]] != null) {// if a packet has been received
				String dataRec = window[winFrames[i]].data;
				dataReceived += dataRec;
				try {
					writer.write(dataRec);
				} catch (IOException e) {
					e.printStackTrace();
				}
				theSender.sendACK(winFrames[i]);// ACK the packet
			}
		}
	}

	private void init() {// reset important values
		dataToSendBool = false;
		dataToSend = null;
		this.lastTopic = topic;
		topic = -1;
		this.lastRec = this.dataReceived;
		this.dataReceived = "";
		this.window = new Frame[Sender.WINDOW_MAX];
		this.windowMax = Sender.DEF_WINDOW_WIDTH;
		this.windowMin = 0;
		theSender = new Sender(tgtName, tgtPort, socket);
	}

	public void MGMTReceived(byte[] data) {// MGMT frame received
		this.tgtPort = Packet.getDataByte(data);// set the target port to that received
		System.out.println("New TargetPort Received: " + tgtPort);
		init();// reset important values
		theSender.sendMGMT_ACK();// ACK the MGMT frame
	}

	public void MGMT_ACKReceived() {// MGMT ACK received
		theSender.endMGMT();// end MGMT timout timer
	}

	public ArrayList<Byte> getSubs() {// return list of new subscriptions
		ArrayList<Byte> retList = subList;
		subList = new ArrayList<Byte>();// reset list
		return retList;
	}

	public ArrayList<Byte> getUSubs() {// return list of new un-subscriptions
		ArrayList<Byte> retList = uSubList;
		uSubList = new ArrayList<Byte>();// reset list
		return retList;
	}

	public void sendSUB(byte topic) {// send a sub packet
		theSender.sendSUB(topic);
	}

	public void sendUSUB(byte topic) {// send and un-sub packet
		theSender.sendUSUB(topic);
	}

	public void SUB_ACKReceived() {
		theSender.endSUB();
	}
}
