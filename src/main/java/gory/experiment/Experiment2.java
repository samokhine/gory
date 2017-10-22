package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Experiment2 implements Experiment {
	private static final DecimalFormat df = new DecimalFormat("0.0000");

	private int numberOfRuns; // K
	private int numberOfRandomPicks; // T
	private int numberOfDigits; // m
	private int sumOfDigits; // n
	private int distance; // d
	private boolean onlyFamilyOfTheHead;
	
	private boolean logNodes;
	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logStatsOfDegrees; 
	private boolean logCliques;

	public void run() throws IOException {
    	OutputLogger out = new OutputLogger("output.txt");
    	out.writeLine("Running experiment 2");
    	out.writeLine("");
    	
    	readParameters();
		
    	List<Partition> partitions = PartitionBuilder.build(sumOfDigits, numberOfDigits);
    	if(onlyFamilyOfTheHead) {
    		List<Integer> summands = new ArrayList<>();
        	for(int i=1; i<=2*numberOfDigits-1; i=i+2) {
        		summands.add(i);
        	}
        	
        	Partition head = new Partition(summands);
        	List<Partition> familyOfTheHead = new ArrayList<>();
        	for(Partition partition : partitions) {
        		if(partition.distanceTo(head)>distance) continue; 
        	
        		familyOfTheHead.add(partition);
        	}
        	partitions = familyOfTheHead;
    	}
    	
    	Random generator = new Random();
    	
    	if(numberOfRuns<=1) {
	    	Node node = new Node(partitions.get(generator.nextInt(partitions.size())));
	    	Graph graph = new Graph(sumOfDigits+" - "+numberOfDigits+" graph", distance);
	    	graph.addNode(node);
	    	
	    	for(int pick=1; pick<=numberOfRandomPicks; pick++) {
	    		node = new Node(partitions.get(generator.nextInt(partitions.size())));
	        	graph.addNode(node);
	    	}
	
	    	if(logNodes) {
	    		graph.logNodes(out);
	    	}
	    	
	    	if(logMatrix) {
	    		graph.logMatrix(out);
	    	}
	    	
	    	if(logClusteringCoefficient) {
	    		graph.logClusteringCoefficient(out);
	    	}
	    	
	    	if(logStatsOfDegrees) {
	    		graph.logStatsOfDegrees(out); 
	    	}
			
	    	if(logCliques) {
	    		graph.logCliques(out);
	    	}
    	} else {
    		List<Double> clusteringCoefficients = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> nodeDegreeDistributions = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> cliqueSizeDistributions = new ArrayList<>();
    		
    		for(int run=1; run<=numberOfRuns; run++) {
		    	Node node = new Node(partitions.get(generator.nextInt(partitions.size())));
		    	Graph graph = new Graph(sumOfDigits+" - "+numberOfDigits+" graph", distance);
		    	graph.addNode(node);
		    	
		    	for(int pick=1; pick<=numberOfRandomPicks; pick++) {
		    		node = new Node(partitions.get(generator.nextInt(partitions.size())));
		        	graph.addNode(node);
		    	}

		    	if(logClusteringCoefficient) {
		    		clusteringCoefficients.add(graph.getClusteringCoefficientUsingMatrix());
		    	}
		    	
		    	if(logStatsOfDegrees) {
		    		nodeDegreeDistributions.add(graph.getNodeDegreeDistribution());
		    	}

		    	if(logCliques) {
		    		cliqueSizeDistributions.add(graph.getCliqueSizeDistribution());
		    	}
    		}

	    	if(logClusteringCoefficient) {
	    		AverageAndStdDev averageStdDev = getAverageAndStdDev(clusteringCoefficients);
	    		out.writeLine("Clustering coefficient for graph:");
	    		out.writeLine(averageStdDev.toString());
	    		out.writeLine("");
	    	}

	    	if(logStatsOfDegrees) {
	    		Map<Integer, AverageAndStdDev> results = merge(nodeDegreeDistributions);
	    		out.writeLine("Distribution of nodes:");
	    		for(int degree : results.keySet()) {
	    			out.writeLine(degree+" "+results.get(degree));
	    		}
	    		out.writeLine("");
	    	}

	    	if(logCliques) {
	    		Map<Integer, AverageAndStdDev> results = merge(cliqueSizeDistributions);
	    		out.writeLine("Distribution of cliques:");
	    		for(int degree : results.keySet()) {
	    			out.writeLine(degree+" "+results.get(degree));
	    		}
	    		out.writeLine("");
	    	}
    	}
    	
    	out.close();
	}
	
	@AllArgsConstructor
	@Getter
	private static class AverageAndStdDev {
		double average;
		double stdDev;
		
		@Override
		public String toString() {
			return df.format(average)+ "("+df.format(stdDev)+")";
		}
	}
	
	private AverageAndStdDev getAverageAndStdDev(List<Double> items) {
		double average = 0;
		double stdDev = 0;
		
		if(items.size() > 0) {
			for(double item : items) {
				average += item;
			}
			average /= items.size();

			if(items.size() > 1) {
				for(double item : items) {
					stdDev += (item - average) * (item - average);
				}
				
				stdDev /= (items.size() - 1);
				
				stdDev = Math.sqrt(stdDev);
			}
		}
		
		return new AverageAndStdDev(average, stdDev);
		
	}
	
	private Map<Integer, AverageAndStdDev> merge(List<Map<Integer, ? extends Number>> items) {
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
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream("input.properties");

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			numberOfRuns = Integer.valueOf(prop.getProperty("K"));
			numberOfRandomPicks = Integer.valueOf(prop.getProperty("T"));
			numberOfDigits = Integer.valueOf(prop.getProperty("m"));
			sumOfDigits = Integer.valueOf(prop.getProperty("n"));
			distance = Integer.valueOf(prop.getProperty("d"));
			onlyFamilyOfTheHead = Boolean.valueOf(prop.getProperty("onlyFamilyOfTheHead"));
			
			logNodes = Boolean.valueOf(prop.getProperty("logNodes"));
			logMatrix = Boolean.valueOf(prop.getProperty("logMatrix"));
			logClusteringCoefficient = Boolean.valueOf(prop.getProperty("logClusteringCoefficient"));
			logStatsOfDegrees = Boolean.valueOf(prop.getProperty("logStatsOfDegrees")); 
			logCliques = Boolean.valueOf(prop.getProperty("logCliques"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
