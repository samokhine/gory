package gory.domain;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper=false,of={"partition"})
public class PartitionNode extends Node {
	@Getter
	private Partition partition;

	@Getter @Setter
	private Integer id;

	@Override
	public String toString() {
		return partition.toString();
	}
	
	public PartitionNode(Partition partition) {
		this(partition, true);
	}

	public PartitionNode(Partition partition, boolean sort) {
		this.partition = new Partition(partition.getSummands(), sort);
	}
	
	@Override
	public INode cloneIt() {
		return new PartitionNode(getPartition());
	}
	
	@Override
	public double distanceTo(INode node) {
		if(node instanceof PartitionNode) {
			return partition.distanceTo(((PartitionNode) node).getPartition());
		} else {
			return -1;
		}
	}

	public int getNumberOfDigits() {
		return (int) partition.getNumberOfDigits();
	}

	public List<Double> getSummands() {
		return partition.getSummands();
	}
}
