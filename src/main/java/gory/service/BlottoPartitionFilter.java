package gory.service;

import java.util.HashMap;
import java.util.Map;

import gory.domain.Partition;

public class BlottoPartitionFilter implements IPartitionFilter {
	private int maxNumRepetitions;
	private int minResource;
	private int maxResource;
	
	public BlottoPartitionFilter(int maxNumRepetitions, int minResource, int maxResource) {
		this.maxNumRepetitions = maxNumRepetitions;
		this.minResource = minResource;
		this.maxResource = maxResource;
	}

	@Override
	public boolean filter(Partition partition) {
		return checkResource(partition) && checkRepetitions(partition);
	}
	
	private boolean checkResource(Partition partition) {
		Partition normilized = partition.clone().normalize();
		
		int resource = 0;
		for(int i=1; i<=5; i++) {
			resource += normilized.getAt(i);
		}
		
		return resource >= minResource && resource <= maxResource;
	}

	private boolean checkRepetitions(Partition partition) {
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
}
