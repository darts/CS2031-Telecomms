import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class Sender extends Thread {
	public static final byte STRT_NUM = -1;
	public static final int DEF_WINDOW_WIDTH = 5;// window
	public static final int WINDOW_MAX = (DEF_WINDOW_WIDTH * 2);// whole range
	int srcPort; // where packets are sent from
	DatagramSocket srcSocket; // ^^
	int tgtPort;// target port
	String tgtName;// target name
	InetSocketAddress tgtAddr;// target address
	public Frame[] frameArray = new Frame[WINDOW_MAX];// array of active frames
	private String data;

	private Frame endF; // the end frame
	private Frame strtF; // the start frame
	private Frame mgmtF; // the management frame
	private Frame subF; // the subscription frame

	public Sender(String tgtName, int tgtPort, DatagramSocket srcSocket) {
		try {
			this.srcSocket = srcSocket;
			this.tgtPort = tgtPort;
			this.tgtName = tgtName;
			tgtAddr = new InetSocketAddress(this.tgtName, this.tgtPort); // get an address
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int activeFrames = 0;// number of frames currently active

	public void sendData(String data) {// send data
		this.data = data;
		this.start();// spin up a new thread to send data
	}

	public void sendACK(byte packetNum) {
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.ACK, packetNum), srcSocket);
		theFrame.send();
		theFrame.cancel();// Don't wait for ACK
	}

	public void sendNAK(byte packetNum) {
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.NAK, packetNum), srcSocket);
		theFrame.send();
		theFrame.cancel();// don't wait for ack
	}

	public void sendSTRT(byte[] topic) {// send a start packet
		strtF = new Frame(new Packet(tgtAddr, Packet.STRT, STRT_NUM, topic), srcSocket);
		strtF.send();
		System.out.println("Sending STRT.");
		System.out.flush();
	}

	public void sendSTRT_ACK() {// send start ack
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.STRT_ACK, STRT_NUM), srcSocket);
		theFrame.send();
		theFrame.cancel();// don't wait for ack
	}

	public void sendEND(byte lastPack) {// send end
		endF = new Frame(new Packet(tgtAddr, Packet.END, lastPack), srcSocket);
		endF.send();
	}

	public void sendEND_ACK() {// send end ack
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.END_ACK, STRT_NUM), srcSocket);
		theFrame.send();
		theFrame.cancel();// don't wait for ack
	}

	public void sendMGMT(int pNum) { // send management frame
		mgmtF = new Frame(new Packet(tgtAddr, Packet.MGMT, pNum), srcSocket);
		mgmtF.send();
	}

	public void sendMGMT_ACK() {// send management ack
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.MGMT_ACK, STRT_NUM), srcSocket);
		theFrame.send();
		theFrame.cancel();//don't wait for ack
	}

	public void endMGMT() {//cancel the timeout 
		mgmtF.cancel();
		mgmtF = null;
	}

	public void sendSUB(byte topic) {//send subscription
		subF = new Frame(new Packet(tgtAddr, Packet.SUB, STRT_NUM), srcSocket);
		subF.send();
	}

	public void sendUSUB(byte topic) {//send un-subscription
		subF = new Frame(new Packet(tgtAddr, Packet.USUB, STRT_NUM), srcSocket);
		subF.send();
	}

	public void sendEND_SUB() {//send end-sub
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.SUB_ACK, STRT_NUM), srcSocket);
		theFrame.send();
		theFrame.cancel();//don't wait for ack
	}

	public void endSUB() {//sub-ack received 
		subF.cancel();//cancel timeout
		subF = null;
	}

	public void endEND() {//end end timeout
		this.endF.cancel();//cancel timeout
		this.endF = null;
		for (int i = 0; i < frameArray.length; i++) {//clear frames from window
			Frame theFrame = frameArray[i];
			if (theFrame != null) {
				theFrame.cancel();
				theFrame = null;
			}
		}
		frameArray = null;
	}

	public void endSTRT() {//end STRT timeout
			strtF.cancel(); //cancel timeout
			strtF = null;
	}

	private void sendData() {
		byte[][] stringsToSend = splitStr(data);// split the string into a series of strings
		int packetsToSend = stringsToSend.length; // number of packets to send
		System.out.flush();
		byte pacNum = 0; //sequence number
		int packetsSent = 0;//number of packets sent
		do { // while there are still packets to send
			while (activeFrames < DEF_WINDOW_WIDTH && packetsToSend > 0) {// while there is space in the window
				Packet thePack = new Packet(tgtAddr, Packet.DATA, pacNum, stringsToSend[packetsSent]);// send it!
				sendPacket(thePack);//send it!
				System.out.println("DATA Sent: " + pacNum);
				System.out.flush();
				if (pacNum + 1 >= WINDOW_MAX)
					pacNum = 0;
				else
					pacNum++;
				packetsToSend--; // one packet sent
				packetsSent++;// ^^
			}
			System.out.flush();
		} while (packetsToSend > 0 && activeFrames > 0);
		if (pacNum == 0)
			pacNum = WINDOW_MAX - 1;
		else
			pacNum--;
		sendEND(pacNum);
		this.interrupt();
	}

	public void run() {//new thread
		sendData();
	}

	private void sendPacket(Packet thePack) {// send a packet
		frameArray[thePack.seqNum] = new Frame(thePack, srcSocket);// new frame in array
		frameArray[thePack.seqNum].send();// send it!
		activeFrames++;// frame is now active
	}

	public void ackRecieved(int index) { // ack-packet received
		try {// in case of null pointer
			frameArray[index].cancel(); // cancel frame timeout timer
		} catch (Exception e) {
			System.err.println("Error Cancelling Packet Timeout: " + index);
		}
		frameArray[index] = null; // nullify frame
		activeFrames--;// frame is no longer active
	}

	public void nakRecieved(int index) { // nak-packet received
		if (index < frameArray.length && index >= 0) {// is in range of array
			frameArray[index].resend();// resend that sucker!
		}
	}

	private byte[][] splitStr(String theString) {// split a string into a series of byte arrays
		byte[] strByteArr = theString.getBytes(Packet.DEF_ENCODING);// string -> byte[]
		int noOfStrings = strByteArr.length / Packet.MAX_LENGTH_BYTES;// number of byte[] required
		if (strByteArr.length % Packet.MAX_LENGTH_BYTES != 0)// check for a shorter byte[] on the end
			noOfStrings++;
		byte[][] resByte = new byte[noOfStrings][];// new byte array array
		int start = 0;
		// if the length of the array is less than the max, use it as the max
		int end = (strByteArr.length < Packet.MAX_LENGTH_BYTES) ? strByteArr.length : Packet.MAX_LENGTH_BYTES;
		for (int i = 0; i < resByte.length; i++) {// for every byte array
			resByte[i] = Arrays.copyOfRange(strByteArr, start, end);// copy section of byte[]
			start += Packet.MAX_LENGTH_BYTES;
			end = ((end + Packet.MAX_LENGTH_BYTES) > strByteArr.length) ? strByteArr.length
					: (end + Packet.MAX_LENGTH_BYTES);
		}
		return resByte;
	}
}
