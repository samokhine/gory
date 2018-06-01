package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment1 extends BaseExperiment {
	private int distance; // d
	private int numberOfDigits; // m
	
	private boolean removeHead;
	private boolean onlyDirectDescendants;
	private boolean onlyLeft, onlyRight;
	private boolean familyMxmPlusOne;
	private Set<String> deleteCliques = new HashSet<>();
	
	private boolean logNodes;
	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logClusteringCoefficientForHead;
	private boolean logStatsOfDegrees; 
	private boolean logCliques;
	private boolean logDistributionOfCliques;
	private boolean logCliquesMatrices;
	private boolean logCoalitionResource;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logNodesByCliques;
	private boolean displayGraph;
	private boolean displayGraphOfCliques;
	private Partition head;
	private Map<Partition, Partition> replace = new HashMap<>();
	private Set<Partition> insert = new HashSet<>(); 
	private Set<Partition> delete = new HashSet<>(); 
	
	public void run() throws IOException {
    	OutputLogger logger = new OutputLogger("output.txt");
    	logger.writeLine("Running experiment 1");
    	logger.writeLine("");
    	
    	readParameters();
		
    	Graph graph = new Graph(numberOfDigits*numberOfDigits+" - "+numberOfDigits+" graph", distance);
    	if(insert.isEmpty()) {
    		PartitionNode headNode;
    		if(head == null) {
				List<Integer> summands = new ArrayList<>();
		    	for(int i=1; i<=2*numberOfDigits-1; i=i+2) {
		    		summands.add(i);
		    	}
		    	
		    	headNode = new PartitionNode(new Partition(summands));
    		} else {
		    	headNode = new PartitionNode(head);
    		}
    		
	    	graph.addNode(headNode);
	    	
	    	List<Partition> partitions = PartitionBuilder.build(numberOfDigits*numberOfDigits, numberOfDigits);
	    	for(Partition partition : partitions) {
	    		if(familyMxmPlusOne) {
		    		if(partition.getAt(numberOfDigits) == 0) {
		    			partition.setAt(numberOfDigits, 1);
		    		}
	    		}
	    		
	    		int d = headNode.distanceTo(new PartitionNode(partition));
	    		if(d <= 0 || d > distance) {
	    			continue;
	    		}
	    		
	    		if(onlyDirectDescendants && partition.getAt(1) != headNode.getPartition().getAt(1)) {
	    			continue;
	    		}
	    		if(onlyLeft && partition.getAt(1) - 1 != headNode.getPartition().getAt(1)) {
	    			continue;
	    		}
	    		if(onlyRight && partition.getAt(1) + 1 != headNode.getPartition().getAt(1)) {
	    			continue;
	    		}
	    		
	    		graph.addNode(new PartitionNode(partition));
	    	}
	    	
	    	for(Partition oldPartition : replace.keySet()) {
	    		Partition newPartition = replace.get(oldPartition);
	    		graph.replaceNode(new PartitionNode(oldPartition), new PartitionNode(newPartition));
	    	}

	    	if(removeHead) {
	    		graph.removeNode(headNode);
	    	} else {
	        	if(logClusteringCoefficientForHead) {
	    	    	logger.writeLine("Clustering coefficient for head of the family:");
	    	    	logger.writeLine(""+headNode.getClusteringCoefficientUsingTriangles());
	    	    	logger.writeLine("");
	        	}
	    	}
	    	
    	} else {
	    	for(Partition partition : insert) {
	    		graph.addNode(new PartitionNode(partition));
	    	}
    	}
    	
    	Set<Graph> cliques = null;
    	if(logCliques || logDistributionOfCliques || logNodesByCliques || !deleteCliques.isEmpty() || logCliquesMatrices || displayGraphOfCliques) {
    		cliques = graph.getCliques();
    	}
    	
    	if(!deleteCliques.isEmpty()) {
    		for(Graph clique : cliques) {
    			if(!deleteCliques.contains(clique.getName())) continue;
    			
    			for(INode node : clique.getNodes()) {
    				graph.removeNode(node);
    			}
    		}
    		cliques = graph.getCliques();
    	}
    	
    	for(Partition p : delete) {
    		graph.removeNode(new PartitionNode(p));
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
		
		if(logCliques) {
			logCliques(graph, cliques, logger);
		}
	
		if(logDistributionOfCliques) {
			logDistributionOfCliques(cliques, logger);
		}
		
		if(logNodesByCliques) {
			logNodesByCliques(graph, cliques, logger);
		}
    	
		if(logCliquesMatrices) {
			logCliquesMatrices(cliques, logger);
		}
		
    	if(logDiameter) {
    		logDiameter(graph, logger);
    	}
    	
    	if(logDensityAdjacentMatrix) {
    		logDensityAdjacentMatrix(graph, logger);
    	}
    	
    	if(displayGraph) {
    		displayGraph(graph, "graph");
    	}

    	if(displayGraphOfCliques) {
    		displayGraph(buildGraphOfCliques(cliques, "Graph of cliques"), "graphOfCliques");
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

			numberOfDigits = readProperty(properties, "m", 4);
			distance = readProperty(properties, "d", 1);

			removeHead = readProperty(properties, "removeHead", false);
			onlyDirectDescendants = readProperty(properties, "onlyDirectDescendants", false);
			onlyLeft = readProperty(properties, "onlyLeft", false);
			onlyRight = readProperty(properties, "onlyRight", false);
			familyMxmPlusOne = readProperty(properties, "familyMxmPlusOne", false);

			logNodes = readProperty(properties, "logNodes", false);
			logMatrix = readProperty(properties, "logMatrix", false);
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logClusteringCoefficientForHead = readProperty(properties, "logClusteringCoefficientForHead", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false); 
			logCliques = readProperty(properties, "logCliques", false);
			logDistributionOfCliques = readProperty(properties, "logDistributionOfCliques", false); 
			logCliquesMatrices = readProperty(properties, "logCliquesMatrices", false);
			logCoalitionResource = readProperty(properties, "logCoalitionResource", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			logNodesByCliques = readProperty(properties, "logNodesByCliques", false);
			displayGraph = readProperty(properties, "displayGraph", false);
			displayGraphOfCliques = readProperty(properties, "displayGraphOfCliques", false);
			
			String replaceStr = properties.getProperty("replace");
			if(replaceStr == null) replaceStr = "";
			replaceStr = replaceStr.replaceAll(" ", "");
			String replaceElements[] = replaceStr.split("\\],\\[");
			for(String replaceElement : replaceElements) {
				if(replaceElement.isEmpty()) continue;
				if(replaceElement.indexOf('[') != 0) replaceElement = "[" + replaceElement;
				if(replaceElement.lastIndexOf(']') != replaceElement.length() - 1) replaceElement = replaceElement + "]";
					
				String arr[] = replaceElement.split("\\=\\>");
				if(arr.length != 2) continue;
				
				replace.put(new Partition(arr[0]), new Partition(arr[1]));
			}

			head = parsePartition(properties.getProperty("head"));
			insert = parseListOfPartitions(properties.getProperty("insert"));
			delete = parseListOfPartitions(properties.getProperty("delete"));
			
			String deleteCliquesStr = properties.getProperty("deleteCliques");
				if(deleteCliquesStr != null) {
				for(String cliqueName : deleteCliquesStr.split(",")) {
					cliqueName = cliqueName.trim();
					if(cliqueName.isEmpty()) continue;
					
					deleteCliques.add(cliqueName);
				}
			}
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
