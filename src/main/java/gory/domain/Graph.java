package gory.domain;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
			if(distance <= connectionDistance) {
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

		int sumOfDegrees = 0;
		logger.writeLine("Nodes:");
		for(Node node : nodes) {
			int degree = node.getConnectedPartitions().size();
			sumOfDegrees += degree;
			logger.writeLine(node.toString()+(detailed ? 
					" degree: "+degree : 
					""));
		}
		logger.writeLine("");

		if(!detailed) return;

		Map<Integer, AtomicInteger> nodeDegreeDistribution = new TreeMap<>();
		for(Node node : nodes) {
			int degree = node.getConnectedPartitions().size();
			
			AtomicInteger degreeCnt = nodeDegreeDistribution.get(degree);
			if(degreeCnt == null) {
				degreeCnt = new AtomicInteger();
				nodeDegreeDistribution.put(degree, degreeCnt);
			}
			degreeCnt.incrementAndGet();
		}

		DecimalFormat df = new DecimalFormat("0.0000");
		logger.writeLine("Distribution of nodes:");
		for(int degree : nodeDegreeDistribution.keySet()) {
			logger.writeLine(degree+" "+df.format(1.0*nodeDegreeDistribution.get(degree).intValue()/getSize()));
		}
		logger.writeLine("");

		Set<Graph> cliques = findMaxCliques();
		Map<Integer, AtomicInteger> cliqueSizeDistribution = new TreeMap<>();
		for(Graph clique : cliques) {
			clique.log(logger, false);

			int size = clique.getSize();
			
			AtomicInteger cliqueSizeCnt = cliqueSizeDistribution.get(size);
			if(cliqueSizeCnt == null) {
				cliqueSizeCnt = new AtomicInteger();
				cliqueSizeDistribution.put(size, cliqueSizeCnt);
			}
			cliqueSizeCnt.incrementAndGet();
		}
		
		logger.writeLine("Distribution of cliques:");
		for(int cliqueSize : cliqueSizeDistribution.keySet()) {
			logger.writeLine(cliqueSize+" "+df.format(1.0*cliqueSizeDistribution.get(cliqueSize).intValue()/cliques.size()));
		}
		logger.writeLine("");
		
		double s2 = 0;
		double avg = 1.0 * sumOfDegrees / nodes.size();
		logger.writeLine("Average: "+avg);
		for(Node node : nodes) {
			int degree = node.getConnectedPartitions().size();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = nodes.size() <= 1 ? 0 : s2/(nodes.size() - 1);
		logger.writeLine("Variance: "+s2);

		logger.writeLine("");
		logger.writeLine("");
	}
}
