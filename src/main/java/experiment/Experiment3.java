package experiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class Experiment3 {
	public static void run() throws IOException {
    	InputParametersV3 in = new InputParametersV3("input.properties");
    	
    	List<Integer> summands = new ArrayList<>();
    	for(int i=1; i<=2*in.getNumberOfDigits()-1; i=i+2) {
    		summands.add(i);
    	}
    	
    	Node node = new Node(new Partition(summands));
    	Graph graph = new Graph(in.getNumberOfDigits()*in.getNumberOfDigits()+" - "+in.getNumberOfDigits()+" graph");
    	graph.addNode(node);
    	
    	for(Partition partition : PartitionBuilder.build(in.getNumberOfDigits()*in.getNumberOfDigits(), in.getNumberOfDigits())) {
    		if(node.getPartition().distanceTo(partition) != 1) continue;
    		
    		graph.addNode(new Node(partition));
    	}

    	OutputLogger out = new OutputLogger("output.txt");
    	graph.log(out);
    	out.close();
	}
}
