package gory.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of={"arr"})
public class Partition {
	// descending
	private static Comparator<Integer> SUMMONDS_COMPARATOR = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			return -1*o1.compareTo(o2);
		}
	};
	
	@Getter
	private int sumOfDigits;

	private int[] arr;
	
	@Override
	public String toString() {
		return getSummands().toString();
	}

	public Partition(String summonds) {
		summonds = summonds.substring(1, summonds.length()-1);
		arr = new int[summonds.split(",").length];
		int i = 0;
		for(String summond : summonds.split(",")) {
			arr[i++] = Integer.valueOf(summond.trim());
		}
	}

	public Partition(List<Integer> summands) {
		this(summands, true);
	}

	public Partition(List<Integer> summands, boolean sort) {
		if(sort) {
			summands.sort(SUMMONDS_COMPARATOR);
		}
		
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
		if(getNumberOfDigits() != partition.getNumberOfDigits()) return -1;
		
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
	
	public int getEvenness() {
		int evenness = 0;
		for(int summand :  getSummands()) {
			if(summand%2 == 0) {
				evenness++;
			}
		}
		return evenness;
	}

	public int getOddness() {
		int oddness = 0;
		for(int summand :  getSummands()) {
			if(summand%2 == 1) {
				oddness++;
			}
		}
		return oddness;
	}
	
	// https://en.wikipedia.org/wiki/Durfee_square
	public int getRank() {
		List<Integer> summands = getSummands();
		summands.sort(SUMMONDS_COMPARATOR); // just on case
		
		int rank;
		for(rank=0; rank<getNumberOfDigits(); rank++) {
			if(rank>summands.get(rank)) {
				return rank;
			}
		}
		
		return rank;
	}
	
	public Partition normalize() {
		arr = Arrays.stream(arr)
		        .boxed()
		        .sorted(Comparator.reverseOrder())
		        .mapToInt(Integer::intValue)
		        .toArray();
		
		return this;
	}
}
