public interface ReceiverInterface { // interface to define all of the required funtions for receiving
	public void ACKReceived(byte seqNum);

	public void NAKReceived(byte seqNum);

	public void DATAReceived(byte[] data);

	public void STRTReceived(byte[] data);

	public void STRT_ACKReceived();

	public void ENDReceived(byte[] data);

	public void END_ACKReceived();

	public void SUBReceived(byte data);

	public void USUBReceived(byte data);

	public void MGMTReceived(byte[] data);

	public void MGMT_ACKReceived();
	
	public void SUB_ACKReceived();
}
