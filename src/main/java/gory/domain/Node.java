package gory.domain;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of={"partition"})
public class Node {
	@Getter
	private Partition partition;

	@Getter
	private Set<Partition> connectedPartitions = new HashSet<>();
	
	public Node(Partition partition) {
		this.partition = partition;
	}
	
	@Override
	public String toString() {
		return partition.toString();
	}
	
	public void connect(Node node) {
		connectedPartitions.add(node.getPartition());
	}
	
	public int distanceTo(Node node) {
		return node.getPartition().distanceTo(partition);
	}
}
