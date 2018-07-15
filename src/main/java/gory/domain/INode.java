package gory.domain;

import java.util.Set;

public interface INode {
	public INode clone(INode node);
	public int distanceTo(INode node);
	public void connect(INode node);
	public Set<INode> getConnectedNodes();
	public boolean isConnectedTo(INode node);
	public int getDegree();
	public double getClusteringCoefficientUsingTriangles();
	
	public Integer getId();
	public void setId(Integer id);
}
