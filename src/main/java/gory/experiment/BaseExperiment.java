package gory.experiment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.graphstream.graph.implementations.SingleGraph;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.Partition;
import gory.service.OutputLogger;
import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class BaseExperiment implements Experiment {
	protected static final DecimalFormat df2 = new DecimalFormat("0.00");
	protected static final DecimalFormat df4 = new DecimalFormat("0.0000");

	@AllArgsConstructor
	@Getter
	protected static class AverageAndStdDev {
		double average;
		double stdDev;
		
		@Override
		public String toString() {
			return df4.format(average)+ "("+df4.format(stdDev)+")";
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

	protected Partition parsePartition(String str) {
		if(str == null) str = "";
		str = str.replaceAll(" ", "");
		if(str.isEmpty()) return null;

		if(str.indexOf('[') != 0) str = "[" + str;
		if(str.lastIndexOf(']') != str.length() - 1) str = str + "]";

		return new Partition(str);
	}

	protected void logMatrix(Graph graph, OutputLogger logger) {
		logger.writeLine("Adjacentcy matrix:");
 		StringBuilder sb = new StringBuilder();
		for(INode n1 : graph.getNodes()) {
			sb.setLength(0);
			for(INode n2 : graph.getNodes()) {
				sb.append(n1.isConnectedTo(n2) ? "1" : "0");
			}
			logger.writeLine(sb.toString());
		}
		logger.writeLine("");
 	}

	protected void logCliques(Graph graph, OutputLogger logger) {
		logCliques(graph, graph.getCliques(), logger);
	}

	protected void logCliques(Graph graph, Set<Graph> cliques, OutputLogger logger) {
		logger.writeLine("Cliques:");
		logger.writeLine("");

		for(Graph clique : cliques) {
			logger.writeLine(clique.getName());
			if(logger != null) {
				logNodes(clique, logger, false);
			}
		}
	}

	protected void logNodesByCliques(Graph graph, Set<Graph> cliques, OutputLogger logger) {
		logger.writeLine("Nodes by cliques:");
		logger.writeLine("");

		for(INode node : graph.getNodes()) {
			String line = node.toString() + " " + node.getDegree();
			int sum = 0;
			for(Graph clique : cliques) {
				boolean found = false;
				for(INode cliqueNode : clique.getNodes()) {
					if(cliqueNode.equals(node)) {
						found = true;
						break;
					}
				}
				if(found) {
					line += " |"+clique.getName()+"|";
					sum++;
				}
			}
			if(sum>0) {
				line += " "+sum;
				line += " "+df4.format(1.0*sum/cliques.size());
			}
			logger.writeLine(line);
		}
		logger.writeLine("");
	}
	
	protected void logCliquesMatrices(Set<Graph> cliques, OutputLogger logger) {
		Map<Integer, Set<Graph>> cliquesBySize = new TreeMap<>(); // sorted by key which is size
		
		int maxNumCliquesOfSize = 0, maxSize = 0;
		for(Graph clique : cliques) {
			int size = clique.getSize();
			if(size > maxSize) {
				maxSize = size;
			}
			Set<Graph> cliquesOfSize = cliquesBySize.get(size);
			if(cliquesOfSize == null) {
				cliquesOfSize = new HashSet<>();
				cliquesBySize.put(size, cliquesOfSize);
			}
			cliquesOfSize.add(clique);
			if(cliquesOfSize.size()>maxNumCliquesOfSize) {
				maxNumCliquesOfSize = cliquesOfSize.size();
			}
		}
		
		List<Integer> sizes = new ArrayList<>();
		for(int size : cliquesBySize.keySet()) {
			sizes.add(size);
		}		
		
		int columnWidth = 3+Integer.toString(maxSize).length()+Integer.toString(maxNumCliquesOfSize).length();
		for(int i=0; i<sizes.size(); i++) {
			int size = sizes.get(i);
			Set<Graph> cliquesOfSize = cliquesBySize.get(size);

			logCliquesMatricesHelper(cliquesOfSize, cliquesOfSize, columnWidth, logger);
			
			if(i<sizes.size()-1) {
				Set<Graph> cliquesOfNextSize = cliquesBySize.get(sizes.get(i+1));

				logCliquesMatricesHelper(cliquesOfNextSize, cliquesOfSize, columnWidth, logger);
			}
		}
	}
	
	protected Graph buildGraphOfCliques(Set<Graph> cliques, String name) {
		int connectionDistance = 0;

		for(Graph clique : cliques) {
			int size= clique.getSize();
			if(size > connectionDistance) {
				connectionDistance = size;
			}
		}
		
		Graph graph = new Graph(name, connectionDistance);
		
		for(Graph clique : cliques) {
			graph.addNode(clique);
		}
		
		return graph;
	}
	
	private void logCliquesMatricesHelper(Set<Graph> cliques1, Set<Graph> cliques2, int columnWidth, OutputLogger logger) {
		String line = StringUtils.leftPad("", columnWidth, " ");
		for(Graph clique2 : cliques2) {
			line += StringUtils.leftPad(clique2.getName(), columnWidth, " ");
		}
		logger.writeLine(line);

		for(Graph clique1 : cliques1) {
			line = StringUtils.leftPad(clique1.getName(), columnWidth, " ");
			Map<String, AtomicInteger> distanceCounts = new HashMap<>();
			for(Graph clique2 : cliques2) {
				String distance = ""+clique1.distanceTo(clique2);
				line += StringUtils.leftPad(distance, columnWidth, " ");
				
				AtomicInteger distanceCount = distanceCounts.get(distance);
				if(distanceCount == null) {
					distanceCount = new AtomicInteger();
					distanceCounts.put(distance, distanceCount);
				}
				distanceCount.incrementAndGet();
			}

			for(String distance : distanceCounts.keySet()) {
				int count = distanceCounts.get(distance).get();
				line += "  "+distance+"("+count+") - "+df2.format(1.0*count/cliques2.size());
			}
				
			logger.writeLine(line);
		}
		logger.writeLine("");
	}

	/*
	public void displayGraphOfCliques(Set<Graph> cliques) {
		org.graphstream.graph.Graph gsGraph = new SingleGraph("");

		// add nodes
		for(Graph clique : cliques) {
			String nodeId = clique.getName();
			org.graphstream.graph.Node gsNode = gsGraph.addNode(nodeId);
			gsNode.addAttribute("ui.label", nodeId);
		}

		// add edges
		for(Graph clique1 : cliques) {
			for(Graph clique2 : cliques) {
				int distance = clique1.distanceTo(clique2);
				if(distance <= 0) continue;

				if(gsGraph.getEdge(clique2.getName()+" - "+clique1.getName()) != null) {
					continue;
				}
				
				gsGraph.addEdge(clique1.getName()+" - "+clique2.getName(), clique1.getName(), clique2.getName());
			}
		}
		
		gsGraph.addAttribute("ui.stylesheet", "node {" +
	    	    "	fill-color: green;" +
	            "	text-color: red;" +
	            "	text-size: 15;" +
	            "}");
		gsGraph.addAttribute("ui.quality");
		gsGraph.addAttribute("ui.antialias");
		
		gsGraph.display();
		
		try {
			Thread.sleep(1_000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		gsGraph.addAttribute("ui.screenshot", "graphOfCliques.png");
	}
	*/
	
	public void displayGraph(Graph graph, String fileName) {
		org.graphstream.graph.Graph gsGraph = new SingleGraph("");

		// add nodes
		for(INode node : graph.getNodes()) {
			String nodeId = node.toString();
			org.graphstream.graph.Node gsNode = gsGraph.addNode(nodeId);
			gsNode.addAttribute("ui.label", nodeId);
		}

		// add edges
		for(INode node : graph.getNodes()) {
			for(INode connectedNode : node.getConnectedNodes()) {
				if(gsGraph.getEdge(connectedNode.toString()+" - "+node.toString()) != null) {
					continue;
				}
				
				gsGraph.addEdge(node.toString()+" - "+connectedNode.toString(), node.toString(), connectedNode.toString());
			}
		}
		
		gsGraph.addAttribute("ui.stylesheet", "node {" +
	    	    "	fill-color: green;" +
	            "	text-color: red;" +
	            "	text-size: 15;" +
	            "}");
		gsGraph.addAttribute("ui.quality");
		gsGraph.addAttribute("ui.antialias");
		
		gsGraph.display();
		
		gsGraph.addAttribute("ui.screenshot", fileName+".png");
	}

	
	public void logDistributionOfCliques(Graph graph, OutputLogger logger) {
		logDistributionOfCliques(graph.getCliques(), logger);
	}

	public void logDistributionOfCliques(Set<Graph> cliques, OutputLogger logger) {
		Map<Integer, AtomicInteger> cliquesCountBySize = getCliquesCountBySize(cliques);
		Map<Integer, Double> cliqueSizeDistribution = getCliqueSizeDistribution(cliques);
		logger.writeLine("Distribution of cliques:");
		for(int cliqueSize : cliqueSizeDistribution.keySet()) {
			logger.writeLine(cliqueSize+" "+cliquesCountBySize.get(cliqueSize).get()+" "+df4.format(cliqueSizeDistribution.get(cliqueSize)));
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
			logger.writeLine(degree+" "+nodeDegreeCount.get(degree).get()+" "+df4.format(nodeDegreeDistribution.get(degree)));
		}
		
		double s2 = 0;
		int sumOfDegrees= graph.getSumOfDegrees();
		double avg = 1.0 * sumOfDegrees / graph.getSize();
		logger.writeLine("Average: "+df4.format(avg));
		for(INode node : graph.getNodes()) {
			int degree = node.getDegree();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = graph.getSize() <= 1 ? 0 : s2/(graph.getSize() - 1);
		logger.writeLine("Variance: "+df4.format(s2));

		logger.writeLine("");
	}

	protected void logDiameter(Graph graph, OutputLogger logger) {
		logger.writeLine("Diameter:");
		logger.writeLine(""+graph.getDiameter());
		logger.writeLine("");
	}
	
	public void logDensityAdjacentMatrix(Graph graph, OutputLogger logger) {
		logger.writeLine("DAM:");
		logger.writeLine(""+df4.format(graph.getDensityAdjacentMatrix()));
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
		for(INode node : graph.getNodes()) {
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
