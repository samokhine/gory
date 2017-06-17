package gory.service;

import java.util.List;
import java.util.Random;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;

public class GraphBuilder {
	static private Random rand = new Random();
	
	static public Graph build(int numBuildingSteps, List<Partition> partitions) {
		Graph graph = new Graph();
	
		Node seedNode = createRandomNode(partitions);
		graph.addNode(seedNode);
		for(int step=1; step<=numBuildingSteps; step++) {
			Node node = createRandomNode(partitions);
			graph.addNode(node);
		}
		
		return graph;
	}
	
	static private Node createRandomNode(List<Partition> partitions) {
		int randomIndex = rand.nextInt(partitions.size());
		return new Node(partitions.get(randomIndex));
	}
}
