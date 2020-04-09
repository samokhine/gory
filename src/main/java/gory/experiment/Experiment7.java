package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.Partition;
import gory.domain.PartitionNode;
import gory.service.OutputLogger;

public class Experiment7 extends BaseExperiment {
	private static final String PROPERTIES_FILE = "experiment7.properties";
	
	private List<Partition> allAPartitions = new ArrayList<>();
	private List<Partition> allBPartitions = new ArrayList<>();

	private boolean logNodes;
	private boolean logClusteringCoefficient;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logCharacteristicPathLength;
	private boolean logStatsOfDegrees;
	private boolean logEnergy;
	private boolean logCliques;
	private boolean logDistributionOfCliques;
	private boolean logAverageEfficiency;
	private boolean displayGraph;

	private boolean logHammingDistance;
	private boolean logPayoutMatrix;

	private int numPartitionsToSelect;
	private int distance;
	private int numberOfRuns; 

	@Override
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 7");
    	logger.writeLine("");

    	readParameters(logger);

    	String sep = "   ";
    	
    	String[] headers = new String[] {
    		"#", 
    		"W Aj/Bj",
    		"w,l,d",
    		"W/l DAM",
    		//"Win/Lost CPL",
    		"W/l Deg"
    	};
    	String[][] rows = new String[numberOfRuns][headers.length];
    	
    	StringBuilder line = new StringBuilder();
    	for(int i=1; i<=numberOfRuns; i++) {
        	Graph graphA = buildGraph("Graph A", allAPartitions, numPartitionsToSelect);
        	Graph graphB = buildGraph("Graph B", allBPartitions, numPartitionsToSelect);

        	logger.writeLine("Run #"+i);
        	logger.writeLine("");

        	processGraph(graphA, logger);
        	processGraph(graphB, logger);
        	
        	if(logHammingDistance) {
        		logHammingDistance(graphA, graphB, logger);
        	}
        	
        	if(logPayoutMatrix) {
        		logPayoutMatrix(graphA, graphB, logger);
        	}

        	int aWins = 0, bWins = 0, draws = 0;
        	Iterator<INode> aNodesIterator =  graphA.getNodes().iterator();
        	Iterator<INode> bNodesIterator =  graphB.getNodes().iterator();
        	while(aNodesIterator.hasNext() && bNodesIterator.hasNext()) {
        		INode aNode = aNodesIterator.next();
        		INode bNode = bNodesIterator.next();
        		
        		int sum = 0;
        		for(int j=0; j<Math.min(((PartitionNode) aNode).getSummands().size(), ((PartitionNode) bNode).getSummands().size()); j++) {
        			int aSummand = ((PartitionNode) aNode).getSummands().get(j);
        			int bSummand = ((PartitionNode) bNode).getSummands().get(j);
        			
        			if(aSummand > bSummand) {
        				sum += 1;
        			} else if(aSummand < bSummand) {
        				sum -= 1;
        			}
        		}
        		if(sum > 0) {
        			aWins++;
        		} else if(sum < 0) {
        			bWins++;
        		} else {
        			draws++;
        		}
        	}
  
        	boolean aWon = aWins >= bWins;
        	
        	double aDAM = graphA.getDensityAdjacentMatrix();
        	double bDAM = graphB.getDensityAdjacentMatrix();
        	
        	//double aCPL = graphA.getCharacteristicPathLength();
        	//double bCPL = graphB.getCharacteristicPathLength();
        	
        	double aAverageDegree = graphA.getSumOfDegrees()/graphA.getSize();
        	double bAverageDegree = graphB.getSumOfDegrees()/graphB.getSize();
        	
        	line.setLength(0);

        	int j=0;
        	String cell = ""+i;
        	rows[i-1][j++] = cell;
        	
        	cell = aWon ? "A" : "B";
        	rows[i-1][j++] = cell;
        	
        	cell = (aWon ? aWins : bWins) + "/" + (aWon ? bWins : aWins) + "/" + draws;
        	rows[i-1][j++] = cell;

        	cell = ""+df1.format(aWon ? aDAM : bDAM) + "/" + df1.format(aWon ? bDAM : aDAM);
        	rows[i-1][j++] = cell;

        	//cell = ""+df4.format(aWon ? aCPL : bCPL) + "/" + df4.format(aWon ? bCPL : aCPL);
        	//line.append(StringUtils.rightPad(cell, headers[4].length()+sep.length(), " "));

        	cell = ""+df1.format(aWon ? aAverageDegree : bAverageDegree) + "/" + df1.format(aWon ? bAverageDegree : aAverageDegree);
        	rows[i-1][j++] = cell;
    	}
    	
    	line.setLength(0);
    	Arrays.asList(headers).stream().forEach(header -> {
        	line.append(header+sep);
    	});
    	logger.writeLine(line.toString());

    	
    	for(int i=1; i<=numberOfRuns; i++) {
        	line.setLength(0);
        	for(int j=1; j<=headers.length; j++) {
            	line.append(StringUtils.rightPad(rows[i-1][j-1], headers[j-1].length()+sep.length(), " "));
        	}
        	logger.writeLine(line.toString());
    	}
	}

	private Graph buildGraph(String graphName, List<Partition> allPartitions, int numPartitionsToSelect) {
		List<Partition> partitions = new ArrayList<>(allPartitions);
		
    	Random random = new Random();

    	List<Partition> selectedPartitions = new ArrayList<>();
    	while(selectedPartitions.size()<numPartitionsToSelect) {
    		int index = random.nextInt(partitions.size());
    		selectedPartitions.add(partitions.remove(index));
    	}

		Graph graph = new Graph(graphName, distance);
		
    	for(Partition partition : selectedPartitions) {
    		graph.addNode(new PartitionNode(partition));
    	}

    	return graph;
	}
	
	private Graph processGraph(Graph graph, OutputLogger logger) {
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
			
			allAPartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("aPartitions")));
			allBPartitions = new ArrayList<>(parseListOfPartitions(properties.getProperty("bPartitions")));

			distance = readProperty(properties, "distance", 1);
			
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

			numPartitionsToSelect = readProperty(properties, "numPartitionsToSelect", 10);
			numberOfRuns = readProperty(properties, "numberOfRuns", 1);

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
