package gory.experiment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;
import gory.service.OutputLogger;
import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class BaseExperiment implements Experiment {
	protected static final DecimalFormat df = new DecimalFormat("0.0000");

	@AllArgsConstructor
	@Getter
	protected static class AverageAndStdDev {
		double average;
		double stdDev;
		
		@Override
		public String toString() {
			return df.format(average)+ "("+df.format(stdDev)+")";
		}
	}
	
	protected AverageAndStdDev getAverageAndStdDev(List<? extends Number> items) {
		double average = 0;
		double stdDev = 0;
		
		if(items.size() > 0) {
			for(Number item : items) {
				average += item.doubleValue();
			}
			average /= items.size();

			if(items.size() > 1) {
				for(Number item : items) {
					stdDev += (item.doubleValue() - average) * (item.doubleValue() - average);
				}
				
				stdDev /= (items.size() - 1);
				
				stdDev = Math.sqrt(stdDev);
			}
		}
		
		return new AverageAndStdDev(average, stdDev);
		
	}
	
	protected Map<Integer, AverageAndStdDev> merge(List<Map<Integer, ? extends Number>> items) {
		Map<Integer, List<Double>> m = new TreeMap<>();
		
		for(Map<Integer, ? extends Number> item : items) {
			for(int key : item.keySet()) {
				List<Double> l = m.get(key);
				if(l == null) {
					l = new ArrayList<>();
					m.put(key, l);
				}
				l.add(item.get(key).doubleValue());
			}
		}
		
		Map<Integer, AverageAndStdDev> r = new TreeMap<>();
		for(int key : m.keySet()) {
			r.put(key, getAverageAndStdDev(m.get(key)));
		}
		
		return r;
	}
	
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
	
	protected Set<Partition> parseListOfPartitions(String str) {
		Set<Partition> partitions = new HashSet<>();
		
		if(str == null) str = "";
		str = str.replaceAll(" ", "");
		String insertElements[] = str.split("\\],\\[");
		for(String insertElement : insertElements) {
			if(insertElement.isEmpty()) continue;
			if(insertElement.indexOf('[') != 0) insertElement = "[" + insertElement;
			if(insertElement.lastIndexOf(']') != insertElement.length() - 1) insertElement = insertElement + "]";

			partitions.add(new Partition(insertElement));
		}
		
		return partitions;
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
		logCliques(graph, graph.getCliques(), logger);
	}

	public void logCliques(Graph graph, Set<Graph> cliques, OutputLogger logger) {
		logger.writeLine("Cliques:");
		logger.writeLine("");

		for(Graph clique : cliques) {
			if(logger != null) {
				logNodes(clique, logger, false);
			}
		}
	}

	public void logNodesByCliques(Graph graph, Set<Graph> cliques, OutputLogger logger) {
		logger.writeLine("Nodes by cliques:");
		logger.writeLine("");

		for(Node node : graph.getNodes()) {
			String line = node.toString() + " " + node.getDegree();
			int sum = 0;
			for(Graph clique : cliques) {
				boolean found = false;
				for(Node cliqueNode : clique.getNodes()) {
					if(cliqueNode.equals(node)) {
						found = true;
						break;
					}
				}
				if(found) {
					line += " 1";
					sum++;
				} else {
					line += " 0";
				}
			}
			line += " "+sum;
			logger.writeLine(line);
		}
		logger.writeLine("");
	}
	
	public void logDistributionOfCliques(Graph graph, OutputLogger logger) {
		logDistributionOfCliques(graph.getCliques(), logger);
	}

	public void logDistributionOfCliques(Set<Graph> cliques, OutputLogger logger) {
		Map<Integer, AtomicInteger> cliquesCountBySize = getCliquesCountBySize(cliques);
		Map<Integer, Double> cliqueSizeDistribution = getCliqueSizeDistribution(cliques);
		logger.writeLine("Distribution of cliques:");
		for(int cliqueSize : cliqueSizeDistribution.keySet()) {
			logger.writeLine(cliqueSize+" "+cliquesCountBySize.get(cliqueSize).get()+" "+df.format(cliqueSizeDistribution.get(cliqueSize)));
		}
		logger.writeLine("");
	}
	
	public Map<Integer, Double> getCliqueSizeDistribution(Set<Graph> cliques) {
		Map<Integer, AtomicInteger> cliquesCountBySize = getCliquesCountBySize(cliques);
		
		int numCliques = 0;
		for(AtomicInteger numcliquesForSize : cliquesCountBySize.values()) {
			numCliques += numcliquesForSize.get();
		}
		
		Map<Integer, Double> cliqueSizeDistribution = new TreeMap<>();
		for(int cliqueSize : cliquesCountBySize.keySet()) {
			cliqueSizeDistribution.put(cliqueSize, 1.0*cliquesCountBySize.get(cliqueSize).intValue()/numCliques);
		}
		
		return cliqueSizeDistribution;
	}

	public Map<Integer, AtomicInteger> getCliquesCountBySize(Set<Graph> cliques) {
		Map<Integer, AtomicInteger> cliquesCountBySize = new TreeMap<>();
		for(Graph clique : cliques) {
			int size = clique.getSize();
			
			AtomicInteger cliqueSizeCnt = cliquesCountBySize.get(size);
			if(cliqueSizeCnt == null) {
				cliqueSizeCnt = new AtomicInteger();
				cliquesCountBySize.put(size, cliqueSizeCnt);
			}
			cliqueSizeCnt.incrementAndGet();
		}
		
		return cliquesCountBySize;
	}
	public void logStatsOfDegrees(Graph graph, OutputLogger logger) {
		Map<Integer, AtomicInteger> nodeDegreeCount = graph.getNodeDegreeCount();
		Map<Integer, Double> nodeDegreeDistribution = graph.getNodeDegreeDistribution(nodeDegreeCount);

		logger.writeLine("Distribution of nodes:");
		for(int degree : nodeDegreeDistribution.keySet()) {
			logger.writeLine(degree+" "+nodeDegreeCount.get(degree).get()+" "+df.format(nodeDegreeDistribution.get(degree)));
		}
		
		double s2 = 0;
		int sumOfDegrees= graph.getSumOfDegrees();
		double avg = 1.0 * sumOfDegrees / graph.getSize();
		logger.writeLine("Average: "+df.format(avg));
		for(Node node : graph.getNodes()) {
			int degree = node.getDegree();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = graph.getSize() <= 1 ? 0 : s2/(graph.getSize() - 1);
		logger.writeLine("Variance: "+df.format(s2));

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
