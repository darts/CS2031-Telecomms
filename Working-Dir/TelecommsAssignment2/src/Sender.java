import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class Sender extends Thread {
	int srcPort; // where packets are sent from
	DatagramSocket srcSocket; // ^^
	int tgtPort;// target port
	String tgtName;// target name
	InetSocketAddress tgtAddr;// target address

	private Frame activeFrame;
	
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
	
	private Packet currentPacket = null;

	public void sendData(String data) {// send data
		this.currentPacket = new Packet(tgtAddr,tgtName, data);
		this.start();// spin up a new thread to send data
	}

	public void run() {//new thread
		sendData();
	}
	
	private void sendData() {
		sendPacket(currentPacket);
		activeFrame.cancel();
		this.interrupt();
	}

	private void sendPacket(Packet thePack) {// send a packet
		activeFrame = new Frame(thePack, srcSocket);// new frame in array
		activeFrame.send();
	}

	public void ackRecieved() { // ack-packet received
		activeFrame.cancel();
	}
}
