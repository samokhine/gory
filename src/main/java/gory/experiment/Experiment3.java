package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment3 extends BaseExperiment {
	private int numberOfRuns; // K
	private int numberOfRandomPicks; // T
	private int distance; // d
	private int numberOfDigits; // m
	
	private boolean logNodes;
	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logStatsOfDegrees; 
	private boolean logCliques;
	private boolean logDistributionOfCliques;
	private boolean logCoalitionResource;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	
	private Set<Partition> deletes = new HashSet<>(); 
	private boolean deleteHead;
	private int deletAllCliquesOfSize;
	private int numberOfCliquesToDelete;
	
	public void run() throws IOException {
    	OutputLogger logger = new OutputLogger("output.txt");
    	logger.writeLine("Running experiment 3");
    	logger.writeLine("");
    	
    	readParameters();
		
		List<Integer> summands = new ArrayList<>();
    	for(int i=1; i<=2*numberOfDigits-1; i=i+2) {
    		summands.add(i);
    	}

    	List<Partition> partitions = PartitionBuilder.build(numberOfDigits*numberOfDigits, numberOfDigits);
    	PartitionNode head = new PartitionNode(new Partition(summands));

    	Random random = new Random();

    	if(numberOfRuns<=1) {
    		Graph graph = buildGraph(head, 
    				partitions, 
    				deletes.isEmpty() ? numberOfRandomPicks : 0,
    				random);
    		
    		if(!deletes.isEmpty()) {
    			for(Partition delete : deletes) {
    				graph.removeNode(new PartitionNode(delete));
    			}
    		} else if(deletAllCliquesOfSize > 0) {
    			graph.deleteAllCliquesOfSize(deletAllCliquesOfSize);
    			if(deleteHead) {
    				graph.removeNode(head);
    			}
	    	} else if(numberOfCliquesToDelete > 0) {
    			graph.deleteNumberOfCliques(numberOfCliquesToDelete);
    			if(deleteHead) {
    				graph.removeNode(head);
    			}
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
	    			logCliques(graph, cliques, logger);
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
    	}  else {
        	List<Double> clusteringCoefficients = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> nodeDegreeDistributions = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> cliqueSizeDistributions = new ArrayList<>();
    		List<Map<Integer, ? extends Number>> cliqueCountDistributions = new ArrayList<>();
    		List<Integer> diameters = new ArrayList<>();
    		
    		for(int run=1; run<=numberOfRuns; run++) {
        		Graph graph = buildGraph(head, partitions, numberOfRandomPicks, random);

        		if(deletAllCliquesOfSize>0) {
        			graph.deleteAllCliquesOfSize(deletAllCliquesOfSize);
        			if(deleteHead) {
        				graph.removeNode(head);
        			}
        		} else if(numberOfCliquesToDelete > 0) {
        			graph.deleteNumberOfCliques(numberOfCliquesToDelete);
        			if(deleteHead) {
        				graph.removeNode(head);
        			}
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
		    	
		    	if(logDiameter) {
		    		diameters.add(graph.getDiameter());
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
	    	
	    	if(logDiameter) {
	    		AverageAndStdDev averageStdDev = getAverageAndStdDev(diameters);
	    		logger.writeLine("Diameter:");
	    		logger.writeLine(averageStdDev.toString());
	    		logger.writeLine("");
	    	}
    	}
    	
    	logger.close();
	}
	
	private Graph buildGraph(PartitionNode head, List<Partition> partitions, int numToDelete, Random random) {
    	Graph graph = new Graph(numberOfDigits*numberOfDigits+" - "+numberOfDigits+" graph", distance);
    	graph.addNode(head);
    	
    	for(Partition partition : partitions) {
    		int d = head.distanceTo(new PartitionNode(partition));
    		if(d <= 0 || d > distance) {
    			continue;
    		}
    		
    		graph.addNode(new PartitionNode(partition));
    	}
	
    	int j = Math.min(numToDelete, graph.getSize());
    	for(int i=0; i<j; i++) {
    		int n = random.nextInt(graph.getSize());
    		INode node = graph.getNode(n);
    		if(node != null) {
    			graph.removeNode(node);
    		}
    	}
    	
    	return graph;
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
			distance = readProperty(properties, "d", 1);

			logNodes = readProperty(properties, "logNodes", false);
			logMatrix = readProperty(properties, "logMatrix", false);
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false); 
			logCliques = readProperty(properties, "logCliques", false);
			logDistributionOfCliques = readProperty(properties, "logDistributionOfCliques", false); 
			logCoalitionResource = readProperty(properties, "logCoalitionResource", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			
			String deleteStr = properties.getProperty("delete");
			if(deleteStr == null) deleteStr = "";
			deleteStr = deleteStr.replaceAll(" ", "");
			String deleteElements[] = deleteStr.split("\\],\\[");
			for(String deleteElement : deleteElements) {
				if(deleteElement.isEmpty()) continue;
				if(deleteElement.indexOf('[') != 0) deleteElement = "[" + deleteElement;
				if(deleteElement.lastIndexOf(']') != deleteElement.length() - 1) deleteElement = deleteElement + "]";

				deletes.add(new Partition(deleteElement));
			}
			deleteHead = readProperty(properties, "deleteHead", false);
			deletAllCliquesOfSize = readProperty(properties, "deletAllCliquesOfSize", 0);
			numberOfCliquesToDelete = readProperty(properties, "numberOfCliquesToDelete", 0);
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
