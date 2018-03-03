package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment2 extends BaseExperiment {
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
	private boolean logDistributionOfCliques;
	private boolean logCoalitionResource;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;

	public void run() throws IOException {
    	OutputLogger logger = new OutputLogger("output.txt");
    	logger.writeLine("Running experiment 2");
    	logger.writeLine("");
    	
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
        		int d = head.distanceTo(partition);
        		if(d <= 0 || d > distance) {
        			continue;
        		}
        	
        		familyOfTheHead.add(partition);
        	}
        	partitions = familyOfTheHead;
    	}
    	
    	Random random = new Random();
    	
    	if(numberOfRuns<=1) {
	    	Node node = new Node(partitions.get(random.nextInt(partitions.size())));
	    	Graph graph = new Graph(sumOfDigits+" - "+numberOfDigits+" graph", distance);
	    	graph.addNode(node);
	    	
	    	for(int pick=1; pick<=numberOfRandomPicks; pick++) {
	    		node = new Node(partitions.get(random.nextInt(partitions.size())));
	        	graph.addNode(node);
	    	}
	
	    	if(logNodes) {
	    		logNodes(graph, logger);
	    	}
	    	
	    	if(logMatrix) {
	    		logMatrix(graph, logger);
	    	}
	    	
	    	if(logClusteringCoefficient) {
	    		logClusteringCoefficient(graph, logger);
	    	}
	    	
	    	if(logCoalitionResource) {
	    		logCoalitionResource(graph, logger);
	    	}

	    	if(logStatsOfDegrees) {
	    		logStatsOfDegrees(graph, logger); 
	    	}
			
	    	if(logCliques || logDistributionOfCliques) {
	    		Set<Graph> cliques = graph.getCliques();
	    		
	    		if(logCliques) {
	    			logCliques(cliques, logger);
	    		}
	    	
	    		if(logDistributionOfCliques) {
	    			logDistributionOfCliques(cliques, logger);
	    		}
	    	}
	    	
	    	if(logDiameter) {
	    		logDiameter(graph, logger);
	    	}
	    	
	    	if(logDensityAdjacentMatrix) {
	    		logDensityAdjacentMatrix(graph, logger);
	    	}

    	} else {
    		List<Double> clusteringCoefficients = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> nodeDegreeDistributions = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> cliqueSizeDistributions = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> cliqueCountDistributions = new ArrayList<>();
    		
    		for(int run=1; run<=numberOfRuns; run++) {
		    	Node node = new Node(partitions.get(random.nextInt(partitions.size())));
		    	Graph graph = new Graph(sumOfDigits+" - "+numberOfDigits+" graph", distance);
		    	graph.addNode(node);
		    	
		    	for(int pick=1; pick<=numberOfRandomPicks; pick++) {
		    		node = new Node(partitions.get(random.nextInt(partitions.size())));
		        	graph.addNode(node);
		    	}

		    	if(logClusteringCoefficient) {
		    		clusteringCoefficients.add(graph.getClusteringCoefficientUsingMatrix());
		    	}
		    	
		    	if(logStatsOfDegrees) {
		    		nodeDegreeDistributions.add(graph.getNodeDegreeDistribution());
		    	}

		    	if(logDistributionOfCliques) {
		    		Set<Graph> cliques = graph.getCliques();
		    		cliqueSizeDistributions.add(getCliqueSizeDistribution(cliques));
		    		cliqueCountDistributions.add(this.getCliquesCountBySize(cliques));
		    	}
    		}

	    	if(logClusteringCoefficient) {
	    		AverageAndStdDev averageStdDev = getAverageAndStdDev(clusteringCoefficients);
	    		logger.writeLine("Clustering coefficient:");
	    		logger.writeLine(averageStdDev.toString());
	    		logger.writeLine("");
	    	}

	    	if(logStatsOfDegrees) {
	    		Map<Integer, AverageAndStdDev> results = merge(nodeDegreeDistributions);
	    		logger.writeLine("Distribution of nodes:");
	    		for(int degree : results.keySet()) {
	    			logger.writeLine(degree+" "+results.get(degree));
	    		}
	    		logger.writeLine("");
	    	}

	    	if(logDistributionOfCliques) {
	    		Map<Integer, AverageAndStdDev> cliqueSizeDistributionResults = merge(cliqueSizeDistributions);
	    		Map<Integer, AverageAndStdDev> cliqueCountDistributionResults = merge(cliqueCountDistributions);
	    		logger.writeLine("Distribution of cliques:");
	    		for(int degree : cliqueSizeDistributionResults.keySet()) {
	    			logger.writeLine(degree+" "+cliqueCountDistributionResults.get(degree)+" "+cliqueSizeDistributionResults.get(degree));
	    		}
	    		logger.writeLine("");
	    	}
    	}
    	
    	logger.close();
	}
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream("input.properties");

			// load a properties file
			Properties properties = new Properties();
			properties.load(input);

			numberOfRuns = readProperty(properties, "K", 10);
			numberOfRandomPicks = readProperty(properties, "T", 100);
			numberOfDigits = readProperty(properties, "m", 4);
			sumOfDigits = readProperty(properties, "n", 16);
			distance = readProperty(properties, "d", 1);

			onlyFamilyOfTheHead = readProperty(properties, "onlyFamilyOfTheHead", false);
			logNodes = readProperty(properties, "logNodes", false);
			logMatrix = readProperty(properties, "logMatrix", false);
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false); 
			logCliques = readProperty(properties, "logCliques", false);
			logDistributionOfCliques = readProperty(properties, "logDistributionOfCliques", false); 
			logCoalitionResource = readProperty(properties, "logCoalitionResource", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
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
