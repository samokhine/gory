package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import gory.domain.Graph;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;

public class Experiment7 extends BaseExperiment {
	private static final String PROPERTIES_FILE = "experiment7.properties";
	
	private List<Partition> playerApartitions = new ArrayList<>();
	private List<Partition> playerBpartitions = new ArrayList<>();

	private int distance;
	
	private boolean logClusteringCoefficient;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
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
	}

	private void processGraph(String graphName, List<Partition> partitions, OutputLogger logger) {
    	partitions.stream().forEach(partition -> partition.normalize());

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

			distance = readProperty(properties, "distance", 1);
			
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
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
