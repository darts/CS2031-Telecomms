import java.io.Serializable;

public class Content implements Serializable {
		private static final long serialVersionUID = 1L;
		byte type;
		String[] tgtInfo;// 0 = senderID, 1 = tgtID, 2 = senderPort,3 = tgtPort, 4 = packetID
		String data;

		public Content(byte type) {
			this.type = type;
			this.data = null;
			this.tgtInfo = null;
		}

		public Content(byte type, String[] tgtInfo, String data) {
			this(type);
			this.data = data;
			this.tgtInfo = tgtInfo;
		}

		public Content(byte type, String[] tgtInfo) {
			this(type);
			this.tgtInfo = tgtInfo;
		}
	}