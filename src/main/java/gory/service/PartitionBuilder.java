package gory.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import gory.domain.Partition;

public class PartitionBuilder {
	static public List<Partition> build(int n, int m) {
		return build(n, m, null);
	}

	static public List<Partition> buildRandom(int n, int m, int num) {
		List<Partition> partitions = new ArrayList<>();
		int cnt1 = 0;
		while(cnt1++ < num) {
			List<Integer> tmp = new ArrayList<>();
			for(int j=1; j<=m-1; j++) {
				tmp.add(ThreadLocalRandom.current().nextInt(0, n));
			}
			
			Collections.sort(tmp);
			tmp.add(0, 0);
			tmp.add(n);
			
			List<Double> summands = new ArrayList<>();
			for(int j=1; j<=m; j++) {
				int summand = tmp.get(j) - tmp.get(j-1);
				summands.add(1.0 * summand);
			}
			Partition partition = new Partition(summands);
			partitions.add(partition);
		}
		return partitions;
	}
	
	static public List<Partition> build(int n, int m, IPartitionFilter filter) {
		List<Partition> partitions = Collections.synchronizedList(new ArrayList<>());

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		int min = (int) Math.ceil(1.0*n/m);
		for(int i=n; i>=min; i--) {
			final int iFinal = i;
			executor.submit(new Callable<Void>() {
				public Void call() {
					List<Double> summands = new ArrayList<>();
					for(int j=1; j<=m; j++) {
						summands.add(1.0 * (j == 1 ? iFinal : 0));
					}
					Partition partition = new Partition(summands);
		
					addNext(n, m, partitions, partition, iFinal, 1, filter);
					return null;
				}
			});
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return partitions;
	}
	
	static private void addNext(int n, int m, List<Partition> partitions, Partition current, int sum, int position, IPartitionFilter filter) {
		if(sum == n) {
			if(filter == null || filter.filter(current)) {
				partitions.add(current);
			}
		} else if(position<m) {
			int max = (int) Math.min(current.getAt(position), n-sum);
			for(int i=1; i<=max; i++) {
				Partition partition = current.clone();
				partition.setAt(position+1, i);
				addNext(n, m, partitions, partition, sum+i, position+1, filter);
			}
		}
	}
}
