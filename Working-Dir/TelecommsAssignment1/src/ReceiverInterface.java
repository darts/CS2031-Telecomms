public interface ReceiverInterface {	
	public void ACKReceived();
	public void NAKReceived();
	public void DATAReceived(byte[] data);
	public void STRTReceived();
	public void STRT_ACKReceived();
	public void ENDReceived();
	public void END_ACKReceived();
	public void SUBReceived();
	public void USUBReceived();
}
