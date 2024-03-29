package gory.experiment;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.graphstream.stream.file.FileSinkDOT;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;
import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class BaseExperiment implements Experiment {
	protected static final DecimalFormat df1 = new DecimalFormat("0.0");
	protected static final DecimalFormat df2 = new DecimalFormat("0.00");
	protected static final DecimalFormat df4 = new DecimalFormat("0.0000");

	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

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
	
	protected String readProperty(Properties properties, String propertyName, String defaultValue) {
		try {
			return properties.getProperty(propertyName);
		} catch(Exception e) {
			return defaultValue;
		}
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

	protected long readProperty(Properties properties, String propertyName, long defaultValue) {
		try {
			return Long.valueOf(properties.getProperty(propertyName));
		} catch(Exception e) {
			return defaultValue;
		}
	}

	protected double readProperty(Properties properties, String propertyName, double defaultValue) {
		try {
			return Double.valueOf(properties.getProperty(propertyName));
		} catch(Exception e) {
			return defaultValue;
		}
	}

	protected List<Double> readArrayOfDoublesProperty(OutputLogger logger, Properties properties, String propertyName, String separator, List<Double> defaultValue) {
		List<Double> values = new ArrayList<>();
		try {
			String doubleSeparator = separator + separator;
			String str = readProperty(properties, propertyName, "").trim();
			while(str.indexOf(doubleSeparator)>=0) {
				StringUtils.replace(str, doubleSeparator, separator);
			}
			
			for(String value : str.split(" ")) {
				try {
					value = StringUtils.replace(value, ",", ".");
					values.add(NUMBER_FORMAT.parse(value).doubleValue());
				} catch(Exception e) {
					logger.writeLine("Cannot convert "+value+" to double for "+propertyName);
				}
			}
		} catch(Exception e) {
			return defaultValue;
		}
		
		return values;
	}

	protected Set<Integer> parseListOfIntegers(String str) {
		Set<Integer> integers = new HashSet<>();
		
		if(str != null) {
			for(String part : str.split(",")) {
				try {
					integers.add(Integer.valueOf(part.trim()));
				} catch(Exception e) {
					// do nothing
				}
			}
		}
		
		return integers;
	}

	
	protected Set<Partition> parseListOfPartitions(String str) {
		return parseListOfPartitions(str, true);
	}

	protected Set<Partition> parseListOfPartitions(String str, boolean sort) {
		Set<Partition> partitions = new HashSet<>();
		
		final String wMarker = ",w=";
		
		if(str == null) str = "";
		str = str.replaceAll(" ", "");
		String insertElements[] = str.toLowerCase().split("\\],\\[");
		for(String insertElement : insertElements) {
			if(insertElement.isEmpty()) continue;
			
			Double w = null;
			int i = insertElement.indexOf(wMarker);
			if(i > 0) {
				String wElement = insertElement.substring(i);
				wElement = StringUtils.replace(wElement, wMarker, "");
				wElement = StringUtils.replace(wElement, "]", "");
				try {
					w = Double.valueOf(wElement);
				} catch(Exception e) {
					// eat it up
				}

				insertElement = insertElement.substring(0, i);
			}
			
			if(insertElement.indexOf('[') != 0) insertElement = "[" + insertElement;
			if(insertElement.lastIndexOf(']') != insertElement.length() - 1) insertElement = insertElement + "]";

			partitions.add(new Partition(insertElement, w, sort));
			//System.out.println(insertElement+" "+partitions.size());
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

			for(int j=i; j<sizes.size(); j++) {
				int nextSize = sizes.get(j);
				Set<Graph> cliquesOfNextSize = cliquesBySize.get(nextSize);

				logCliquesMatricesHelper(cliquesOfNextSize, cliquesOfSize, columnWidth, logger);
			}
		}
	}

	protected Graph buildGraphOfCliques(Set<Graph> cliques, String name) {
		return buildGraphOfCliques(cliques, name, 1, false);
	}

	
	protected Graph buildGraphOfCliques(Set<Graph> cliques, String name, int connectionDistance, boolean onlyConnected) {
		Graph graph = new Graph(name, connectionDistance);
		
		for(Graph clique : cliques) {
			graph.addNode(clique);
		}

		if(onlyConnected) {
			Set<INode> disconnectedNodes = graph.getNodes().stream().filter(node -> node.getDegree() == 0).collect(Collectors.toSet());
			disconnectedNodes.forEach(node -> graph.removeNode(node));
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

	protected void logNotFullyConnectedMatrix(Graph graph, OutputLogger logger) {
		int columnWidth = 0;
		Set<INode> notFullyConnectedNodes = new HashSet<>();
		for(INode node : graph.getNodes()) {
			if(node.getConnectedNodes().size()<graph.getSize()-1) {
				notFullyConnectedNodes.add(node);
				if(node.toString().length()>columnWidth) {
					columnWidth = node.toString().length();
				}
			}
		}
		if(notFullyConnectedNodes.isEmpty()) return;
		
		columnWidth += 3;
		
		logger.writeLine("Not fully connected matrix:");
		String line = StringUtils.leftPad("", columnWidth, " ");
		for(INode node : notFullyConnectedNodes) {
			line += StringUtils.leftPad(node.toString(), columnWidth, " ");
		}
		logger.writeLine(line);

		for(INode node1 : notFullyConnectedNodes) {
			line = StringUtils.leftPad(node1.toString(), columnWidth, " ");
			for(INode node2 : notFullyConnectedNodes) {
				line += StringUtils.leftPad(node1.isConnectedTo(node2) ? "1" : "0", columnWidth, " ");
			}
			logger.writeLine(line);
		}
		logger.writeLine("");
	}
	
	public void displayGraph(Graph graph) {
		displayGraph(graph, graph.getName());
	}
	
	public void displayGraph(Graph graph, String fileName) {
		org.graphstream.graph.Graph gsGraph = graph.asGsGraph();
		
		gsGraph.display();

		try {
			Thread.sleep(1_000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		gsGraph.addAttribute("ui.screenshot", fileName+".png");
	}

	public void saveInDotFormat(Graph graph, String fileName) {
		org.graphstream.graph.Graph gsGraph = graph.asGsGraph();

		FileSinkDOT fs = new FileSinkDOT();
		try {
			fs.writeAll(gsGraph, fileName+".dot");
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		logger.writeLine("Distribution of nodes for "+graph.getName());
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
		logger.writeLine("Diameter of "+graph.getName());
		logger.writeLine(""+graph.getDiameter());
		logger.writeLine("");
	}
	
	public void logDensityAdjacentMatrix(Graph graph, OutputLogger logger) {
		logger.writeLine("DAM for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getDensityAdjacentMatrix()));
		logger.writeLine("");
	}

	public void logDensityAdjacentMatrixOnlyConnected(Graph graph, OutputLogger logger) {
		logger.writeLine("DAM (only connected) for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getDensityAdjacentMatrix(true)));
		logger.writeLine("");
	}

	public void logCoalitionResource(Graph graph, OutputLogger logger) {
		logger.writeLine("Coalition resource for "+graph.getName());
		logger.writeLine(""+graph.getCoalitionResource());
		logger.writeLine("");
	}
	
	public void logNodes(Graph graph, OutputLogger logger) {
		logNodes(graph, logger, true); 
	}

	public void logNodes(Graph graph, OutputLogger logger, boolean detailed) {
		if(detailed) logger.writeLine("Nodes for "+graph.getName());
		for(INode node : graph.getNodes()) {
			String line = node.toString();
			if(detailed) {
				int degree = node.getDegree();
				line += " degree: "+degree;
				
				if(node instanceof PartitionNode) {
					line += " oddness: "+((PartitionNode) node).getPartition().getOddness();
				}

				if(node instanceof PartitionNode) {
					line += " rank: "+((PartitionNode) node).getPartition().getRank();
				}
			}
			logger.writeLine(line);
		}
		logger.writeLine("");
	}

	public void logClusteringCoefficient(Graph graph, OutputLogger logger) {
		logger.writeLine("Clustering coefficient for "+graph.getName());
		logger.writeLine(""+graph.getClusteringCoefficientUsingMatrix());
		logger.writeLine("");
	}

	public void logCharacteristicPathLength(Graph graph, OutputLogger logger) {
		logger.writeLine("Characteristic path length for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getCharacteristicPathLength()));
		logger.writeLine("");
	}

	public void logAverageEfficiency(Graph graph, OutputLogger logger) {
		logger.writeLine("Average efficiency for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getAverageEfficiency()));
		logger.writeLine("");
	}

	public void logEnergy(Graph graph, OutputLogger logger) {
		logger.writeLine("Energy for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getEnergy()));
		logger.writeLine("");
	}

	public void logCheegerConstant(Graph graph, OutputLogger logger) {
		logger.writeLine("Cheeger for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getCheegerConstant()));
		logger.writeLine("");
	}
	
	public void logGlobalOverlapping(Graph graph, Set<Graph> cliques, OutputLogger logger) {
		logger.writeLine("Global Overlapping for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getGlobalOverlapping(cliques)));
		logger.writeLine("");
	}
	
	public void logHammingDistance(Graph graph1, Graph graph2, OutputLogger logger) {
		logger.writeLine("Hamming distance between "+graph1.getName()+" and "+graph2.getName());
		logger.writeLine(""+df4.format(graph1.getHammingDistance(graph2)));
		logger.writeLine("");
	}

	public void logAverageRank(Graph graph, OutputLogger logger) {
		logger.writeLine("Average rank for "+graph.getName());
		logger.writeLine(""+df4.format(graph.getAverageRank()));
		logger.writeLine("");
	}
	
	public void logPayoutMatrix(Graph graphA, Graph graphB, OutputLogger logger) {
		int columnWidth = 5;
		String sumHeaderName = "Sum";
		logger.writeLine("Payout Matrix:");
    	
		// header
    	String line = StringUtils.leftPad("", columnWidth, " ");
    	int bSize = graphB.getNodes().size();
    	for(int i=1; i<=bSize; i++) {
    		line += StringUtils.leftPad("B"+i, columnWidth, " ");
    	}
    	line += StringUtils.leftPad(""+sumHeaderName, columnWidth, " ");
    	logger.writeLine(line);
    	
    	int[] bResults = new int[bSize];
    	int aCount = 0;
    	for(INode aNode : graphA.getNodes()) {
    		line = StringUtils.leftPad("A"+(++aCount), columnWidth, " ");
    		int sumResult = 0;
    		int bCount = 0;
        	for(INode bNode : graphB.getNodes()) {
        		int sum = 0;
        		for(int i=0; i<Math.min(((PartitionNode) aNode).getSummands().size(), ((PartitionNode) bNode).getSummands().size()); i++) {
        			double aSummand = ((PartitionNode) aNode).getSummands().get(i);
        			double bSummand = ((PartitionNode) bNode).getSummands().get(i);
        			
        			if(aSummand > bSummand) {
        				sum += 1;
        			} else if(aSummand < bSummand) {
        				sum -= 1;
        			}
        		}
        		int result = sum > 0 ? 1 : sum < 0 ? -1 :0;
        		line += StringUtils.leftPad(""+result, columnWidth, " ");
        		sumResult += result;
        		
        		bResults[bCount++] += result;
        	}
    		line += StringUtils.leftPad(""+sumResult, columnWidth, " ");
        	logger.writeLine(line);
    	}
    	
		// footer
    	line = StringUtils.leftPad(sumHeaderName, columnWidth, " ");
    	for(int i=1; i<=bSize; i++) {
    		line += StringUtils.leftPad(""+bResults[i-1], columnWidth, " ");
    	}
    	logger.writeLine(line);
    	
    	logger.writeLine("");
	}
	
	public AverageAndStdDev logDistanceDistribution(Graph graphA, Graph graphB, OutputLogger logger) {
		List<INode> nodes = new ArrayList<>(graphA.getNodes());
		nodes.addAll(graphB.getNodes());
		
		List<Integer> allDistances = new ArrayList<>();
				
		Map<Integer, AtomicInteger> distanceCounts = new HashMap<>();
		for(int i=0; i<nodes.size(); i++) {
			List<Double> aSummands = ((PartitionNode) nodes.get(i)).getSummands();
			for(int j=0; j<nodes.size(); j++) {
				List<Double> bSummands = ((PartitionNode) nodes.get(j)).getSummands();
				
				for(int k=0; k<Math.min(aSummands.size(), bSummands.size()); k++) {
					int distance = (int) Math.round(Math.abs(aSummands.get(k) - bSummands.get(k)));
					
					allDistances.add(distance);
					
					AtomicInteger count = distanceCounts.get(distance);
					if(count == null) {
						count = new AtomicInteger();
					}
					count.incrementAndGet();
					distanceCounts.put(distance, count);
				}
			}			
		}

		int totalCount = distanceCounts.values().stream().map(count -> count.get()).reduce(0, (a, b) -> a + b);
		List<Integer> distances = new ArrayList<>(distanceCounts.keySet());
		Collections.sort(distances);
		
		AverageAndStdDev averageAndStdDev = getAverageAndStdDev(allDistances);
		
		logger.writeLine("Distance distribution");
		distances.forEach(distance -> {
			logger.writeLine(distance+" "+distanceCounts.get(distance)+" "+df4.format(1.0*distanceCounts.get(distance).get()/totalCount));
		});
		logger.writeLine(averageAndStdDev.toString());
    	logger.writeLine("");

    	return averageAndStdDev;
	}
	
	public void logConfidenceInterval(String name, List<Double> measurments, int numberOfRuns, OutputLogger logger) {
		Map<Double, Double> zs = new LinkedHashMap<>();
		zs.put(80., 1.282);
		zs.put(85., 1.440);
		zs.put(90., 1.645);
		zs.put(95., 1.960);
		zs.put(99., 2.576);
		zs.put(99.5, 2.807);
		zs.put(99.9, 3.291);
		
		AverageAndStdDev averageAndStdDev = getAverageAndStdDev(measurments);
		
		logger.writeLine("Confidence Intervals for average "+name+" of "+df4.format(averageAndStdDev.getAverage())+":");
		zs.keySet().forEach(confidenceInterval -> {
			double z = zs.get(confidenceInterval);
			double interval = z * averageAndStdDev.getStdDev() / Math.sqrt(numberOfRuns);
			logger.writeLine(df2.format(confidenceInterval)+"% +/-"+df4.format(interval));
		});
		logger.writeLine("");
	}
}
