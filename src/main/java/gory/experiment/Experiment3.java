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
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment3 extends BaseExperiment {
	private int numberOfRuns; 
	private int numberOfSteps; 
	private double probabilityOfMistake; 
	private int distance; // d
	private int numberOfDigits; // m
	
	private boolean removeHead;
	private boolean logNodes;
	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logStatsOfDegrees; 
	private boolean logDistributionOfCliques;
	private boolean logCoalitionResource;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logGlobalOverlapping;
	private boolean logCharacteristicPathLength;
	private boolean logAverageEfficiency;
	private boolean logEnergy;
	private boolean logCheegerConstant;
	private boolean logAverageRank;
	private boolean displayGraph;
	private boolean saveGraphInDotFormat;

	private boolean logCliques;
	private boolean logNodesByCliques;
	private boolean logDensityAdjacentMatrixForGraphOfCliques;
	private boolean displayGraphOfCliques;
	private boolean saveGraphOfCliquesInDotFormat;

	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 3");
    	logger.writeLine("");
    	
    	readParameters();
		
    	Random random = new Random();
    	for(int run=1; run<=numberOfRuns; run++) {
    		List<Map<Integer, ? extends Number>> nodeDegreeDistributions = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> cliqueCountDistributions = new ArrayList<>();
    		List<Double> densityAdjacentMatrixDistributions = new ArrayList<>();

    		List<Partition> prevPartitions = new ArrayList<>();
	    	int highDigit = 2*numberOfDigits-1;
	    	for(Partition partition : PartitionBuilder.build(numberOfDigits*numberOfDigits, numberOfDigits)) {
	    		if(Math.abs(highDigit - partition.getSummands().get(0))>1) continue;
	    		
	    		prevPartitions.add(partition);
	    	}
			
	    	List<Integer> summands = new ArrayList<>();
	    	for(int j=1; j<=2*numberOfDigits-1; j=j+2) {
	    		summands.add(j);
	    	}
	    	Partition prevHead = new Partition(summands);
     		
    		int initialNumberOfDigits = numberOfDigits;
	    	for(int step=1; step<=numberOfSteps; step++) {
	    		initialNumberOfDigits++;
	
	    		summands = new ArrayList<>();
	        	for(int j=1; j<=2*initialNumberOfDigits-1; j=j+2) {
	        		summands.add(j);
	        	}
	        	Partition head = new Partition(summands);
	
	        	Graph graph = new Graph("Graph for step #"+step, distance);
	        	graph.addNode(new PartitionNode(head));
	        	
	        	List<Partition> partitions = new ArrayList<>();
	        	
	        	highDigit = 2*initialNumberOfDigits-1;
	        	for(Partition prevPartition : prevPartitions) {
	        		if(prevPartition.equals(prevHead)) continue;
	        		
	        		summands = new ArrayList<>();
	    			summands.add(generateHighDigit(highDigit, random));
	        		summands.addAll(prevPartition.getSummands());
	        		
	        		partitions.add(new Partition(summands));
	        	}
	
	        	List<Partition> rightPartitions = new ArrayList<>();
	        	highDigit = 2*initialNumberOfDigits;
	        	for(Partition partition : partitions) {
	        		summands = new ArrayList<>();
	    			summands.add(generateHighDigit(highDigit, random));
	
	    			int index = random.nextInt(initialNumberOfDigits-1)+1;
	    			for(int j=1; j<initialNumberOfDigits; j++) {
	    				int summond = partition.getSummands().get(j);
	    				if(j == index && summond>0) {
	    					summond--;
	    				}
	    				summands.add(summond);
	    			}
	        		rightPartitions.add(new Partition(summands));
	        	}    	
	
	        	List<Partition> leftPartitions = new ArrayList<>();
	        	highDigit = 2*initialNumberOfDigits - 2;
	        	for(Partition partition : partitions) {
	        		summands = new ArrayList<>();
	    			summands.add(generateHighDigit(highDigit, random));
	
	    			int index = random.nextInt(initialNumberOfDigits-1)+1;
	    			for(int j=1; j<initialNumberOfDigits; j++) {
	    				int summond = partition.getSummands().get(j);
	    				if(j == index && summond>0) {
	    					summond++;
	    				}
	    				summands.add(summond);
	    			}
	        		leftPartitions.add(new Partition(summands));
	        	}  
	        	
	        	partitions.addAll(rightPartitions);
	        	partitions.addAll(leftPartitions);
	        	
	        	for(Partition partition : partitions) {
	        		graph.addNode(new PartitionNode(partition));
	        	} 
	        	
		    	if(removeHead) {
		    		graph.removeNode(new PartitionNode(head));
		    	}
	        	
	        	prevPartitions = partitions;
	        	prevHead = head;
	        	
	        	Set<Graph> cliques = graph.getCliques();
	        	
	        	nodeDegreeDistributions.add(graph.getNodeDegreeDistribution());
	        	cliqueCountDistributions.add(getCliquesCountBySize(cliques));
	        	densityAdjacentMatrixDistributions.add(graph.getDensityAdjacentMatrix());
	        	
	        	if(step == numberOfSteps) {
		        	logger.writeLine("Run #"+run+" Step #"+step);
		        	logger.writeLine("");
		        	
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
		    		
		    		if(logCliques) {
		    			logCliques(graph, cliques, logger);
		    		}
		    	
		    		if(logDistributionOfCliques) {
		    			logDistributionOfCliques(cliques, logger);
		    		}
		    		
		    		if(logNodesByCliques) {
		    			logNodesByCliques(graph, cliques, logger);
		    		}
		        	
		        	if(logDiameter) {
		        		logDiameter(graph, logger);
		        	}
		        	
		        	if(logDensityAdjacentMatrix) {
		        		logDensityAdjacentMatrix(graph, logger);
		        	}
		
		        	if(logDensityAdjacentMatrixForGraphOfCliques) {
		    			Graph graphOfCliques = buildGraphOfCliques(cliques, "Graph of cliques for step #"+step);
		        		logDensityAdjacentMatrix(graphOfCliques, logger);
		        	}
		        	
		        	if(logCharacteristicPathLength) {
		        		logCharacteristicPathLength(graph, logger);
		        	}
		
		        	if(logAverageEfficiency) {
		        		logAverageEfficiency(graph, logger);
		        	}
		
		        	if(logEnergy) {
		        		logEnergy(graph, logger);
		        	}
		
		        	if(logCheegerConstant) {
		        		logCheegerConstant(graph, logger);
		        	}
		
		        	if(logGlobalOverlapping) {
		        		logGlobalOverlapping(graph, cliques, logger);
		        	}
		
		        	if(logAverageRank) {
		        		logAverageRank(graph, logger);
		        	}
		        	
		        	if(displayGraph) {
		        		displayGraph(graph, "graph");
		        	}
		        	
		        	if(saveGraphInDotFormat) {
		        		saveInDotFormat(graph, "graph");
		        	}
		
		        	if(displayGraphOfCliques || saveGraphOfCliquesInDotFormat) {
		        		Graph graphOfCliques = buildGraphOfCliques(cliques, "Graph of cliques");
		
		        		logNotFullyConnectedMatrix(graphOfCliques, logger);
		        		
		        		if(displayGraphOfCliques) {
		        			displayGraph(graphOfCliques, "graphOfCliques");
		        		}
		        		
		        		if(saveGraphOfCliquesInDotFormat) {
		        	   		saveInDotFormat(graphOfCliques, "graphOfCliques");
		        		}
		        	}
		        	
		    		Map<Integer, AverageAndStdDev> results = merge(nodeDegreeDistributions);
		    		logger.writeLine("Distribution of nodes for run #"+run+":");
		    		for(int degree : results.keySet()) {
		    			logger.writeLine(degree+" "+results.get(degree));
		    		}
		    		logger.writeLine("");
		    		
		    		Map<Integer, AverageAndStdDev> cliqueCountDistributionResults = merge(cliqueCountDistributions);
		    		logger.writeLine("Distribution of cliques for run #"+run+":");
		    		for(int degree : cliqueCountDistributionResults.keySet()) {
		    			logger.writeLine(degree+" "+cliqueCountDistributionResults.get(degree));
		    		}
		    		logger.writeLine("");

		    		logger.writeLine("Distribution of DAM for run #"+run+":");
		    		logger.writeLine(""+getAverageAndStdDev(densityAdjacentMatrixDistributions));
		    		logger.writeLine("");
	        	}
	    	}
    	}
	}

	private int generateHighDigit(int highDigit, Random random) {
		int nextInt = random.nextInt(100);
		if(nextInt <= probabilityOfMistake*100) { // mistake
			boolean isPlus = random.nextBoolean();
			return highDigit + (isPlus ? 1 : -1);
		} else {
			return highDigit;
		}
	}
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream("experiment3.properties");

			// load a properties file
			Properties properties = new Properties();
			properties.load(input);

			numberOfRuns = readProperty(properties, "numberOfRuns", 1);
			numberOfSteps = readProperty(properties, "numberOfSteps", 1);
			probabilityOfMistake = readProperty(properties, "probabilityOfMistake", 0.1);
			numberOfDigits = readProperty(properties, "m", 4);
			distance = readProperty(properties, "d", 1);

			removeHead = readProperty(properties, "removeHead", false);
			logNodes = readProperty(properties, "logNodes", false);
			logMatrix = readProperty(properties, "logMatrix", false);
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false); 
			logCliques = readProperty(properties, "logCliques", false);
			logDistributionOfCliques = readProperty(properties, "logDistributionOfCliques", false); 
			logCoalitionResource = readProperty(properties, "logCoalitionResource", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			logDensityAdjacentMatrixForGraphOfCliques = readProperty(properties, "logDensityAdjacentMatrixForGraphOfCliques", false);
			logNodesByCliques = readProperty(properties, "logNodesByCliques", false);
			logCharacteristicPathLength = readProperty(properties, "logCharacteristicPathLength", false);
			logAverageEfficiency = readProperty(properties, "logAverageEfficiency", false);
			logEnergy = readProperty(properties, "logEnergy", false);
			logCheegerConstant = readProperty(properties, "logCheegerConstant", false);
			logGlobalOverlapping = readProperty(properties, "logGlobalOverlapping", false);
			logAverageRank = readProperty(properties, "logAverageRank", false);
			
			displayGraph = readProperty(properties, "displayGraph", false);
			displayGraphOfCliques = readProperty(properties, "displayGraphOfCliques", false);
			saveGraphInDotFormat = readProperty(properties, "saveGraphInDotFormat", false);
			saveGraphOfCliquesInDotFormat = readProperty(properties, "saveGraphOfCliquesInDotFormat", false);
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
