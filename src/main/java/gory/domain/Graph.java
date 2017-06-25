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
				newNode.connect(node);
			}
		}
		
		nodes.add(newNode);
		
		return true;
	}
	
	public void log(OutputLogger log) {
		if(nodes.isEmpty()) return;
		
		int sumOfDegrees = 0;
		log.writeLine("Nodes:");
		for(Node node : nodes) {
			int degree = node.getConnectedNodes().size();
			sumOfDegrees += degree;
			log.writeLine(node.toString()+" degree: "+degree);
		}
		log.writeLine("");

		double s2 = 0;
		double avg = 1.0 * sumOfDegrees / nodes.size();
		log.writeLine("Average: "+avg);
		for(Node node : nodes) {
			int degree = node.getConnectedNodes().size();
			s2 += (degree - avg)*(degree - avg);
		}
		s2 = nodes.size() <= 1 ? 0 : s2/(nodes.size() - 1);
		log.writeLine("Variance: "+s2);
	}
}
