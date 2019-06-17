package gory.service;

import gory.domain.Partition;

public interface IPartitionFilter {
	boolean filter(Partition partition);
}
