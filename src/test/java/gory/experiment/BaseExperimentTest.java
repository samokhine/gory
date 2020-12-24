package gory.experiment;

import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gory.domain.Partition;

public class BaseExperimentTest {
	@Test
	public void parseListOfPartitionsTest() {
		Experiment1 experiment = new Experiment1();
		
		Set<Partition> partitions = experiment.parseListOfPartitions("[1, 2,   3 ],  [3,4, 5]");
		assertEquals(2, partitions.size());
		
		double w=0.5;
		partitions = experiment.parseListOfPartitions("[1, 2,   3, w="+w+"]");
		assertEquals(1, partitions.size());
		assertNotNull(partitions.iterator().next().getW());
		assertTrue(partitions.iterator().next().getW().doubleValue() == w);
	}
}
