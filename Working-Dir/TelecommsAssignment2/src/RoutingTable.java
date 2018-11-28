import java.util.HashMap;
import java.util.Map;

public class RoutingTable {// this is used by the controller
	private Map<String[], Path> pathMap;

	public RoutingTable() {
		pathMap = new HashMap<String[], Path>();
	}

	public void addPath(String dst, String src, String[] rtList, String[] inList, String[] outList) {
		pathMap.put(new String[] {dst, src}, new Path(rtList, inList, outList));
	}

	public Path getPath(String dst, String src) {
		return pathMap.get(new String[] {dst, src});
	}
	
	public class Path {
		public String[] rtList;
		public String[] inList;
		public String[] outList;

		public Path(String[] rtList, String[] inList, String[] outList) {
			this.rtList = rtList;
			this.inList = inList;
			this.outList = outList;
		}
	}
}
