
public class AckPacket extends Packet{
	public AckPacket(byte seqNum, byte seqTotal) {
		super(ACK, seqNum, seqTotal, "");
	}
}
