package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment4 extends BaseExperiment {
	private int distance; // d
	private int numberOfDigits; // m
	
	private boolean deleteAnyClique;
	private int deleteAnyCliqueOfSize;
	private int diameterThreshold;
	private boolean logNodes;
	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logStatsOfDegrees; 
	private boolean logCliques;
	private boolean logDistributionOfCliques;
	private boolean logCoalitionResource;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean displayGraph;
	private boolean displayGraphOfCliques;
	
	public void run() throws IOException {
    	OutputLogger logger = new OutputLogger("output.txt");
    	logger.writeLine("Running experiment 4");
    	logger.writeLine("");
    	
    	readParameters();
		
    	Graph graph = new Graph(numberOfDigits*numberOfDigits+" - "+numberOfDigits+" graph", distance);
    	List<Partition> partitions = PartitionBuilder.build(numberOfDigits*numberOfDigits, numberOfDigits);
    	for(Partition partition : partitions) {
    		graph.addNode(new Node(partition));
    	}
    	
    	List<Graph> cliques = null;
    	int numSteps = 0;
		while(true) {
			cliques = new ArrayList<>(graph.getCliques());

			int diameter = graph.getDiameter();
			if(diameter >= diameterThreshold) {
				break;
			}

    		numSteps ++;

    		if(deleteAnyClique) {
				int randomIndex = ThreadLocalRandom.current().nextInt(0, cliques.size());
				Graph randomClique = cliques.get(randomIndex);
				for(Node node : randomClique.getNodes()) {
					graph.removeNode(node);
				}
    		} else if(deleteAnyCliqueOfSize>0) {
    			Map<Integer, List<Graph>> cliquesBySize = new TreeMap<>(); // sorted by key which is size
    			
    			for(Graph clique : cliques) {
    				int size = clique.getSize();
    				List<Graph> cliquesOfSize = cliquesBySize.get(size);
    				if(cliquesOfSize == null) {
    					cliquesOfSize = new ArrayList<>();
    					cliquesBySize.put(size, cliquesOfSize);
    				}
    				cliquesOfSize.add(clique);
    			}

    			if(cliquesBySize.get(deleteAnyCliqueOfSize) == null) {
    				break;
    			} else {
	    			Integer numCliquesOfSize = cliquesBySize.get(deleteAnyCliqueOfSize).size();
    				int randomIndex = ThreadLocalRandom.current().nextInt(0, numCliquesOfSize);
    				Graph randomClique = cliquesBySize.get(deleteAnyCliqueOfSize).get(randomIndex);
    				for(Node node : randomClique.getNodes()) {
    					graph.removeNode(node);
    				}
    			}
    		} else {
    			break;
    		}
		}
		
		logger.writeLine("Number of steps: "+numSteps);
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

    	if(logStatsOfDegrees) {
    		logStatsOfDegrees(graph, logger); 
    	}
		
		if(logCliques) {
			logCliques(graph, new HashSet<>(cliques), logger);
		}
	
		if(logDistributionOfCliques) {
			logDistributionOfCliques(new HashSet<>(cliques), logger);
		}

    	if(logCoalitionResource) {
    		logCoalitionResource(graph, logger);
    	}

    	if(logDiameter) {
    		logDiameter(graph, logger);
    	}
    	
    	if(logDensityAdjacentMatrix) {
    		logDensityAdjacentMatrix(graph, logger);
    	}

    	if(displayGraph) {
    		displayGraph(graph);
    	}
	
    	if(displayGraphOfCliques) {
    		displayGraphOfCliques(new HashSet<>(cliques));
    	}
	}
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream("input.properties");

			// load a properties file
			Properties properties = new Properties();
			properties.load(input);

			numberOfDigits = readProperty(properties, "m", 4);
			distance = readProperty(properties, "d", 1);

			deleteAnyClique = readProperty(properties, "deleteAnyClique", false);
			deleteAnyCliqueOfSize = readProperty(properties, "deleteAnyCliqueOfSize", 0);
			diameterThreshold = readProperty(properties, "diameterThreshold", 0);
			logNodes = readProperty(properties, "logNodes", false);
			logMatrix = readProperty(properties, "logMatrix", false);
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false); 
			logCliques = readProperty(properties, "logCliques", false);
			logDistributionOfCliques = readProperty(properties, "logDistributionOfCliques", false); 
			logCoalitionResource = readProperty(properties, "logCoalitionResource", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			
			displayGraph = readProperty(properties, "displayGraph", false);
			displayGraphOfCliques = readProperty(properties, "displayGraphOfCliques", false);
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
