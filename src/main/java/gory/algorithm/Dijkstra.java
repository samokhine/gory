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
import gory.domain.INode;

public class Dijkstra {
	public int getDiameter(Graph graph) {
		if(graph.getSize()<=0) {
			return Integer.MAX_VALUE;
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		AtomicInteger diameter = new AtomicInteger();
		for(INode node : graph.getNodes()) {
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
	
	public int getLongestShortestDistance(Graph graph, INode start) {
		Map<INode, Integer> shortestDistances = getShortestDistances(graph, start);
		
		int maxDistance = 0;
		for(int distance : shortestDistances.values()) {
			//if(distance != Integer.MAX_VALUE && distance > maxDistance) {
			if(distance > maxDistance) {
				maxDistance = distance;
			}
		}
		
		return maxDistance;
	}
	
	public Map<INode, Integer> getShortestDistances(Graph graph, INode start) {
		Map<INode, List<INode>> shortestPaths = new HashMap<>();
		Map<INode, Integer> distances = new HashMap<>();
		for(INode node : graph.getNodes()) {
			distances.put(node, node.equals(start) ? 0 : Integer.MAX_VALUE);
			shortestPaths.put(node, new LinkedList<>());
		}
		
	    Set<INode> settledNodes = new HashSet<>();
	    Set<INode> unsettledNodes = new HashSet<>();
	 
	    unsettledNodes.add(start);
	 
	    while (unsettledNodes.size() != 0) {
	        INode currentNode = getLowestDistanceNode(unsettledNodes, distances);
	        unsettledNodes.remove(currentNode);
	        for(INode adjacentNode : currentNode.getConnectedNodes()) {
	            if(!settledNodes.contains(adjacentNode)) {
	                calculateMinimumDistance(graph, adjacentNode, currentNode, distances, shortestPaths);
	                unsettledNodes.add(adjacentNode);
	            }
	        }
	        settledNodes.add(currentNode);
	    }
		
		return distances;
	}
	
	private INode getLowestDistanceNode(Set<INode> unsettledNodes, Map<INode, Integer> distances) {
	    INode lowestDistanceNode = null;
	    int lowestDistance = Integer.MAX_VALUE;
	    for (INode node: unsettledNodes) {
	        int nodeDistance = distances.get(node);
	        if (nodeDistance < lowestDistance) {
	            lowestDistance = nodeDistance;
	            lowestDistanceNode = node;
	        }
	    }
	    return lowestDistanceNode;
	}
	
	private void calculateMinimumDistance(Graph graph, INode evaluationNode, INode sourceNode, Map<INode, Integer> distances, Map<INode, List<INode>> shortestPaths) {
        int edgeWeight = sourceNode.isConnectedTo(evaluationNode) ? 1 : 0;
	    int sourceDistance = distances.get(sourceNode);
	    int distance = sourceDistance + edgeWeight;
	    if (distance < distances.get(evaluationNode).intValue()) {
	    	distances.put(evaluationNode, distance);
	        LinkedList<INode> shortestPath = new LinkedList<>(shortestPaths.get(sourceNode));
	        shortestPath.add(sourceNode);
	        shortestPaths.put(evaluationNode, shortestPath);
	    }
	}
}
