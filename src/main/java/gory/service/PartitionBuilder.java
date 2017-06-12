package gory.service;

import java.util.ArrayList;
import java.util.List;

import gory.domain.Partition;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PartitionBuilder {
	private int n, m;
	
	public List<Partition> build() {
		List<Partition> partitions = new ArrayList<>();

		int min = (int) Math.ceil(1.0*n/m);
		for(int i=n; i>=min; i--) {
			List<Integer> summands = new ArrayList<>();
			for(int j=1; j<=m; j++) {
				summands.add(j == 1 ? i : 0);
			}
			Partition partition = new Partition(summands);

			addNext(n, m, partitions, partition, i, 1);
		}
		
		return partitions;
	}
	
	private void addNext(int n, int m, List<Partition> partitions, Partition current, int sum, int position) {
		if(sum == n) {
			partitions.add(current);
		} else if(position<m) {
			int max = Math.min(current.getAt(position), n-sum);
			for(int i=1; i<=max; i++) {
				Partition partition = current.clone();
				partition.setAt(position+1, i);
				addNext(n, m, partitions, partition, sum+i, position+1);
			}
		}
	}
}
