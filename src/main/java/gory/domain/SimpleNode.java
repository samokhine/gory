package gory.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SimpleNode extends Node {
	@Getter
	private String name;

	@Override
	public String toString() {
		return name;
	}

	@Override
	public INode clone(INode node) {
		return new SimpleNode(name);
	}

	@Override
	public int distanceTo(INode node) {
		return node.isConnectedTo(this) ? 1 : 0;
	}

	@Override
	public Integer getId() {
		return null;
	}

	@Override
	public void setId(Integer id) {
	}
}
