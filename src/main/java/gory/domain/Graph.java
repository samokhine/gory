package gory.domain;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import gory.algorithm.BronKerbosch;
import gory.service.OutputLogger;
import lombok.Getter;
import lombok.Setter;

public class Graph {
	private static final DecimalFormat df = new DecimalFormat("0.0000");
	
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
			if(distance >= 0 && distance <= connectionDistance) {
				node.connect(newNode);
			}
		}
		
		nodes.add(newNode);
		
		return true;
	}
	
	public void removeNode(Node nodeToRemove) {
		nodes.remove(nodeToRemove);
		
		for(Node node : nodes) {
			node.getConnectedNodes().remove(nodeToRemove);
		}
	}
	
	public void replaceNode(Node oldNode, Node newNode) {
		removeNode(oldNode);
		addNode(newNode);
	}
	
	public Set<Graph> findMaxCliques() {
		BronKerbosch algorithm = new BronKerbosch();
		return algorithm.findMaxCliques(this);
	}

	public double getClusteringCoefficientUsingTriangles() {
		double total = 0.0;
        for (Node v : getNodes()) {
        	total += v.getClusteringCoefficientUsingTriangles();

        }

        return total / getSize();
	}
	
 	public double getClusteringCoefficientUsingMatrix() {
        List<Node> nodes = new ArrayList<>(getNodes());
		int Ntr = 0;
		for(int i=0; i<nodes.size(); i++) {
			for(int j=i+1; j<nodes.size(); j++) {
				for(int k=j+1; k<nodes.size(); k++) {
					Ntr += nodes.get(i).isConnectedTo(nodes.get(j)) ? 
								nodes.get(i).isConnectedTo(nodes.get(k)) ? 
										nodes.get(j).isConnectedTo(nodes.get(k)) ? 1 : 0
									: 0
							: 0;
		        }
	        }
        }

		int N3 = 0;
		for(int i=0; i<nodes.size(); i++) {
			for(int j=i+1; j<nodes.size(); j++) {
				for(int k=j+1; k<nodes.size(); k++) {
					N3 += (nodes.get(i).isConnectedTo(nodes.get(j)) ? 1 : 0)*(nodes.get(i).isConnectedTo(nodes.get(k)) ? 1 : 0) +
							(nodes.get(j).isConnectedTo(nodes.get(i)) ? 1 : 0)*(nodes.get(j).isConnectedTo(nodes.get(k)) ? 1 : 0) +
							(nodes.get(k).isConnectedTo(nodes.get(i)) ? 1 : 0)*(nodes.get(k).isConnectedTo(nodes.get(j)) ? 1 : 0);
				}
	        }
        }

		
		return N3 > 0 ? 3.0 * Ntr / N3 : 0;
 	} 
	
 	public void logMatrix(OutputLogger logger) {
		logger.writeLine("Adjacentcy matrix:");
 		StringBuilder sb = new StringBuilder();
		for(Node n1 : nodes) {
			sb.setLength(0);
			for(Node n2 : nodes) {
				sb.append(n1.isConnectedTo(n2) ? "1" : "0");
			}
			logger.writeLine(sb.toString());
		}
		logger.writeLine("");
 	}
 	
	public void logStatsOfDegrees(OutputLogger logger) {
		Map<Integer, Double> nodeDegreeDistribution = getNodeDegreeDistribution();

		logger.writeLine("Distribution of nodes:");
		for(int degree : nodeDegreeDistribution.keySet()) {
			logger.writeLine(degree+" "+df.format(nodeDegreeDistribution.get(degree)));
		}
		
		double s2 = 0;
		int sumOfDegrees= getSumOfDegrees();
		double avg = 1.0 * sumOfDegrees / nodes.size();
		logger.writeLine("Average: "+avg);
		for(Node node : nodes) {
			int degree = node.getDegree();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = nodes.size() <= 1 ? 0 : s2/(nodes.size() - 1);
		logger.writeLine("Variance: "+s2);

		logger.writeLine("");
	}
	
	public Map<Integer, Double> getNodeDegreeDistribution() {
		Map<Integer, AtomicInteger> nodeDegreeDistribution = new TreeMap<>();
		for(Node node : nodes) {
			int degree = node.getDegree();
			
			AtomicInteger degreeCnt = nodeDegreeDistribution.get(degree);
			if(degreeCnt == null) {
				degreeCnt = new AtomicInteger();
				nodeDegreeDistribution.put(degree, degreeCnt);
			}
			degreeCnt.incrementAndGet();
		}
		
		Map<Integer, Double> result = new TreeMap<>();
		for(int degree : nodeDegreeDistribution.keySet()) {
			result.put(degree, 1.0*nodeDegreeDistribution.get(degree).intValue()/getSize());
		}
		
		return result;
	}
	
	public void logCliques(OutputLogger logger) {
		logger.writeLine("Cliques:");
		logger.writeLine("");

		Map<Integer, Double> cliqueSizeDistribution = getCliqueSizeDistribution(logger);
		logger.writeLine("Distribution of cliques:");
		for(int cliqueSize : cliqueSizeDistribution.keySet()) {
			logger.writeLine(cliqueSize+" "+df.format(cliqueSizeDistribution.get(cliqueSize)));
		}
	}

	public Map<Integer, Double> getCliqueSizeDistribution() {
		return getCliqueSizeDistribution(null);
	}

	private Map<Integer, Double> getCliqueSizeDistribution(OutputLogger logger) {
		Set<Graph> cliques = findMaxCliques();
		Map<Integer, AtomicInteger> cliqueSizeDistribution = new TreeMap<>();
		for(Graph clique : cliques) {
			if(logger != null) {
				clique.logNodes(logger, false);
			}

			int size = clique.getSize();
			
			AtomicInteger cliqueSizeCnt = cliqueSizeDistribution.get(size);
			if(cliqueSizeCnt == null) {
				cliqueSizeCnt = new AtomicInteger();
				cliqueSizeDistribution.put(size, cliqueSizeCnt);
			}
			cliqueSizeCnt.incrementAndGet();
		}
		
		int numCliques = 0;
		for(AtomicInteger numcliquesForSize : cliqueSizeDistribution.values()) {
			numCliques += numcliquesForSize.get();
		}
		
		Map<Integer, Double> result = new TreeMap<>();
		for(int cliqueSize : cliqueSizeDistribution.keySet()) {
			result.put(cliqueSize, 1.0*cliqueSizeDistribution.get(cliqueSize).intValue()/numCliques);
		}
		
		return result;
	}
	
	public void logCoalitionResource(OutputLogger logger) {
		logger.writeLine("Coalition resource:");
		logger.writeLine(""+getCoalitionResource());
		logger.writeLine("");
	}
	
	public int getCoalitionResource() {
		int coalitionResource = 0;
		for(Node node : nodes) {
			int sumOfSummonds = 0;
			for(int summond : node.getSummands()) {
				sumOfSummonds += summond;
			}

			int degree = node.getDegree();
			coalitionResource += degree * sumOfSummonds;
		}
		
		return coalitionResource;
	}
	
	public void logNodes(OutputLogger logger) {
		logNodes(logger, true); 
	}

	public void logNodes(OutputLogger logger, boolean detailed) {
		if(detailed) logger.writeLine("Nodes:");
		for(Node node : nodes) {
			int degree = node.getDegree();
			logger.writeLine(node.toString()+(detailed ? " degree: "+degree : ""));
		}
		logger.writeLine("");
	}

	public void logClusteringCoefficient(OutputLogger logger) {
		logger.writeLine("Clustering coefficient:");
		logger.writeLine(""+getClusteringCoefficientUsingMatrix());
		logger.writeLine("");
	}
	
	public int getSumOfDegrees() {
		int sumOfDegrees = 0;
		for(Node node : nodes) {
			int degree = node.getDegree();
			sumOfDegrees += degree;
		}
		return sumOfDegrees;
	}
}
