package gory.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PartitionTest {
	@Test
	public void testRank() {
		Partition partition = new Partition("[15, 15, 15, 13, 12, 11, 8, 5, 4, 4]");
		assertEquals(7, partition.getRank());
		
		partition = new Partition("[10, 10, 10, 10, 10, 10, 10, 10, 10, 10]");
		assertEquals(10, partition.getRank());

		partition = new Partition("[100, 0, 0, 0, 0, 0, 0, 0, 0, 0]");
		assertEquals(1, partition.getRank());
	}
}
