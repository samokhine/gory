package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import gory.domain.Graph;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment7 extends BaseExperiment {
	private static final String PROPERTIES_FILE = "experiment7.properties";
	
	private List<Partition> playerApartitions = new ArrayList<>();
	private List<Partition> playerBpartitions = new ArrayList<>();

	private int numPartitionsToPick;
	private int distance;
	private int numberOfRuns;

	private boolean logClusteringCoefficient;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logDensityAdjacentMatrixOnlyConnected;
	private boolean logCharacteristicPathLength;
	private boolean logStatsOfDegrees;
	
	private boolean displayGraph;

	@Override
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 7");
    	logger.writeLine("");
    	
    	readParameters(logger);
    	
    	processGraph("Player A", playerApartitions, logger);
    	processGraph("Player B", playerBpartitions, logger);
    	
    	Random rand = new Random();
    	
    	List<Partition> partitions = PartitionBuilder.build(100, 10);
    	List<Double> clusteringCoefficients = new ArrayList<>();
    	List<Integer> diameters = new ArrayList<>();
    	List<Double> densities = new ArrayList<>();
    	List<Double> densitiesOnlyConnected = new ArrayList<>();
    	List<Double> characteristicPathLengths = new ArrayList<>();
    	for(int i=0; i<numberOfRuns; i++) {
	    	List<Partition> selectedPartitions = new ArrayList<>();
	    	int cnt = 0;
	    	List<Partition> clone = new ArrayList<>(partitions);
	    	while(cnt<numPartitionsToPick) {
	    		int index = rand.nextInt(clone.size());
	    		selectedPartitions.add(clone.remove(index));
	    		cnt++;
	    	}
	    	
			Graph graph = new Graph("Graph", distance);
			
	    	for(Partition partition : selectedPartitions) {
	    		graph.addNode(new PartitionNode(partition));
	    	}
	    	
	    	/*
	    	boolean hasDisconnectedNodes = false;
	    	for(INode node : graph.getNodes()) {
	    		if(node.getConnectedNodes().isEmpty()) {
	    			hasDisconnectedNodes = true;
	    			break;
	    		}
	    	}
	    	if(hasDisconnectedNodes) continue;
	    	*/
	    	
	    	if(logClusteringCoefficient) {
	    		clusteringCoefficients.add(graph.getClusteringCoefficientUsingMatrix());
	    	}
	    	if(logDiameter) {
	    		diameters.add(graph.getDiameter());
	    	}
	    	if(logDensityAdjacentMatrix) {
	    		densities.add(graph.getDensityAdjacentMatrix());
	    	}
	    	if(logDensityAdjacentMatrixOnlyConnected) {
	    		densitiesOnlyConnected.add(graph.getDensityAdjacentMatrix(true));
	    	}
	    	if(logCharacteristicPathLength) {
	    		characteristicPathLengths.add(graph.getCharacteristicPathLength());
	    	}
    	}

    	if(logClusteringCoefficient) {
    		logger.writeLine("Clustering coefficient for random graph:");
    		logger.writeLine(""+clusteringCoefficients.stream().mapToDouble(d -> d).average().getAsDouble());
    		logger.writeLine("");
    	}

    	if(logDiameter) {
    		logger.writeLine("Diameter for random graph:");
    		logger.writeLine(""+diameters.stream().mapToInt(d -> d).average().getAsDouble());
    		logger.writeLine("");
    	}
    	
    	if(logDensityAdjacentMatrix) {
    		logger.writeLine("DAM for random graph:");
    		logger.writeLine(""+df4.format(densities.stream().mapToDouble(d -> d).average().getAsDouble()));
    		logger.writeLine("");
    	}

    	if(logDensityAdjacentMatrixOnlyConnected) {
    		logger.writeLine("DAM (only connected) for random graph:");
    		logger.writeLine(""+df4.format(densitiesOnlyConnected.stream().mapToDouble(d -> d).average().getAsDouble()));
    		logger.writeLine("");
    	}

    	if(logCharacteristicPathLength) {
    		logger.writeLine("Characteristic path length for random graph:");
    		logger.writeLine(""+df4.format(characteristicPathLengths.stream().mapToDouble(d -> d).average().getAsDouble()));
    		logger.writeLine("");
    	}
	}

	private void processGraph(String graphName, List<Partition> partitions, OutputLogger logger) {
    	partitions.stream().forEach(partition -> partition.normilize());

		Graph graph = new Graph(graphName, distance);
		
    	for(Partition partition : partitions) {
    		graph.addNode(new PartitionNode(partition));
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

    	if(logDensityAdjacentMatrixOnlyConnected) {
    		logDensityAdjacentMatrixOnlyConnected(graph, logger);
    	}

    	if(logCharacteristicPathLength) {
    		logCharacteristicPathLength(graph, logger);
    	}

    	if(logStatsOfDegrees) {
    		logStatsOfDegrees(graph, logger);
    	}
    	
    	if(displayGraph) {
    		displayGraph(graph);
    	}
	}
	
	private Properties readParameters(OutputLogger logger) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(PROPERTIES_FILE);

			properties.load(input);
			
			playerApartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("playerApartitions")));
			playerBpartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("playerBpartitions")));

			numPartitionsToPick = readProperty(properties, "numPartitionsToPick", 10);
			numberOfRuns = readProperty(properties, "numberOfRuns", 20);
			distance = readProperty(properties, "distance", 1);

			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			logDensityAdjacentMatrixOnlyConnected = readProperty(properties, "logDensityAdjacentMatrixOnlyConnected", false);
			logCharacteristicPathLength = readProperty(properties, "logCharacteristicPathLength", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false);
			
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
