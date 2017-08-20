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
	
	public double getClusteringCoefficientUsingTriangles() {
        int possible = 0;
        int actual = 0;
        Set<Node> seen = new HashSet<>();
        for (Node u : getConnectedNodes()) {
            seen.add(u);
        	for (Node w : getConnectedNodes()) {
            	if(seen.contains(w)) continue;
            	
            	possible++;
                if(u.isConnectedTo(w)) {
                    actual++;
                }
            }
        }
        return possible > 0 ? 1.0 * actual / possible : 0;
	}
}
