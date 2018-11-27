public class RoutingTable {//this is used by the controller
	public int dst;
	public int src;
	public int[][] rtList;
	public int[][] inList;
	public int[][] outList;
	
	public RoutingTable(int dst, int src, int[][] rtList, int[][] inList, int[][] outList) {
		this.dst = dst;
		this.src = src;
		this.rtList = rtList;
		this.inList = inList;
		this.outList = outList;
	}
}
