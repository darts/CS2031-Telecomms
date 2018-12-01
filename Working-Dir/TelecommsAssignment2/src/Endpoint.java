import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Endpoint extends CommPoint {
	public static String PREFIX = "E";
	public static int DEFAULT_PORT = 50000;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private Frame activePacket;
	private boolean awaitingData;

	public Endpoint() throws SocketException {
		super(new DatagramSocket(DEFAULT_PORT));
		activePacket = null;
		awaitingData = false;
	}

	public void ACKReceived(DatagramPacket thePacket) {

	}

	public void HELLOReceived(DatagramPacket thePacket) {

	}

	public boolean DATAReceived(DatagramPacket thePacket) {
		return false;
	}

	public void UPDATEReceived(DatagramPacket thePacket) {

	}

	public void HELPReceived(DatagramPacket thePacket) {

	}

	public void sendDATA(String dst, String data) {

	}

	private class UserInterface extends Thread {
		private Endpoint parent;
		private boolean online;
		private String QUIT = "QUIT";

		public UserInterface(Endpoint parent) {
			this.online = true;
			this.parent = parent;
			this.start();
		}

		public void run() {
			Scanner userInputScanner = new Scanner(System.in);
			while (online) {
				System.out.print("Please enter a destination:");
				String dst = userInputScanner.next();
				if (dst.equals(QUIT))
					online = false;
				else {
					System.out.print("Please enter the text you would like to send:");
					String dataToSend = userInputScanner.nextLine();
					parent.sendDATA(dst, dataToSend);
				}
			}
			userInputScanner.close();
		}
	}

}
