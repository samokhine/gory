package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.SimpleNode;
import gory.service.OutputLogger;

public class Experiment5 extends BaseExperiment {
	private int cliqueSize;
	private int numSteps;
	//private int numberOfNodesToConnectOnEachStep;
	private double probabilityToConnect, probabilityToMerge;
	private int maxDegree;

	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logStatsOfDegrees; 
	private boolean logCoalitionResource;
	private boolean logDiameter;
	private boolean logDensityAdjacentMatrix;
	private boolean logCharacteristicPathLength;
	private boolean logAverageEfficiency;
	private boolean logEnergy;
	private boolean logCheegerConstant;
	private boolean logCliques;
	private boolean logCliquesMatrices;
	private boolean logDistributionOfCliques;
	private boolean logDensityAdjacentMatrixForGraphOfCliques;
	private boolean logNodesByCliques;
	private boolean displayGraphOfCliques;
	private boolean saveGraphOfCliquesInDotFormat;
	private boolean displayGraph;
	private boolean saveGraphInDotFormat;
	
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 5");
    	logger.writeLine("");
    	
    	readParameters();

    	if(numSteps <= 0) {
        	logger.writeLine("numSteps has to be > 0");
        	return;
    	}

    	if(cliqueSize <= 1) {
        	logger.writeLine("cliqueSize has to be > 1");
        	return;
    	}
    	
    	if(maxDegree < cliqueSize) {
        	logger.writeLine("maxDegree has to be >= cliqueSize");
        	return;
    	}
    	
    	Random rand = new Random();
    	
    	List<INode> oldNodesToConnect = new ArrayList<>();
    	int conectionDistance = -1;
		Graph graph = new Graph("graph", conectionDistance);
    	for(int step=0; step<=numSteps; step++) {
	    	logger.writeLine("Running step #" + step);
	    	logger.writeLine("");
    		
	    	// create new nodes
    		List<INode> newNodes = new ArrayList<>();
    		for(int i=1; i<=cliqueSize; i++) {
    			INode node = new SimpleNode(""+step+"_"+i);
    			newNodes.add(node);
    			
    			graph.addNode(node);
    		}
    		
    		// connect them between each other 
    		for(int i=0; i<newNodes.size(); i++) {
        		for(int j=1; j<newNodes.size(); j++) {
        			if(i == j) continue;
        			
        			newNodes.get(i).connect(newNodes.get(j));
        		}
    		}
    		
			Set<INode> newNodesMerged = new HashSet<>();
    		if(step>0) {
    			for(INode newNode : newNodes) {
    				Iterator<INode> oldNodes = oldNodesToConnect.iterator();
    				while(oldNodes.hasNext()) {
    					INode oldNode = oldNodes.next();
    					
    					double p = rand.nextInt(100)/100.0;
    					if(p>=0 && p<probabilityToConnect && oldNode.getDegree()<maxDegree && newNode.getDegree()<maxDegree) {
        					newNode.connect(oldNode);

        					if(newNode.getDegree()>=maxDegree) {
        						break;
        					}

        					if(oldNode.getDegree()>=maxDegree) {
        						oldNodes.remove();
        					}
    					} else if(p>=probabilityToConnect && p<probabilityToConnect+probabilityToMerge && (newNode.getDegree()+oldNode.getDegree())<=maxDegree) {
    						newNodesMerged.add(newNode);
    						oldNode.getConnectedNodes().addAll(newNode.getConnectedNodes());

    						if(oldNode.getDegree()>=maxDegree) {
        						oldNodes.remove();
        					}
    						graph.removeNode(newNode);
    						break;
    					}
    				}
    			}
    		}
    		
    		for(INode newNode : newNodes) {
    			if(newNodesMerged.contains(newNode) || newNode.getDegree()>=maxDegree) continue;
    			
    			oldNodesToConnect.add(newNode);
    		}
    		
        	Set<Graph> cliques = null;
        	if(logCliques || logDistributionOfCliques || logCliquesMatrices || logDensityAdjacentMatrixForGraphOfCliques 
        			|| logNodesByCliques || displayGraphOfCliques || saveGraphOfCliquesInDotFormat) {
        		cliques = graph.getCliques();
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
    		
        	if(logDiameter) {
        		logDiameter(graph, logger);
        	}
        	
        	if(logDensityAdjacentMatrix) {
        		logDensityAdjacentMatrix(graph, logger);
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

        	if(logDensityAdjacentMatrixForGraphOfCliques || displayGraphOfCliques || saveGraphOfCliquesInDotFormat) {
        		Graph graphOfCliques = buildGraphOfCliques(cliques, "Graph of cliques");

            	if(logDensityAdjacentMatrixForGraphOfCliques) {
            		logDensityAdjacentMatrix(graphOfCliques, logger);
            	}

        		if(displayGraphOfCliques) {
        			displayGraph(graphOfCliques, "graphOfCliques");
        		}
        		
        		if(saveGraphOfCliquesInDotFormat) {
        	   		saveInDotFormat(graphOfCliques, "graphOfCliques");
        		}
        	}
        	
        	if(displayGraph) {
        		displayGraph(graph, "graph-"+step);
        	}
        	
        	if(saveGraphInDotFormat) {
        		saveInDotFormat(graph, "graph-"+step);
        	}

    	}
	}
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream("experiment5.properties");

			// load a properties file
			Properties properties = new Properties();
			properties.load(input);

			cliqueSize = readProperty(properties, "cliqueSize", 3);
			numSteps = readProperty(properties, "numSteps", 5);
			probabilityToConnect = readProperty(properties, "probabilityToConnect", 0.0);
			probabilityToMerge = readProperty(properties, "probabilityToMerge", 0.0);

			maxDegree = readProperty(properties, "maxDegree", 3);

			logMatrix = readProperty(properties, "logMatrix", false);
			logClusteringCoefficient = readProperty(properties, "logClusteringCoefficient", false);
			logStatsOfDegrees = readProperty(properties, "logStatsOfDegrees", false); 
			logCoalitionResource = readProperty(properties, "logCoalitionResource", false);
			logDiameter = readProperty(properties, "logDiameter", false);
			logDensityAdjacentMatrix = readProperty(properties, "logDensityAdjacentMatrix", false);
			logCharacteristicPathLength = readProperty(properties, "logCharacteristicPathLength", false);
			logAverageEfficiency = readProperty(properties, "logAverageEfficiency", false);
			logEnergy = readProperty(properties, "logEnergy", false);
			logCheegerConstant = readProperty(properties, "logCheegerConstant", false);
			
			logCliques = readProperty(properties, "logCliques", false);
			logDistributionOfCliques = readProperty(properties, "logDistributionOfCliques", false); 
			logCliquesMatrices = readProperty(properties, "logCliquesMatrices", false);
			logDensityAdjacentMatrixForGraphOfCliques = readProperty(properties, "logDensityAdjacentMatrixForGraphOfCliques", false);
			logNodesByCliques = readProperty(properties, "logNodesByCliques", false);

			displayGraphOfCliques = readProperty(properties, "displayGraphOfCliques", false);
			saveGraphOfCliquesInDotFormat = readProperty(properties, "saveGraphOfCliquesInDotFormat", false);

			displayGraph = readProperty(properties, "displayGraph", false);
			saveGraphInDotFormat = readProperty(properties, "saveGraphInDotFormat", false);
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
