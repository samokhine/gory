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
			}
		}
		
		nodes.add(newNode);
		
		return true;
	}
	
	public Set<Graph> findMaxCliques() {
		BronKerbosch algorithm = new BronKerbosch();
		return algorithm.findMaxCliques(this);
	}

	public double getClusteringCoefficient() {
		Set<Node> seen = new HashSet<>();
		//StringBuilder str = new StringBuilder();;
		double total = 0.0;
        for (Node v : getNodes()) {
        	System.out.println(v);
            // Cumulate local clustering coefficient of vertex v.
            int possible = 0; //v.getDegree() * (v.getDegree() - 1);
            int actual = 0;
            seen.clear();
            for (Node u : v.getConnectedNodes()) {
                seen.add(u);
            	for (Node w : v.getConnectedNodes()) {
                	if(seen.contains(w)) continue;
                	
                	//str.setLength(0);
                	//str.append(v + " " + u + " " + w);
                	possible++;
                    if(u.isConnectedTo(w)) {
                        actual++;
                        //str.append(" triangle");
                    }
                    //System.out.println(str.toString());
                }
            }
            //System.out.println("");
            if (possible > 0) {
                total += 1.0 * actual / possible;
            }
        }
        return total / getSize();
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
			int degree = node.getDegree();
			sumOfDegrees += degree;
			logger.writeLine(node.toString()+(detailed ? 
					" degree: "+degree : 
					""));
		}
		logger.writeLine("");

		if(!detailed) return;

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

		DecimalFormat df = new DecimalFormat("0.0000");
		logger.writeLine("Distribution of nodes:");
		for(int degree : nodeDegreeDistribution.keySet()) {
			logger.writeLine(degree+" "+df.format(1.0*nodeDegreeDistribution.get(degree).intValue()/getSize()));
		}
		logger.writeLine("");
		
		double s2 = 0;
		double avg = 1.0 * sumOfDegrees / nodes.size();
		logger.writeLine("Average: "+avg);
		for(Node node : nodes) {
			int degree = node.getDegree();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = nodes.size() <= 1 ? 0 : s2/(nodes.size() - 1);
		logger.writeLine("Variance: "+s2);

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
		logger.writeLine("");
	}
}
