package gory.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of={"arr"})
public class Partition {
	@Getter
	private int sumOfDigits;

	private int[] arr;
	
	@Override
	public String toString() {
		return getSummands().toString();
	}

	public Partition(List<Integer> summands) {
		summands.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return -1*o1.compareTo(o2);
			}
		});
		
		sumOfDigits = 0;
		for(int summand : summands) {
			sumOfDigits += summand;
		}
		
		this.arr = new int[summands.size()];
		for(int i=0; i<summands.size(); i++) {
			this.arr[i] = summands.get(i);
		}
	}
	
	public int getNumberOfDigits() {
		return arr.length;
	}
	
	// position is 1-based
	public int getAt(int position) {
		return arr[position-1];
	}

	// position is 1-based
	public int setAt(int position, int value) {
		sumOfDigits = sumOfDigits - getAt(position) + value; 
		return arr[position-1] = value;
	}

	public List<Integer> getSummands() {
		List<Integer> summonds = new ArrayList<>(this.arr.length);
		
		for(int i : arr) {
			summonds.add(i);
		}
		
		return summonds;
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
		return new Partition(getSummands());
	}
}
