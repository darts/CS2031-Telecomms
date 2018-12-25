import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RoutingTable {// this is used by the controller to map packet paths
	private Map<List<String>, Path> pathMap;

	public RoutingTable() { //initialize the object
		pathMap = new HashMap<List<String>, Path>();
	}

	//Add a path to the map
	public void addPath(String dst, String src, String[] rtList, String[] inList, String[] outList) {
		pathMap.put(Arrays.asList(dst, src), new Path(rtList, inList, outList));
	}

	//get a path from the map
	public Path getPath(String dst, String src) {
		return pathMap.get(Arrays.asList(dst, src));
	}
	
	//does the object contain a path with this dst and src?
	public boolean checkContains(String dst, String src) {
		return pathMap.containsKey(Arrays.asList(dst, src));
	}
	
	public class Path { //the path object
		public String[] rtList; //list of routers
		public String[] inList;	//packet source
		public String[] outList; //packet destination

		public Path(String[] rtList, String[] inList, String[] outList) {
			this.rtList = rtList;
			this.inList = inList;
			this.outList = outList;
		}
	}
}
