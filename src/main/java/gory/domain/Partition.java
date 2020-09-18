package gory.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of={"arr"})
public class Partition {
	// descending
	private static Comparator<Double> SUMMONDS_COMPARATOR = new Comparator<Double>() {
		@Override
		public int compare(Double o1, Double o2) {
			return -1*o1.compareTo(o2);
		}
	};
	
	@Getter
	private double sumOfDigits;

	private double[] arr;
	
	@Override
	public String toString() {
		return getSummands().toString();
	}

	public Partition(String summonds) {
		this(summonds, true);
	}

	public Partition(String summonds, boolean sort) {
		summonds = summonds.substring(1, summonds.length()-1);
		arr = new double[summonds.split(",").length];
		int i = 0;
		for(String summond : summonds.split(",")) {
			arr[i++] = Double.valueOf(summond.trim());
		}
		
		if(sort) {
			Arrays.sort(arr);
			ArrayUtils.reverse(arr);
		}
		sumOfDigits = Arrays.stream(arr).sum();
	}

	public Partition(List<Double> summands) {
		this(summands, true);
		sumOfDigits = Arrays.stream(arr).sum();;
	}

	public Partition(List<Double> summands, boolean sort) {
		if(sort) {
			summands.sort(SUMMONDS_COMPARATOR);
		}
		
		sumOfDigits = 0;
		for(double summand : summands) {
			sumOfDigits += summand;
		}
		
		this.arr = new double[summands.size()];
		for(int i=0; i<summands.size(); i++) {
			this.arr[i] = summands.get(i);
		}
	}
	
	public double getNumberOfDigits() {
		return arr.length;
	}
	
	// position is 1-based
	public double getAt(int position) {
		return arr[position-1];
	}

	// position is 1-based
	public double setAt(int position, int value) {
		sumOfDigits = sumOfDigits - getAt(position) + value; 
		return arr[position-1] = value;
	}

	public List<Double> getSummands() {
		List<Double> summonds = new ArrayList<>(this.arr.length);
		
		for(double i : arr) {
			summonds.add(i);
		}
		
		return summonds;
	}
	
	public boolean isSameType(Partition partition) {
		return getNumberOfDigits() == partition.getNumberOfDigits() && getSumOfDigits() == partition.getSumOfDigits();
	}
	
	public double distanceTo(Partition partition) {
		if(getNumberOfDigits() != partition.getNumberOfDigits()) return -1;
		
		double distance = 0;
		for(int i=1; i<=getNumberOfDigits(); i++) {
			double d = Math.abs(getAt(i) - partition.getAt(i));
			if(d > distance) distance = d;
		}
		
		return distance;
	}
	
	public Partition clone() {
		return new Partition(getSummands());
	}
	
	public int getEvenness() {
		int evenness = 0;
		for(double summand : getSummands()) {
			if(summand%2 == 0) {
				evenness++;
			}
		}
		return evenness;
	}

	public int getOddness() {
		int oddness = 0;
		for(double summand : getSummands()) {
			if(summand%2 == 1) {
				oddness++;
			}
		}
		return oddness;
	}
	
	// https://en.wikipedia.org/wiki/Durfee_square
	public int getRank() {
		List<Double> summands = getSummands();
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
		        .mapToDouble(Double::doubleValue)
		        .toArray();
		
		return this;
	}
	
	public void applyNormalDistribution(double standardDeviation, int geneAccuracy) {
		if(standardDeviation <= 0) return;
		
		double accuracy = Math.pow(10, geneAccuracy);
		Random random = new Random();
		for(int i=0; i<arr.length; i++) {
			arr[i] = Math.round(accuracy * (arr[i] +  random.nextGaussian() * standardDeviation))/accuracy;
		}
	}
}
