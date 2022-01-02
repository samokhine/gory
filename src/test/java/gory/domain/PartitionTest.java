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

	@Test
	public void testSort() {
		Partition partition = new Partition("[1, 5, 3]");
		assertEquals("[5.0, 3.0, 1.0]", partition.toString());

		partition = new Partition("[1, 5, 3]", null, false);
		assertEquals("[1.0, 5.0, 3.0]", partition.toString());
	}
}
