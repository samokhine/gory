package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.BlottoPartitionFilter;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment7 extends BaseExperiment {
	private static final String PROPERTIES_FILE = "experiment7.properties";
	
	/*
	private List<Partition> playerApartitions = new ArrayList<>();
	private List<Partition> playerBpartitions = new ArrayList<>();

	private int numPartitionsToPick;
	private int numberOfRuns;
	*/
	private int distance;
	
	int numPartitionsToBuild;
	int maxNumRepetitions;
	int minResource;
	int maxResource;

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

    	BlottoPartitionFilter blottoPartitionFilter = new BlottoPartitionFilter(maxNumRepetitions, minResource, maxResource);
    	
    	List<Partition> allPartitions = PartitionBuilder.build(100, 10, blottoPartitionFilter);
    	
    	Random random = new Random();
    	Set<Partition> partitions = new HashSet<>();
    	while(partitions.size()<numPartitionsToBuild) {
    		int index = random.nextInt(allPartitions.size());
    		partitions.add(allPartitions.remove(index));
    	}
    	
    	for(Partition partition : partitions) {
        	logger.writeLine(partition.toString());
    	}
    	
    	/*
    	
    	processGraph("Player A", playerApartitions, logger);
    	processGraph("Player B", playerBpartitions, logger);
    	
    	Random rand = new Random();
    	
    	List<Partition> partitions = PartitionBuilder.build(100, 10);
    	List<Double> clusteringCoefficients = new ArrayList<>();
    	List<Integer> diameters = new ArrayList<>();
    	List<Double> densities = new ArrayList<>();
    	List<Double> characteristicPathLengths = new ArrayList<>();
    	int graphsAccepted = 0;
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
	    	
	    	int diameter = graph.getDiameter();
	    	if(diameter == Integer.MAX_VALUE) {
	    		continue;
	    	}
	    	graphsAccepted ++;
	    	
	    	if(logClusteringCoefficient) {
	    		clusteringCoefficients.add(graph.getClusteringCoefficientUsingMatrix());
	    	}
	    	if(logDiameter) {
	    		diameters.add(diameter);
	    	}
	    	if(logDensityAdjacentMatrix) {
	    		densities.add(graph.getDensityAdjacentMatrix());
	    	}
	    	if(logCharacteristicPathLength) {
	    		characteristicPathLengths.add(graph.getCharacteristicPathLength());
	    	}
    	}
    	
    	if(graphsAccepted == 0) {
    		logger.writeLine("Cannot generate a single connected graph. Aborting.");
    		return;
    	} else {
    		logger.writeLine("Found "+graphsAccepted+" connected graph.");
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

    	if(logCharacteristicPathLength) {
    		logger.writeLine("Characteristic path length for random graph:");
    		logger.writeLine(""+df4.format(characteristicPathLengths.stream().mapToDouble(d -> d).average().getAsDouble()));
    		logger.writeLine("");
    	}
    	*/
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
			
	    	numPartitionsToBuild = readProperty(properties, "numPartitionsToBuild", 10);
	    	maxNumRepetitions = readProperty(properties, "maxNumRepetitions", 2);
	    	minResource = readProperty(properties, "minResource", 69);
	    	maxResource = readProperty(properties, "maxResource", 75);
			
			/*
			playerApartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("playerApartitions")));
			playerBpartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("playerBpartitions")));

			numPartitionsToPick = readProperty(properties, "numPartitionsToPick", 10);
			numberOfRuns = readProperty(properties, "numberOfRuns", 20);
			distance = readProperty(properties, "distance", 1);
			*/

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
