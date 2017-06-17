package gory.domain;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public class Node {
	@Getter
	private Partition partition;

	@Getter
	private Set<Node> connectedNodes = new HashSet<>();
	
	public Node(Partition partition) {
		this.partition = partition;
	}
	
	@Override
	public String toString() {
		return partition.toString();
	}
	
	public void connect(Node node) {
		connectedNodes.add(node);
	}
	
	public int distanceTo(Node node) {
		return node.getPartition().distanceTo(partition);
	}
}
