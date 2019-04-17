package gory.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import gory.domain.Graph;
import gory.domain.INode;
import gory.domain.SimpleNode;
import gory.service.OutputLogger;

public class Experiment6 extends BaseExperiment {
	private static final String PROPERTIES_FILE = "experiment6.properties";
	
	private boolean displayGraph;
	private List<String> countries = new ArrayList<>();
	private List<Double> thresholds = new ArrayList<>();
	
	public void run(OutputLogger logger) throws IOException {
    	logger.writeLine("Running experiment 6");
    	logger.writeLine("");
    	
    	Properties properties = readParameters(logger);
    	
    	if(countries.isEmpty()) {
    		logger.writeLine("Did not find any countries in the input file");
    		return;
    	}

    	if(thresholds.isEmpty()) {
    		logger.writeLine("Did not find any thresholds in the input file");
    		return;
    	}

    	boolean error = false;
    	List<List<Double>> valuesByCountry = new ArrayList<>();
    	for(String country : countries) {
    		if(error) break;
    		
			List<Double> values = new ArrayList<>();
			values.addAll(readArrayOfDoublesProperty(logger, properties, country, " ", new ArrayList<>()));
			if(values.size() == thresholds.size()) {
    			valuesByCountry.add(values);
			} else {
				error = true;
				logger.writeLine("Found "+values.size()+" values for country "+country+" but expected "+thresholds.size());
			}
    	}
    	if(error) return;
    	
    	int conectionDistance = -1;
		Graph graph = new Graph("graph", conectionDistance);
		List<INode> nodes = new ArrayList<>();
		for(int i=0; i<countries.size(); i++) {
			INode node = new SimpleNode(countries.get(i));
			nodes.add(node);
			
			graph.addNode(node);
    	}

		for(int i=0; i<countries.size(); i++) {
			INode node1 = nodes.get(i);
        	List<Double> values1 = valuesByCountry.get(i);
			for(int j=i+1; j<countries.size(); j++) {
				INode node2 = nodes.get(j);
	        	List<Double> values2 = valuesByCountry.get(j);
        		for(int k=0; k<thresholds.size(); k++) {
        			double threshold = thresholds.get(k);
        			if(Math.abs(values1.get(k) - values2.get(k))<threshold) {
        				node1.connect(node2);
        				break;
        			}
        		}
        	}
    	}

    	if(displayGraph) {
    		displayGraph(graph, "graph");
    	}
	}
	
	private Properties readParameters(OutputLogger logger) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(PROPERTIES_FILE);

			// load a properties file
			properties.load(input);
			
			countries.addAll(Arrays.asList(readProperty(properties, "countries", "").split(",")));
			thresholds.addAll(readArrayOfDoublesProperty(logger, properties, "thresholds", " ", new ArrayList<>()));
			
			displayGraph = readProperty(properties, "displayGraph", false);
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
