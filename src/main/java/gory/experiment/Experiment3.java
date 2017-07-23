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

public class Experiment3 implements Experiment {
	private int distance; // d
	private int numberOfDigits; // m
	
	public void run() throws IOException {
    	OutputLogger out = new OutputLogger("output.txt");
    	out.writeLine("Running experiment 3");
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
    	
    	out.writeLine("Clustering coefficient: "+graph.getClusteringCoefficient());
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
