import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Scanner;

public class UserInterface {
	public static final byte T_COMPUTERS = 0;
	public static final byte T_TELECOMMS = 1;
	public static final byte T_ALGORITHMS = 2;

	public static final String T_COMPUTERS_S = "Computers";
	public static final String T_TELECOMMS_S = "Telecomms";
	public static final String T_ALGORITHMS_S = "Algorithms";

	public static final String EXIT_KEYWORD = "quit";
	public static final String SUBSCRIBE_KEYWORD = "sub";
	public static final String SEND_INTERFACE_KEYWORD = "send";

	private boolean online;
	private CommPoint commPoint;

	public UserInterface(String tgtName, int tgtPort, DatagramSocket srcSocket) {
		this.online = true;
		try {
			commPoint = new CommPoint(tgtName, tgtPort, srcSocket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		uInterface();
	}

	private void uInterface() {
		Scanner userInputScanner = new Scanner(System.in);
		while (online) {
			System.out.print("Please enter a command: ");
			String userInput = userInputScanner.next();
			if (userInput.equals(EXIT_KEYWORD)) {
				online = false;
			} else if (userInput.equals(SEND_INTERFACE_KEYWORD)) {
				System.out.print("Please enter the name of the file you would like to send: ");
				try {
					String tgtFile = userInputScanner.next();
					File theFile = new File(tgtFile);
					BufferedReader theReader = new BufferedReader(new FileReader(theFile));
					String fData = "";
					String ln = "";
					while((ln = theReader.readLine()) != null)
						fData += ln;
					theReader.close();
					commPoint.startDataTransmission(fData, T_COMPUTERS);
				} catch (Exception e) {
					System.out.println("And error occured trying to open the file.");
				}
			} else if (userInput.equals(SUBSCRIBE_KEYWORD)) {

			} else {
				System.out.println("ERROR! Input not recognised!");
			}
		}
		userInputScanner.close();
	}

	public static String parseTopic(byte topic) {
		switch (topic) {
		case T_COMPUTERS:
			return T_COMPUTERS_S;
		case T_TELECOMMS:
			return T_TELECOMMS_S;
		case T_ALGORITHMS:
			return T_ALGORITHMS_S;
		default:
			return "ERROR! TOPIC UNKNOWN";
		}
	}
	
	private static String tmpData = "This is a test.";
}
