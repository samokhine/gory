package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;

public class Experiment7 extends BaseExperiment {
	private static final String PROPERTIES_FILE = "experiment7.properties";
	
	private List<Partition> aPartitions = new ArrayList<>();
	private List<Partition> allBPartitions = new ArrayList<>();

	private int numPartitionsToSelect;
	private int distance;
	
	private boolean logNodes;
	private boolean logClusteringCoefficient;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logCharacteristicPathLength;
	private boolean logStatsOfDegrees;
	private boolean logHammingDistance;
	private boolean logEnergy;
	private boolean logCliques;
	private boolean logDistributionOfCliques;
	private boolean logAverageEfficiency;
	private boolean logPayoutMatrix;
	
	private boolean displayGraph;

	@Override
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 7");
    	logger.writeLine("");

    	readParameters(logger);

    	Random random = new Random();
    	List<Partition> selectedBPartitions = new ArrayList<>();
    	while(selectedBPartitions.size()<numPartitionsToSelect) {
    		int index = random.nextInt(allBPartitions.size());
    		selectedBPartitions.add(allBPartitions.remove(index));
    	}
    	
    	Graph graphA = processGraph("Graph A", aPartitions, logger);
    	Graph graphB = processGraph("Graph B", selectedBPartitions, logger);
    	
    	if(logHammingDistance) {
    		logHammingDistance(graphA, graphB, logger);
    	}
    	
    	if(logPayoutMatrix) {
    		logPayoutMatrix(graphA, graphB, logger);
    	}
	}

	private Graph processGraph(String graphName, List<Partition> partitions, OutputLogger logger) {
    	partitions.stream().forEach(partition -> partition.normalize());

		Graph graph = new Graph(graphName, distance);
		
    	for(Partition partition : partitions) {
    		graph.addNode(new PartitionNode(partition));
    	}
    	
    	Set<Graph> cliques = null;
    	if(logCliques || logDistributionOfCliques) {
    		cliques = graph.getCliques();
    	}
    	
    	if(logNodes) {
    		logNodes(graph, logger);
    	}

    	if(logClusteringCoefficient) {
    		logClusteringCoefficient(graph, logger);
    	}
    	
    	if(logDiameter) {
    		logDiameter(graph, logger);
    	}
    	
    	if(logDensityAdjacentMatrix) {
    		logDensityAdjacentMatrix(graph, logger);
    	}

    	if(logCharacteristicPathLength) {
    		logCharacteristicPathLength(graph, logger);
    	}

    	if(logStatsOfDegrees) {
    		logStatsOfDegrees(graph, logger);
    	}
    	
    	if(logEnergy) {
    		logEnergy(graph, logger);
    	}
    	
    	if(logAverageEfficiency) {
    		logAverageEfficiency(graph, logger);
    	}
    	
		if(logCliques) {
			logCliques(graph, cliques, logger);
		}
	
		if(logDistributionOfCliques) {
			logDistributionOfCliques(cliques, logger);
		}

    	if(displayGraph) {
    		displayGraph(graph);
    	}
    	
    	return graph;
	}
	
	private Properties readParameters(OutputLogger logger) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(PROPERTIES_FILE);

			properties.load(input);
			
			aPartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("aPartitions")));
			allBPartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("bPartitions")));

			distance = readProperty(properties, "distance", 1);
			numPartitionsToSelect = readProperty(properties, "numPartitionsToSelect", 10);
			
			logNodes = readProperty(properties, "logNodes", false);
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			logCharacteristicPathLength = readProperty(properties, "logCharacteristicPathLength", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false);
			logHammingDistance = readProperty(properties, "logHammingDistance", false);
			logEnergy = readProperty(properties, "logEnergy", false);
			logCliques = readProperty(properties, "logCliques", false);
			logDistributionOfCliques = readProperty(properties, "logDistributionOfCliques", false); 
			logAverageEfficiency = readProperty(properties, "logAverageEfficiency", false);
			logPayoutMatrix = readProperty(properties, "logPayoutMatrix", false);
			
			displayGraph = readProperty(properties, "displayGraph", false);
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
		
		return properties;
	}
}
