import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CommPoint extends Listener implements ReceiverInterface{
	private Sender theSender;
	private DatagramSocket socket;
	private boolean dataToSendBool = false;
	private String dataToSend;
	private boolean subToSend = false;
	
	public CommPoint(String tgtName, int tgtPort, int srcPort) {
		try {
			this.socket = new DatagramSocket(srcPort);
			this.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		theSender = new Sender(tgtName, tgtPort, srcPort);
	}
	
	public void startDataTransmission(String theData) {
		dataToSend = theData;
		dataToSendBool = true;
		theSender.sendSTRT();
	}

	@Override
	public void packetRecieved(DatagramPacket thePacket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ACKReceived() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void NAKReceived() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void STRTReceived() {
		theSender.sendSTRT_ACK();
		
	}

	@Override
	public void STRT_ACKReceived() {
		if(dataToSendBool)
			theSender.sendData(dataToSend);
	}

	@Override
	public void ENDReceived() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void END_ACKReceived() {
		// TODO Auto-generated method stub
		
	}
	
}
