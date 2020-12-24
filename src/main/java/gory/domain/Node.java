package gory.domain;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Node implements INode { 
	@Getter
	private Set<INode> connectedNodes = new HashSet<>();
	
	@Override
	public void connect(INode node) {
		connectedNodes.add(node);
		node.getConnectedNodes().add(this);
	}
	
	@Override
	public boolean isConnectedTo(INode node) {
		return connectedNodes.contains(node);
	}
	
	@Override
	public int getDegree() {
		return connectedNodes.size();
	}
	
	@Override
	public double getClusteringCoefficientUsingTriangles() {
        int possible = 0;
        int actual = 0;
        Set<INode> seen = new HashSet<>();
        for (INode u : getConnectedNodes()) {
            seen.add(u);
        	for (INode w : getConnectedNodes()) {
            	if(seen.contains(w)) continue;
            	
            	possible++;
                if(u.isConnectedTo(w)) {
                    actual++;
                }
            }
        }
        return possible > 0 ? 1.0 * actual / possible : 0;
	}
	
	@Override
	public Double getW() {
		return null;
	}
}
