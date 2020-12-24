package gory.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;
import lombok.Cleanup;

public class Experiment1 extends BaseExperiment {
	private static final String PROEPRTIES_FILE_NAME = "experiment1.properties";
	
	private int distance; // d
	private double realDistance; // dga
	private int numberOfDigits; // m
	private int start;
	
	private boolean removeHead;
	private boolean onlyDirectDescendants;
	private boolean onlyLeft, onlyRight;
	private boolean familyMxmPlusOne;
	private boolean extendedFamily;
	private int extendedFamilyDelta;
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
	private boolean logDensityAdjacentMatrixOnlyConnected;
	private boolean logDensityAdjacentMatrixForGraphOfCliques;
	private boolean logNodesByCliques;
	private boolean logCharacteristicPathLength;
	private boolean logAverageEfficiency;
	private boolean logEnergy;
	private boolean logCheegerConstant;
	private boolean logGlobalOverlapping;
	private boolean logAverageRank;
	private boolean displayGraph;
	private boolean displayGraphOfCliques;
	private boolean saveGraphInDotFormat;
	private boolean saveGraphOfCliquesInDotFormat;
	private Partition head;
	private boolean orderSummonds;
	private double standardDeviation;
	private int geneAccuracy;
	private boolean saveResultAsCreate;
	private Map<Partition, Partition> replace = new HashMap<>();
	private Set<Partition> create = new HashSet<>(); 
	private Set<Partition> insert = new HashSet<>(); 
	private Set<Partition> delete = new HashSet<>(); 
	private Set<Integer> prohibitedOddness = new HashSet<>();
	
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 1");
    	logger.writeLine("");
    	
    	readParameters();
		
    	Graph graph = null;
    	if(!create.isEmpty()) {
    		numberOfDigits = (int) create.iterator().next().getNumberOfDigits();
    		graph = new Graph(numberOfDigits*numberOfDigits+" - "+numberOfDigits+" graph", realDistance);
    		graph.getProhibitedOddness().addAll(prohibitedOddness);
    		
	    	for(Partition partition : create) {
	    		partition.applyNormalDistribution(standardDeviation, geneAccuracy);
	    		graph.addNode(new PartitionNode(partition, null, orderSummonds));
	    	}
    	} else { 
			PartitionNode headNode;
			if(head == null) {
				List<Double> summands = new ArrayList<>();
		    	for(int i=start; i<=start+2*numberOfDigits-1; i=i+2) {
		    		summands.add(1.0 * i);
		    	}
		    	
		    	headNode = new PartitionNode(new Partition(summands));
			} else {
		    	headNode = new PartitionNode(head);
			}
			int sumOfDigits = (int) headNode.getPartition().getSumOfDigits();
			
    		graph = new Graph(sumOfDigits+" - "+numberOfDigits+" graph", realDistance);
       		graph.getProhibitedOddness().addAll(prohibitedOddness);
       	    		
	    	graph.addNode(headNode);
	    	
	    	List<Partition> partitions = PartitionBuilder.build(sumOfDigits, numberOfDigits);
	    	if(extendedFamily && extendedFamilyDelta>0) {
	    		partitions.addAll(PartitionBuilder.build(numberOfDigits*numberOfDigits-extendedFamilyDelta, numberOfDigits));
	    		partitions.addAll(PartitionBuilder.build(numberOfDigits*numberOfDigits+extendedFamilyDelta, numberOfDigits));
	    	}
	    	Random random = new Random();
	    	for(Partition partition : partitions) {
	    		if(familyMxmPlusOne) {
		    		if(partition.getAt(numberOfDigits) == 0) {
		    			partition.setAt(numberOfDigits, 1);
		    		}
	    		}
	    		
	    		double d = headNode.distanceTo(new PartitionNode(partition));
	    		if(d <= 0 || d > distance) {
	    			continue;
	    		}
	    		
	    		if(!onlyDirectDescendants && !onlyLeft && !onlyRight
	    				|| onlyDirectDescendants && partition.getAt(1) == headNode.getPartition().getAt(1) 
	    				|| onlyLeft && partition.getAt(1) - 1 == headNode.getPartition().getAt(1) 
	    				|| onlyRight && partition.getAt(1) + 1 == headNode.getPartition().getAt(1)) {

		    		partition.applyNormalDistribution(standardDeviation, geneAccuracy);
		    		partition.setW(-1.0 + 2.0 * random.nextInt(partitions.size()+1)/partitions.size());
	    			graph.addNode(new PartitionNode(partition, partition.getW()));
	    		}
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
		    	
    		if(!insert.isEmpty()) {
		    	for(Partition partition : insert) {
		    		graph.addNode(new PartitionNode(partition));
		    	}
			}
    	}
    		
    	Set<Graph> cliques = null;
    	if(logCliques || logDistributionOfCliques || logNodesByCliques || !deleteCliques.isEmpty() || logCliquesMatrices || logGlobalOverlapping
    			|| displayGraphOfCliques || logDensityAdjacentMatrixForGraphOfCliques) {
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
    	
    	if(!delete.isEmpty()) {
	    	for(Partition p : delete) {
	    		graph.removeNode(new PartitionNode(p));
	    	}
			cliques = graph.getCliques();
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

    	if(logDensityAdjacentMatrixOnlyConnected) {
    		logDensityAdjacentMatrixOnlyConnected(graph, logger);
    	}

    	if(logDensityAdjacentMatrixForGraphOfCliques) {
			Graph graphOfCliques = buildGraphOfCliques(cliques, "Graph of only connected cliques", distance, true);
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
    	
    	if(saveResultAsCreate) {
    		String create = graph.getNodes().stream().map(n -> ((PartitionNode) n).getPartition().toString()).collect(Collectors.joining(","));

    		@Cleanup
    		BufferedReader reader = new BufferedReader(new FileReader(PROEPRTIES_FILE_NAME));
    		List<String> lines = new ArrayList<>();
    		boolean found = false;
    		String line = reader.readLine();
    		while(line != null) {
    			if(line.indexOf("create=")>=0) {
    				line = "create="+create;
    				found = true;
    			}
    			lines.add(line);
    			
    			line = reader.readLine();
    		}
    		
    		if(!found) {
				line = "create="+create;
    			lines.add(line);
    		}

    		File file = new File(PROEPRTIES_FILE_NAME);
    		file.delete();
    		
    		@Cleanup
    		BufferedWriter writer = new BufferedWriter(new FileWriter(PROEPRTIES_FILE_NAME));
    		for(String l : lines) {
    			writer.write(l+System.lineSeparator());
    		}
    	}
	}	
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream(PROEPRTIES_FILE_NAME);

			// load a properties file
			Properties properties = new Properties();
			properties.load(input);

			numberOfDigits = readProperty(properties, "m", 4);
			distance = readProperty(properties, "d", 1);
			realDistance = readProperty(properties, "dga", 1.0);
			start = readProperty(properties, "start", 1);

			removeHead = readProperty(properties, "removeHead", false);
			onlyDirectDescendants = readProperty(properties, "onlyDirectDescendants", false);
			onlyLeft = readProperty(properties, "onlyLeft", false);
			onlyRight = readProperty(properties, "onlyRight", false);
			familyMxmPlusOne = readProperty(properties, "familyMxmPlusOne", false);
			extendedFamily = readProperty(properties, "extendedFamily", false);
			extendedFamilyDelta = readProperty(properties, "extendedFamilyDelta", 1);

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
			logDensityAdjacentMatrixOnlyConnected = readProperty(properties, "logDensityAdjacentMatrixOnlyConnected", false);
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
			orderSummonds = readProperty(properties, "orderSummonds", true);
			standardDeviation = readProperty(properties, "standardDeviation", 0.0);
			geneAccuracy = readProperty(properties, "geneAccuracy", 2);
			saveResultAsCreate = readProperty(properties, "saveResultAsCreate", false);
			
			create = parseListOfPartitions(properties.getProperty("create"), orderSummonds);
			insert = parseListOfPartitions(properties.getProperty("insert"), orderSummonds);
			delete = parseListOfPartitions(properties.getProperty("delete"), orderSummonds);
			
			String deleteCliquesStr = properties.getProperty("deleteCliques");
				if(deleteCliquesStr != null) {
				for(String cliqueName : deleteCliquesStr.split(",")) {
					cliqueName = cliqueName.trim();
					if(cliqueName.isEmpty()) continue;
					
					deleteCliques.add(cliqueName);
				}
			}
			
			String po = properties.getProperty("prohibitedOddness");
			if(po != null) {
				for(String part : po.split(",")) {
					try {
						prohibitedOddness.add(Integer.valueOf(part.trim()));
					} catch(Exception e) {
						// do nothing
					}
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
