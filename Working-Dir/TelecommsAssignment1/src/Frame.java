import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class Frame {
	Packet thePack;
	DatagramSocket theSocket;
	Timer timeoutTimer;
	
	public Frame(Packet thePack, DatagramSocket theSocket) {
		this.thePack = thePack;
		this.theSocket = theSocket;
	}
	
	public void send() {
		try {
			theSocket.send(thePack.toDatagramPacket());
			timeoutTimer = new Timer();
			timeoutTimer.schedule(new TimerTask() {
				public void run() {
					timeoutTimer.cancel();
					send();
				}
			}, 7000);// Resend in 7 seconds
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void resend() {
		timeoutTimer.cancel();
		send();
	}
	
	public void cancel() {
		timeoutTimer.cancel();
	}
}
