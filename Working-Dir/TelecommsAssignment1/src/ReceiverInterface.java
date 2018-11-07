public interface ReceiverInterface {	
	public void ACKReceived(byte seqNum);
	public void NAKReceived(byte seqNum);
	public void DATAReceived(byte[] data);
	public void STRTReceived(byte[] data);
	public void STRT_ACKReceived();
	public void ENDReceived(byte[] data);
	public void END_ACKReceived();
	public void SUBReceived();
	public void USUBReceived();
}
