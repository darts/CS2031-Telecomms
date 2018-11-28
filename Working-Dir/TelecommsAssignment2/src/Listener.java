//This was ripped directly from the first assignment.

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class Listener extends Thread{
	
	int port;
	DatagramSocket socket;
	final int MAX_SIZE = Packet.MAX_LENGTH_BYTES;
	public Listener(DatagramSocket socket) {
		this.socket = socket;
	}
	public Listener() {
	}
	
	public Listener(int port){
		init(port);
	}
	
	public void init(int port) {
		this.port = port;
		try {
			this.socket = new DatagramSocket(port);//create listening port
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.start();
	}
	
	public void run() {
		while(true) {
			DatagramPacket packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);//create a packet to listen to
			try {
				socket.receive(packet);//try to receive a packet
			} catch (IOException e) {
				e.printStackTrace();
			}
			packetRecieved(packet);//woop woop we got one! Great, now pass it on!
		}
	}
	
	public abstract void packetRecieved(DatagramPacket thePacket);//what happens when a packet is recieved
	
}
