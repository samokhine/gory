package gory.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of={"name"}, callSuper=false)
public class SimpleNode extends Node {
	@Getter
	private String name;

	@Getter @Setter
	private Integer id;
	
	public SimpleNode(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public INode cloneIt() {
		INode clone = new SimpleNode(name);
		clone.getConnectedNodes().addAll(getConnectedNodes());
		return clone;
	}

	@Override
	public int distanceTo(INode node) {
		return node.isConnectedTo(this) ? 1 : 0;
	}
}
