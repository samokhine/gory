package gory.domain;

import java.util.ArrayList;
import java.util.List;

public class Partition {
	private List<Integer> summands = new ArrayList<>();
	
	@Override
	public String toString() {
		return summands.toString();
	}
	
	public Partition(List<Integer> summands) {
		this.summands.addAll(summands);
	}
	
	// position is 1-based
	public int getAt(int position) {
		return summands.get(position-1);
	}

	// position is 1-based
	public int setAt(int position, int value) {
		return summands.set(position-1, value);
	}

	public List<Integer> getSummands() {
		return new ArrayList<>(summands);
	}
	
	public Partition clone() {
		return new Partition(summands);
	}
}
