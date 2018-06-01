package gory.service;

import java.util.List;
import java.util.Random;

import gory.domain.Graph;
import gory.domain.Partition;
import gory.domain.PartitionNode;

public class GraphBuilder {
	static private Random rand = new Random();
	
	static public Graph build(String name, int numBuildingSteps, List<Partition> partitions) {
		Graph graph = new Graph(name);
	
		PartitionNode seedNode = createRandomNode(partitions);
		graph.addNode(seedNode);
		for(int step=1; step<=numBuildingSteps; step++) {
			PartitionNode node = createRandomNode(partitions);
			graph.addNode(node);
		}
		
		return graph;
	}
	
	static private PartitionNode createRandomNode(List<Partition> partitions) {
		int randomIndex = rand.nextInt(partitions.size());
		return new PartitionNode(partitions.get(randomIndex));
	}
}
