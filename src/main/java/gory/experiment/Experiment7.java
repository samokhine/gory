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

public class Experiment7 extends BaseExperiment {
	private static final String PROPERTIES_FILE = "experiment7.properties";
	
	private List<Partition> partitions = new ArrayList<>();
	private int numPartitionsToPick;
	private int distance;
	private int numberOfRuns;

	private boolean logClusteringCoefficient;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logCharacteristicPathLength;

	@Override
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 7");
    	logger.writeLine("");
    	
    	readParameters(logger);
    	if(numPartitionsToPick <= 0 || numPartitionsToPick > partitions.size()) {
        	logger.writeLine("numPartitionsToPick should be a positive number between 1 and the size of the partitions");
    		return;
    	}
    	
    	partitions.stream().forEach(partition -> partition.normilize());
    	
    	Random rand = new Random();
    	
    	List<Double> clusteringCoefficients = new ArrayList<>();
    	List<Integer> diameters = new ArrayList<>();
    	List<Double> densities = new ArrayList<>();
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
	    	
	    	if(logClusteringCoefficient) {
	    		clusteringCoefficients.add(graph.getClusteringCoefficientUsingMatrix());
	    	}
	    	if(logDiameter) {
	    		diameters.add(graph.getDiameter());
	    	}
	    	if(logDensityAdjacentMatrix) {
	    		densities.add(graph.getDensityAdjacentMatrix());
	    	}
	    	if(logCharacteristicPathLength) {
	    		characteristicPathLengths.add(graph.getCharacteristicPathLength());
	    	}
    	}

    	if(logClusteringCoefficient) {
    		logger.writeLine("Clustering coefficient:");
    		logger.writeLine(""+clusteringCoefficients.stream().mapToDouble(d -> d).average().getAsDouble());
    		logger.writeLine("");
    	}

    	if(logDiameter) {
    		logger.writeLine("Diameter:");
    		logger.writeLine(""+diameters.stream().mapToInt(d -> d).average().getAsDouble());
    		logger.writeLine("");
    	}
    	
    	if(logDensityAdjacentMatrix) {
    		logger.writeLine("DAM:");
    		logger.writeLine(""+df4.format(densities.stream().mapToDouble(d -> d).average().getAsDouble()));
    		logger.writeLine("");
    	}
    	if(logCharacteristicPathLength) {
    		logger.writeLine("Characteristic path length:");
    		logger.writeLine(""+df4.format(characteristicPathLengths.stream().mapToDouble(d -> d).average().getAsDouble()));
    		logger.writeLine("");
    	}
	}

	private Properties readParameters(OutputLogger logger) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(PROPERTIES_FILE);

			properties.load(input);
			
			partitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("partitions")));
			numPartitionsToPick = readProperty(properties, "numPartitionsToPick", 10);
			numberOfRuns = readProperty(properties, "numberOfRuns", 20);
			distance = readProperty(properties, "distance", 1);

			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			logCharacteristicPathLength = readProperty(properties, "logCharacteristicPathLength", false);
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
