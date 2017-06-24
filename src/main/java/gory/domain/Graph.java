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
		if(nodes.contains(newNode)) return false;
		
		for(Node node : nodes) {
			int distance = node.distanceTo(newNode);
			if(distance == CONNECTION_DISATNCE) {
				node.connect(newNode);
			}
		}
		
		nodes.add(newNode);
		
		return true;
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
