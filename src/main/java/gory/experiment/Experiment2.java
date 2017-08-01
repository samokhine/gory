package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment2 implements Experiment {
	private int numberOfRandomPicks; // T
	private int numberOfDigits; // m
	private int sumOfDigits; // n
	private int distance; // d
	
	public void run() throws IOException {
    	OutputLogger out = new OutputLogger("output.txt");
    	out.writeLine("Running experiment 2");
    	out.writeLine("");
    	
    	readParameters();
		
    	List<Partition> partitions = PartitionBuilder.build(sumOfDigits, numberOfDigits);
    	
    	Random generator = new Random();
    	
    	Node node = new Node(partitions.get(generator.nextInt(partitions.size())));
    	Graph graph = new Graph(sumOfDigits+" - "+numberOfDigits+" graph", distance);
    	graph.addNode(node);
    	
    	for(int pick=1; pick<=numberOfRandomPicks; pick++) {
    		node = new Node(partitions.get(generator.nextInt(partitions.size())));
        	graph.addNode(node);
    	}
    	graph.logStats(out);
    	
    	out.close();
	}
	
	private void readParameters() {
		InputStream input = null;
		try {
			input = new FileInputStream("input.properties");

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			numberOfRandomPicks = Integer.valueOf(prop.getProperty("T"));
			numberOfDigits = Integer.valueOf(prop.getProperty("m"));
			sumOfDigits = Integer.valueOf(prop.getProperty("n"));
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
