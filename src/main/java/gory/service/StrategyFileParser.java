package gory.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gory.domain.Graph;
import gory.domain.Node;
import gory.domain.Partition;

public class StrategyFileParser {
	static public List<Graph> parse(File file, int connectionDistance) throws IOException {
		List<Graph> graphs = new ArrayList<>();
		
		Graph attacker = new Graph("Attacker", connectionDistance);
		Graph defender = new Graph("Defender", connectionDistance);
		
    	Graph current = null;
		BufferedReader br = new BufferedReader(new FileReader(file));
	    String line;
	    while ((line = br.readLine()) != null) {
	    	line = line.toLowerCase().trim();
	    	if(line.isEmpty()) continue;
	    	
	    	if(line.startsWith(attacker.getName().toLowerCase())) {
	    		current = attacker;
	    		continue;
	    	} else if(line.startsWith(defender.getName().toLowerCase())) {
	    		current = defender;
	    		continue;
	    	}
	    	if(current == null) continue;
	    	
	    	Partition partition = parseLine(line);
	    	Node node = new Node(partition);
	    	
	    	current.addNode(node);
	    }
	    br.close();
	    
	    graphs.add(attacker);
	    graphs.add(defender);
		
		return graphs;
	}
	
	static private Partition parseLine(String line) {
		while(line.indexOf("  ")>=0) {
			line = line.replaceAll("  ", " ");
		}
		
		String[] parts = line.split(" ");
		
		List<Integer> summands = new ArrayList<>();
		for(int i=3; i<parts.length-1; i++) {
			summands.add(Integer.valueOf(parts[i]));
		}
		
		Partition partition = new Partition(summands);
		
		return partition;
	}
}
