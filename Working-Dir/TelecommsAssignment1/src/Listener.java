import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class Listener extends Thread{
	
	int port;
	DatagramSocket socket;
	final int MAX_SIZE = Packet.MAX_LENGTH_BITS;
	
	public Listener(int port){
		this.port = port;
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.start();
	}
	
	public void run() {
		while(true) {
			DatagramPacket packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			packetRecieved(packet);
		}
	}
	
	public abstract void packetRecieved(DatagramPacket thePacket);
	
}
