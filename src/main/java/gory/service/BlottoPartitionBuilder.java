package gory.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import gory.domain.Partition;

public class BlottoPartitionBuilder {
	private static Random random = new Random();
	private static int sum = 100;
	private static int numParts = 10;
	
	private static List<List<Integer>> weightedDigits = new ArrayList<>();
	private static int sumOfWeights = 0;
	static {
		int maxWeight = sum - numParts;
		for(int i=1; i<=sum; i++) {
			int weight;
			if(i<=numParts) {
				weight = maxWeight/numParts * i;
			} else {
				weight = maxWeight - (i - numParts) + 1;
			}
			sumOfWeights += weight;
			
			List<Integer> weightedDigit = new ArrayList<>();
			weightedDigit.add(i);
			weightedDigit.add(weight);
			weightedDigits.add(weightedDigit);
		}
	}
	
	static public Set<Partition> build(int numPartitions, long maxAttempts, int maxNumRepetitions, int minResource, int maxResource, AtomicLong attemptsMade) {
		Set<Partition> partitions = new LinkedHashSet<>();
		
		long attempts = 0;
		while(partitions.size() < numPartitions && attempts < maxAttempts) {
			attempts++;
			Partition partition = generateRandom();
			
			if(!checkRepetitions(partition, maxNumRepetitions)) continue;

			if(!checkResource(partition, minResource, maxResource)) continue;

			partitions.add(partition);
		}
		
		attemptsMade.set(attempts);
		
		return partitions;
	}

	static private boolean checkResource(Partition partition, int minResource, int maxResource) {
		Partition normilized = partition.clone().normalize();
		
		int resource = 0;
		for(int i=1; i<=5; i++) {
			resource += normilized.getAt(i);
		}
		
		return resource >= minResource && resource <= maxResource;
	}

	static private boolean checkRepetitions(Partition partition, int maxNumRepetitions) {
		Map<Integer, Integer> repetitionCounts = new HashMap<>();
		
		for(int summand : partition.getSummands()) {
			Integer count = repetitionCounts.get(summand);
			if(count == null) count = 0;
			count++;
			if(count > maxNumRepetitions) {
				return false;
			}
			
			repetitionCounts.put(summand, count);
		}
		
		return true;
	}
	
	private static Partition generateRandom() {
		List<Integer> summands = new ArrayList<>();
		List<Integer> indexes = new ArrayList<>();
		for(int i=0; i<numParts; i++) {
			indexes.add(i);
			summands.add(0);
		}
		
		int cumSum = 0;
		while(!indexes.isEmpty()) {
			int index = random.nextInt(indexes.size());
			indexes.remove(index);
			
			int summond = getRandomDigit();
			cumSum += summond;
			if(cumSum < sum) {
				summands.set(index, summond);
			} else {
				summands.set(index, summond - (cumSum - sum));
				break;
			}
		}
		
		return new Partition(summands, false);
	}
	
	private static int getRandomDigit() {
		int thresholdWeight = random.nextInt(sumOfWeights);
		int cumWeight = 0;
		for(List<Integer> weightedDigit : weightedDigits) {
			cumWeight += weightedDigit.get(1);
			if(cumWeight>=thresholdWeight) {
				return weightedDigit.get(0);
			}
		}
		
		return 0;
	}
}
