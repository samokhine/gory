package gory.experiment;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import gory.domain.Graph;
import gory.domain.Node;
import gory.service.OutputLogger;

public abstract class BaseExperiment implements Experiment {
	private static final DecimalFormat df = new DecimalFormat("0.0000");

	protected boolean readProperty(Properties properties, String propertyName, boolean defaultValue) {
		try {
			return Boolean.valueOf(properties.getProperty(propertyName));
		} catch(Exception e) {
			return defaultValue;
		}
	}

	protected int readProperty(Properties properties, String propertyName, int defaultValue) {
		try {
			return Integer.valueOf(properties.getProperty(propertyName));
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
 	public void logMatrix(Graph graph, OutputLogger logger) {
		logger.writeLine("Adjacentcy matrix:");
 		StringBuilder sb = new StringBuilder();
		for(Node n1 : graph.getNodes()) {
			sb.setLength(0);
			for(Node n2 : graph.getNodes()) {
				sb.append(n1.isConnectedTo(n2) ? "1" : "0");
			}
			logger.writeLine(sb.toString());
		}
		logger.writeLine("");
 	}
 	
	public void logCliques(Graph graph, OutputLogger logger) {
		Set<Graph> cliques = graph.getCliques();

		logger.writeLine("Cliques:");
		logger.writeLine("");

		for(Graph clique : cliques) {
			if(logger != null) {
				logNodes(clique, logger, false);
			}
		}
		
		Map<Integer, Double> cliqueSizeDistribution = getCliqueSizeDistribution(cliques);
		logger.writeLine("Distribution of cliques:");
		for(int cliqueSize : cliqueSizeDistribution.keySet()) {
			logger.writeLine(cliqueSize+" "+df.format(cliqueSizeDistribution.get(cliqueSize)));
		}

	}

	public Map<Integer, Double> getCliqueSizeDistribution(Set<Graph> cliques) {
		Map<Integer, AtomicInteger> cliqueSizeDistribution = new TreeMap<>();
		for(Graph clique : cliques) {
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

	public void logStatsOfDegrees(Graph graph, OutputLogger logger) {
		Map<Integer, Double> nodeDegreeDistribution = graph.getNodeDegreeDistribution();

		logger.writeLine("Distribution of nodes:");
		for(int degree : nodeDegreeDistribution.keySet()) {
			logger.writeLine(degree+" "+df.format(nodeDegreeDistribution.get(degree)));
		}
		
		double s2 = 0;
		int sumOfDegrees= graph.getSumOfDegrees();
		double avg = 1.0 * sumOfDegrees / graph.getSize();
		logger.writeLine("Average: "+avg);
		for(Node node : graph.getNodes()) {
			int degree = node.getDegree();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = graph.getSize() <= 1 ? 0 : s2/(graph.getSize() - 1);
		logger.writeLine("Variance: "+s2);

		logger.writeLine("");
	}

	protected void logDiameter(Graph graph, OutputLogger logger) {
		logger.writeLine("Diameter:");
		logger.writeLine(""+graph.getDiameter());
		logger.writeLine("");
	}
	
	public void logDensityAdjacentMatrix(Graph graph, OutputLogger logger) {
		logger.writeLine("DAM:");
		logger.writeLine(""+df.format(graph.getDensityAdjacentMatrix()));
		logger.writeLine("");
	}
	
	public void logCoalitionResource(Graph graph, OutputLogger logger) {
		logger.writeLine("Coalition resource:");
		logger.writeLine(""+graph.getCoalitionResource());
		logger.writeLine("");
	}
	
	public void logNodes(Graph graph, OutputLogger logger) {
		logNodes(graph, logger, true); 
	}

	public void logNodes(Graph graph, OutputLogger logger, boolean detailed) {
		if(detailed) logger.writeLine("Nodes:");
		for(Node node : graph.getNodes()) {
			int degree = node.getDegree();
			logger.writeLine(node.toString()+(detailed ? " degree: "+degree : ""));
		}
		logger.writeLine("");
	}

	public void logClusteringCoefficient(Graph graph, OutputLogger logger) {
		logger.writeLine("Clustering coefficient:");
		logger.writeLine(""+graph.getClusteringCoefficientUsingMatrix());
		logger.writeLine("");
	}

}
