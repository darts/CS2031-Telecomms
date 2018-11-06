public class AckPacket extends Packet{
	public AckPacket(byte seqNum) {
		super(ACK, seqNum, "");
	}
}
