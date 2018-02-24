package gory.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import gory.domain.Graph;
import gory.domain.Node;

public class Dijkstra {
	public int getDiameter(Graph graph) {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		AtomicInteger diameter = new AtomicInteger();
		for(Node node : graph.getNodes()) {
			executor.submit(new Callable<Void>() {
				public Void call() {
					int distance = getLongestShortestDistance(graph, node);
					diameter.updateAndGet(x -> x < distance ? distance : x);
					
					return null;
				}
			});
		}
			
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		return diameter.get();
	}
	
	public int getLongestShortestDistance(Graph graph, Node start) {
		Map<Node, Integer> shortestDistances = getShortestDistances(graph, start);
		
		int maxDistance = 0;
		for(int distance : shortestDistances.values()) {
			//if(distance != Integer.MAX_VALUE && distance > maxDistance) {
			if(distance > maxDistance) {
				maxDistance = distance;
			}
		}
		
		return maxDistance;
	}
	
	public Map<Node, Integer> getShortestDistances(Graph graph, Node start) {
		Map<Node, List<Node>> shortestPaths = new HashMap<>();
		Map<Node, Integer> distances = new HashMap<>();
		for(Node node : graph.getNodes()) {
			distances.put(node, node.equals(start) ? 0 : Integer.MAX_VALUE);
			shortestPaths.put(node, new LinkedList<>());
		}
		
	    Set<Node> settledNodes = new HashSet<>();
	    Set<Node> unsettledNodes = new HashSet<>();
	 
	    unsettledNodes.add(start);
	 
	    while (unsettledNodes.size() != 0) {
	        Node currentNode = getLowestDistanceNode(unsettledNodes, distances);
	        unsettledNodes.remove(currentNode);
	        for(Node adjacentNode : currentNode.getConnectedNodes()) {
	            if(!settledNodes.contains(adjacentNode)) {
	                calculateMinimumDistance(adjacentNode, currentNode, distances, shortestPaths);
	                unsettledNodes.add(adjacentNode);
	            }
	        }
	        settledNodes.add(currentNode);
	    }
		
		return distances;
	}
	
	private Node getLowestDistanceNode(Set<Node> unsettledNodes, Map<Node, Integer> distances) {
	    Node lowestDistanceNode = null;
	    int lowestDistance = Integer.MAX_VALUE;
	    for (Node node: unsettledNodes) {
	        int nodeDistance = distances.get(node);
	        if (nodeDistance < lowestDistance) {
	            lowestDistance = nodeDistance;
	            lowestDistanceNode = node;
	        }
	    }
	    return lowestDistanceNode;
	}
	
	private void calculateMinimumDistance(Node evaluationNode, Node sourceNode, Map<Node, Integer> distances, Map<Node, List<Node>> shortestPaths) {
        int edgeWeight = sourceNode.distanceTo(evaluationNode);
	    int sourceDistance = distances.get(sourceNode);
	    int distance = sourceDistance + edgeWeight;
	    if (distance < distances.get(evaluationNode).intValue()) {
	    	distances.put(evaluationNode, distance);
	        LinkedList<Node> shortestPath = new LinkedList<>(shortestPaths.get(sourceNode));
	        shortestPath.add(sourceNode);
	        shortestPaths.put(evaluationNode, shortestPath);
	    }
	}
}
