
public class myTuple {
	public myTuple(int nodeId, double cost, int index, double capacity, int preNode) {
		super();
		this.nodeId = nodeId;
		this.cost = cost;
		this.index = index;
		this.capacity = capacity;
		this.preNode = preNode;
	}

	
	
	int nodeId;
	double cost;
	int index;
	double capacity;
	int preNode;

	public myTuple() {
		this.cost = -1.0;
		
		// TODO Auto-generated constructor stub
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getPreNode() {
		return preNode;
	}

	public void setPreNode(int preNode) {
		this.preNode = preNode;
	}

}
