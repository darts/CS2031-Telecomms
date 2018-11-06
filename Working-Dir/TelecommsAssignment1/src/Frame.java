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
	
	public Frame(Packet thePack, DatagramSocket theSocket) {
		this.thePack = thePack;
		this.theSocket = theSocket;
	}
	
	public void send() {
		try {
			theDataPack = thePack.toDatagramPacket();//packet -> datagramPacket
			theSocket.send(theDataPack);//send it! (for reals tho)
			timeoutTimer = new Timer();//start timeout timer
			timeoutTimer.schedule(new TimerTask() {//tell the timer what to do
				public void run() {
					timeoutTimer.cancel();//cancel existing timer
					send();//resend
				}
			}, 7000);// Resend in 7 seconds
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void resend() {//resend a packer
		timeoutTimer.cancel();
		send();
	}
	
	public void cancel() {//don't resend a packet
		timeoutTimer.cancel();
	}
}
