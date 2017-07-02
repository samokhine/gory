package gory.app;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gory.domain.Graph;
import gory.service.OutputLogger;
import gory.service.StrategyFileParser;

public class App {
    public static void main(String[] args) throws InterruptedException, IOException {
    	InputParametersV2 in = new InputParametersV2("input.properties");
    	OutputLogger out = new OutputLogger("output.txt");
    	
    	List<Graph> graphs = StrategyFileParser.parse(new File(in.getStrategyFileName()), in.getDistance());
    	
    	for(Graph graph : graphs) {
    		graph.findMaxCliques();
    		graph.log(out);
    	}
    	out.close();
    }
}
