package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment4 extends BaseExperiment {
	private int distance; // d
	private int numberOfDigits; // m
	
	private int numberOfRuns;
	private boolean deleteAnyClique;
	private int deleteAnyCliqueOfSize;
	private int diameterThreshold;
	private boolean removeHead;
	private boolean logNodes;
	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logStatsOfDegrees; 
	private boolean logCliques;
	private boolean logDistributionOfCliques;
	private boolean logCoalitionResource;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logEachStep;
	private boolean displayGraph;
	private boolean displayGraphOfCliques;
	
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 4");
    	logger.writeLine("");
    	
    	readParameters();
		
    	for(int runNum=1; runNum<=numberOfRuns; runNum++) {
    		if(numberOfRuns>1) {
	    		logger.writeLine("Run number: "+runNum);
	        	logger.writeLine("");
    		}

    		Graph graph = new Graph(numberOfDigits*numberOfDigits+" - "+numberOfDigits+" graph", distance);
	    	
			List<Integer> summands = new ArrayList<>();
	    	for(int i=1; i<=2*numberOfDigits-1; i=i+2) {
	    		summands.add(i);
	    	}
	    	PartitionNode headNode = new PartitionNode(new Partition(summands));
	    	graph.addNode(headNode);
	    	
	    	List<Partition> partitions = PartitionBuilder.build(numberOfDigits*numberOfDigits, numberOfDigits);
	    	for(Partition partition : partitions) {
	    		int d = headNode.distanceTo(new PartitionNode(partition));
	    		if(d <= 0 || d > distance) {
	    			continue;
	    		}
	
	    		graph.addNode(new PartitionNode(partition));
	    	}
	    	
	    	if(removeHead) {
	    		graph.removeNode(headNode);
	    	}
	    	
	    	List<Graph> cliques = null;
	    	Graph graphOfCliques;
	    	int stepNum = 0;
			while(true) {
				Set<Graph> cliquesSet = graph.getCliques();
				cliques = new ArrayList<>(cliquesSet);
				
				graphOfCliques = buildGraphOfCliques(cliquesSet, "Graph of cliques");
	
				int diameter = graphOfCliques.getDiameter();
				if(diameter >= diameterThreshold) {
					break;
				}
	
	    		stepNum ++;
	
	    		if(deleteAnyClique) {
	    			if(cliques.isEmpty()) break;
	
	    			int randomIndex = ThreadLocalRandom.current().nextInt(0, cliques.size());
					Graph randomClique = cliques.get(randomIndex);
					for(INode node : randomClique.getNodes()) {
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
	    				for(INode node : randomClique.getNodes()) {
	    					graph.removeNode(node);
	    				}
	    			}
	    		} else {
	    			break;
	    		}
	
	    		if(logEachStep) {
	    			doLoggingAfterStep(graph, logger, runNum, stepNum, false);
	    		}
			}
			
			doLoggingAfterStep(graph, logger, runNum, stepNum, true);
    	}
	}

	private void doLoggingAfterStep(Graph graph, OutputLogger logger, int runNum, int stepNum, boolean isLastStep) {
		logger.writeLine("Step number: "+stepNum);
		logger.writeLine("");

		List<Graph> cliques = null;
		Graph graphOfCliques = null;
		if(logCliques || logDistributionOfCliques || displayGraphOfCliques || logDiameter) {
			Set<Graph> cliquesSet = graph.getCliques();
			cliques = new ArrayList<>(cliquesSet);
			if(displayGraphOfCliques || logDiameter) {
				graphOfCliques = buildGraphOfCliques(cliquesSet, "Graph of cliques");
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
    		logDiameter(graphOfCliques, logger);
    	}
    	
    	if(logDensityAdjacentMatrix) {
    		logDensityAdjacentMatrix(graph, logger);
    	}

    	if(isLastStep) {
	    	if(displayGraph) {
	    		displayGraph(graph, "graph"+(numberOfRuns == 1 ? "" : "-"+runNum));
	    	}
		
	    	if(displayGraphOfCliques) {
	    		displayGraph(graphOfCliques, "graphOfCliques"+(numberOfRuns == 1 ? "" : "-"+runNum));
	    	}
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

			numberOfRuns = readProperty(properties, "numberOfRuns", 1);
			deleteAnyClique = readProperty(properties, "deleteAnyClique", false);
			deleteAnyCliqueOfSize = readProperty(properties, "deleteAnyCliqueOfSize", 0);
			diameterThreshold = readProperty(properties, "diameterThreshold", 0);
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
			logEachStep = readProperty(properties, "logEachStep", false);
			
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
