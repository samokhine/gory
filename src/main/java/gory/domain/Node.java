package gory.domain;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public class Node extends Partition {
	@Getter
	private Set<Node> connectedNodes = new HashSet<>();
	
	public Node(Partition partition) {
		super(partition.getSummands());
	}
	
	public void connect(Node node) {
		connectedNodes.add(node);
		node.getConnectedNodes().add(this);
	}
	
	public boolean isConnectedTo(Node node) {
		return connectedNodes.contains(node);
	}
	
	public int getDegree() {
		return connectedNodes.size();
	}
}
