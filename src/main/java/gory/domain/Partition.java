package gory.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

public class Partition {
	@Getter
	private int sumOfDigits;

	private List<Integer> summands = new ArrayList<>();
	
	@Override
	public String toString() {
		return summands.toString();
	}
	
	public Partition(List<Integer> summands) {
		sumOfDigits = 0;
		for(int summand : summands) {
			sumOfDigits += summand;
		}
		
		this.summands.addAll(summands);
		
		this.summands.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return -1*o1.compareTo(o2);
			}
		});
	}
	
	public int getNumberOfDigits() {
		return summands.size();
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
	
	public boolean isSameType(Partition partition) {
		return getNumberOfDigits() == partition.getNumberOfDigits() && getSumOfDigits() == partition.getSumOfDigits();
	}
	
	public int distanceTo(Partition partition) {
		if(!isSameType(partition)) return -1;
		
		int distance = 0;
		for(int i=1; i<=getNumberOfDigits(); i++) {
			int d = Math.abs(getAt(i) - partition.getAt(i));
			if(d > distance) distance = d;
		}
		
		return distance;
	}
	
	public Partition clone() {
		return new Partition(summands);
	}
}
