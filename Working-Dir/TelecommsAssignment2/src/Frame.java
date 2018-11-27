import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class Frame {
	Packet thePack;
	DatagramSocket theSocket;
	Timer timeoutTimer;
	DatagramPacket theDataPack;
	public static final int TIMEOUT_DELAY = 5000;
	public boolean isPlaceHolder;
	String data;

	public Frame() {
		isPlaceHolder = true;
	}

	public Frame(String data) {
		this.data = data;
		isPlaceHolder = false;
	}

	public Frame(Packet thePack, DatagramSocket theSocket) {
		this.thePack = thePack;
		this.theSocket = theSocket;
		isPlaceHolder = false;
	}
	
	public Frame(DatagramPacket thePacket) {
		theDataPack = thePacket;
	}
	
	public void forward() {
		try {
			theSocket.send(theDataPack);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send() {// send a packet
		try {
			theDataPack = thePack.toDatagramPacket();// packet -> datagramPacket
			timeoutTimer = new Timer();// start timeout timer
			timeoutTimer.schedule(new TimerTask() {// tell the timer what to do
				public void run() {
					timeoutTimer.cancel();// cancel existing timer
					send();// resend
				}
			}, Frame.TIMEOUT_DELAY);// Resend in x seconds
			theSocket.send(theDataPack);// send it! (for reals tho)
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void resend() {// resend a packet
		timeoutTimer.cancel();
		timeoutTimer.purge();
		send();
	}

	public void cancel() {// don't resend a packet
		timeoutTimer.cancel();
		timeoutTimer.purge();
		timeoutTimer = null;
	}
}
