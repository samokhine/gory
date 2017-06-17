package gory.domain;

import java.util.HashSet;
import java.util.Set;

import gory.service.OutputLogger;

public class Graph {
	static public final int CONNECTION_DISATNCE = 1;
	
	private Set<Node> nodes = new HashSet<>();

	@Override
	public String toString() {
		return nodes.toString();
	}
	
	public int getSize() {
		return nodes.size();
	}
	
	public boolean addNode(Node newNode) {
		boolean connected = getSize() == 0;
		
		for(Node node : nodes) {
			int distance = node.distanceTo(newNode);
			
			if(distance == 0) { // the node is already in the graph
				connected = false;
				break;
			}
			else if(distance == CONNECTION_DISATNCE) {
				connected = true;
				node.connect(newNode);
			}
		}
		
		if(connected) {
			nodes.add(newNode);
		}
		
		return connected;
	}
	
	public void log(OutputLogger log) {
		log.writeLine("Nodes:");
		for(Node node : nodes) {
			log.writeLine(node.toString());
		}
		log.writeLine("");

		log.writeLine("Matrix:");
		for(Node y : nodes) {
			StringBuilder sb = new StringBuilder();
			for(Node x : nodes) {
				sb.append(x.getConnectedNodes().contains(y) ? 1 : 0);
			}
			log.writeLine(sb.toString());
		}
	}
}
