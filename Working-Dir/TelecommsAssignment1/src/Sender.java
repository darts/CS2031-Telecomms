import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
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

	private Frame endF;
	private Frame strtF;

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

	public void sendData(String data) {
		this.data = data;
		this.start();
	}

	public void sendACK(byte packetNum) {
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.ACK, packetNum), srcSocket);
		theFrame.send();
		theFrame.cancel();// Don't wait for ACK
	}

	public void sendNAK(byte packetNum) {
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.NAK, packetNum), srcSocket);
		theFrame.send();
		theFrame.cancel();
	}

	public void sendSTRT(byte[] topic) {
		strtF = new Frame(new Packet(tgtAddr, Packet.STRT, STRT_NUM, topic), srcSocket);
		strtF.send();
		System.out.println("Sending STRT.");
		System.out.flush();
	}

	public void sendSTRT_ACK() {
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.STRT_ACK, STRT_NUM), srcSocket);
		theFrame.send();
		theFrame.cancel();
	}

	public void sendEND(byte lastPack) {
		endF = new Frame(new Packet(tgtAddr, Packet.END, lastPack), srcSocket);
		endF.send();
	}

	public void sendEND_ACK() {
		Frame theFrame = new Frame(new Packet(tgtAddr, Packet.END_ACK, STRT_NUM), srcSocket);
		theFrame.send();
		theFrame.cancel();
	}

	public void endEND() {
		this.endF.cancel();
		this.endF = null;
		for (int i = 0; i < frameArray.length; i++) {
			Frame theFrame = frameArray[i];
			if (theFrame != null) {
				theFrame.cancel();
				theFrame = null;
			}
		}
		frameArray = null;
//		this.interrupt();
	}

	public void endSTRT() {
		try {
			strtF.cancel();
			strtF = null;
		} catch (Exception e) {
			System.err.println("STRT Cancel Failed");
		}
	}

	private void sendData() {
		byte[][] stringsToSend = splitStr(data);// split the string into a series of strings
		int packetsToSend = stringsToSend.length; // number of packets to send
//		System.out.println(packetsToSend + " packets to send.");
		System.out.flush();
		byte pacNum = 0;
		int packetsSent = 0;
		do{ // while there are still packets to send
			while (activeFrames < DEF_WINDOW_WIDTH && packetsToSend > 0) {// while there is space in the window
				Packet thePack = new Packet(tgtAddr, Packet.DATA, pacNum, stringsToSend[packetsSent]);// send it!
				sendPacket(thePack);
				System.err.println("DATA Sent: " + pacNum);
				System.err.flush();
				if (pacNum + 1 >= WINDOW_MAX)
					pacNum = 0;
				else
					pacNum++;
				packetsToSend--; // one packet sent
				packetsSent++;// ^^
			}
//			System.out.println(packetsToSend + " packets to send.");
			System.out.flush();
		}while (packetsToSend > 0 && activeFrames > 0);
		if (pacNum == 0)
			pacNum = WINDOW_MAX - 1;
		else
			pacNum--;
		sendEND(pacNum);
		this.interrupt();
	}

	public void run() {
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
//		}
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
