package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.SimpleNode;
import gory.service.OutputLogger;

public class Experiment5 extends BaseExperiment {
	private int cliqueSize;
	private int numSteps;
	private int numberOfNodesToConnectOnEachStep;
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
    	
    	if(numberOfNodesToConnectOnEachStep<=0 || numberOfNodesToConnectOnEachStep>cliqueSize) {
        	logger.writeLine("numberOfNodesToConnectOnEachStep has to be > 0 and <= cliqueSize");
        	return;
    	}
    	
    	if(maxDegree < cliqueSize) {
        	logger.writeLine("maxDegree has to be >= cliqueSize");
        	return;
    	}
    	
    	Random rand = new Random();
    	
    	List<INode> oldNodesToConnect = new ArrayList<>();
		Graph graph = new Graph("graph", -1);
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
    		
    		if(step>0) {
    			// lets select new nodes to connect
    			List<INode> newNodesToConnect = new ArrayList<>();
        		while(newNodesToConnect.size()<numberOfNodesToConnectOnEachStep) {
        			int i = rand.nextInt(newNodes.size());
        			
        			newNodesToConnect.add(newNodes.get(i));
        			newNodes.remove(i);
        			
        			if(newNodes.isEmpty()) break; // should not happen
        		}
    			
    			// lets connect new nodes to the existing ones
        		int numRetry = 0, maxNumRetry = 2 * cliqueSize;
    			int numLinks = 0;
    			while(numLinks<cliqueSize && !oldNodesToConnect.isEmpty()) {
        			int i = rand.nextInt(newNodesToConnect.size());
    				INode newNode = newNodesToConnect.get(i);
    				
        			int j = rand.nextInt(oldNodesToConnect.size());
    				INode oldNode = oldNodesToConnect.get(j);
    				
    				if(newNode.isConnectedTo(oldNode)) {
    					if(++numRetry >= maxNumRetry) {
    						break;
    					}
    				} else {
    					newNode.connect(oldNode);
    					numLinks++;
    					
    					if(newNode.getDegree()>=maxDegree) {
    						newNodesToConnect.remove(i);
    					}

    					if(oldNode.getDegree()>=maxDegree) {
    						oldNodesToConnect.remove(j);
    					}
    				}
    			}
    		}
    		
    		for(int i=0; i<newNodes.size(); i++) {
    			oldNodesToConnect.add(newNodes.get(i));
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
			input = new FileInputStream("input.properties");

			// load a properties file
			Properties properties = new Properties();
			properties.load(input);

			cliqueSize = readProperty(properties, "cliqueSize", 3);
			numSteps = readProperty(properties, "numSteps", 5);
			numberOfNodesToConnectOnEachStep = readProperty(properties, "numberOfNodesToConnectOnEachStep", 2);
			maxDegree = readProperty(properties, "maxDegree", 2);

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
