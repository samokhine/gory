package gory.app;

import java.io.IOException;
import java.util.List;

import gory.domain.Graph;
import gory.domain.Partition;
import gory.service.GraphBuilder;
import gory.service.OutputLogger;
import gory.service.PartitionBuilder;

public class App {
    public static void main(String[] args) throws InterruptedException, IOException {
    	InputParameters in = new InputParameters("input.properties");
    	OutputLogger out = new OutputLogger("output.txt");
    	
    	List<Partition> partitions = PartitionBuilder.build(in.getSumOfDigits(), in.getNumberOfDigits());
    	for(int i=1; i<=in.getNumberOfGraphs(); i++) {
    		Graph graph = GraphBuilder.build(in.getNumberOfGraphBuildingSteps(), partitions);
    		out.writeLine("Graph #"+i);
    		out.writeLine("");
    		graph.log(out);
    		out.writeLine("");
    		out.writeLine("");
    	}
    	out.close();
    }
}
