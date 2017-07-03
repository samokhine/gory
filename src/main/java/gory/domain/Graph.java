package gory.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import gory.algorithm.BronKerbosch;
import gory.service.OutputLogger;
import lombok.Getter;
import lombok.Setter;

public class Graph {
	@Getter @Setter
	private String name;

	@Getter
	private int connectionDistance = 1;
	
	@Getter
	private Set<Node> nodes = new HashSet<>();

	public Graph(String name) {
		this.name = name;
	}

	public Graph(String name, int connectionDistance) {
		this.name = name;
		this.connectionDistance = connectionDistance;
	}
	
	@Override
	public String toString() {
		return nodes.toString();
	}
	
	public int getSize() {
		return nodes.size();
	}
	
	public boolean addNode(Node newNode) {
		if(nodes.contains(newNode)) return false;
		
		for(Node node : nodes) {
			int distance = node.distanceTo(newNode);
			if(distance == connectionDistance) {
				node.connect(newNode);
				newNode.connect(node);
			}
		}
		
		nodes.add(newNode);
		
		return true;
	}
	
	public Set<Graph> findMaxCliques() {
		BronKerbosch algorithm = new BronKerbosch();
		return algorithm.findMaxCliques(this);
	}

	public void log(OutputLogger logger) {
		log(logger, true);
	}

	private void log(OutputLogger logger, boolean detailed) {
		logger.writeLine(name);
		logger.writeLine("");

		if(nodes.isEmpty()) return;

		Map<Integer, AtomicInteger> nodeDegreeDistribution = new HashMap<>();
		for(Node node : nodes) {
			int degree = node.getConnectedNodes().size();
			
			AtomicInteger degreeCnt = nodeDegreeDistribution.get(degree);
			if(degreeCnt == null) {
				degreeCnt = new AtomicInteger();
				nodeDegreeDistribution.put(degree, degreeCnt);
			}
			degreeCnt.incrementAndGet();
		}
		
		int sumOfDegrees = 0;
		logger.writeLine("Nodes:");
		for(Node node : nodes) {
			int degree = node.getConnectedNodes().size();
			sumOfDegrees += degree;
			logger.writeLine(node.toString()+(detailed ? 
					" degree: "+degree+", distribution: "+(1.0*nodeDegreeDistribution.get(degree).get()/getSize()) : 
					""));
		}
		logger.writeLine("");

		if(!detailed) return;
		
		Set<Graph> cliques = findMaxCliques();
		Map<Integer, AtomicInteger> cliqueSizeDistribution = new HashMap<>();
		for(Graph clique : cliques) {
			int size = clique.getSize();
			
			AtomicInteger cliqueSizeCnt = cliqueSizeDistribution.get(size);
			if(cliqueSizeCnt == null) {
				cliqueSizeCnt = new AtomicInteger();
				cliqueSizeDistribution.put(size, cliqueSizeCnt);
			}
			cliqueSizeCnt.incrementAndGet();
		}
		
		for(Graph clique : cliques) {
			clique.setName(clique.getName()+" distribution: "+1.0*cliqueSizeDistribution.get(clique.getSize()).get()/cliques.size());
			clique.log(logger, false);
		}
		
		double s2 = 0;
		double avg = 1.0 * sumOfDegrees / nodes.size();
		logger.writeLine("Average: "+avg);
		for(Node node : nodes) {
			int degree = node.getConnectedNodes().size();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = nodes.size() <= 1 ? 0 : s2/(nodes.size() - 1);
		logger.writeLine("Variance: "+s2);

		logger.writeLine("");
		logger.writeLine("");
	}
}
