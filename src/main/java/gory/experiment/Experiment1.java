package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment1 implements Experiment {
	private int distance; // d
	private int numberOfDigits; // m
	
	private boolean logNodes;
	private boolean logMatrix;
	private boolean logClusteringCoefficient;
	private boolean logClusteringCoefficientForHead;
	private boolean logStatsOfDegrees; 
	private boolean logCliques;
	
	public void run() throws IOException {
    	OutputLogger out = new OutputLogger("output.txt");
    	out.writeLine("Running experiment 1");
    	out.writeLine("");
    	
    	readParameters();
		
		List<Integer> summands = new ArrayList<>();
    	for(int i=1; i<=2*numberOfDigits-1; i=i+2) {
    		summands.add(i);
    	}
    	
    	Node node = new Node(new Partition(summands));
    	Graph graph = new Graph(numberOfDigits*numberOfDigits+" - "+numberOfDigits+" graph", distance);
    	graph.addNode(node);
    	
    	for(Partition partition : PartitionBuilder.build(numberOfDigits*numberOfDigits, numberOfDigits)) {
    		int d = node.distanceTo(partition);
    		if(d <= 0 || d > distance) continue;
    		
    		graph.addNode(new Node(partition));
    	}
    	
    	if(logNodes) {
    		graph.logNodes(out);
    	}
    	
    	if(logMatrix) {
    		graph.logMatrix(out);
    	}
    	
    	if(logClusteringCoefficient) {
    		graph.logClusteringCoefficient(out);
    	}
    	
    	if(logClusteringCoefficientForHead) {
	    	out.writeLine("Clustering coefficient for head of the family:");
	    	out.writeLine(""+node.getClusteringCoefficientUsingTriangles());
	    	out.writeLine("");
    	}
    	
    	if(logStatsOfDegrees) {
    		graph.logStatsOfDegrees(out); 
    	}
		
    	if(logCliques) {
    		graph.logCliques(out);
    	}
    	
    	out.close();
	}
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream("input.properties");

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			numberOfDigits = Integer.valueOf(prop.getProperty("m"));
			distance = Integer.valueOf(prop.getProperty("d"));
			
			logNodes = Boolean.valueOf(prop.getProperty("logNodes"));
			logMatrix = Boolean.valueOf(prop.getProperty("logMatrix"));
			logClusteringCoefficient = Boolean.valueOf(prop.getProperty("logClusteringCoefficient"));
			logClusteringCoefficientForHead = Boolean.valueOf(prop.getProperty("logClusteringCoefficientForHead"));
			logStatsOfDegrees = Boolean.valueOf(prop.getProperty("logStatsOfDegrees")); 
			logCliques = Boolean.valueOf(prop.getProperty("logCliques"));

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
