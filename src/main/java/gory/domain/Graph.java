package gory.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.graphstream.graph.implementations.SingleGraph;

//import gory.algorithm.BronKerbosch;
import gory.algorithm.BronKerbosch2;
import gory.algorithm.Dijkstra;
import lombok.Getter;
import lombok.Setter;

public class Graph extends Node {
	@Getter @Setter
	private String name;

	@Getter
	private int connectionDistance = 1;

	@Getter @Setter
	private Integer id;
	
	@Getter
	private Set<INode> nodes = new HashSet<>();

	public Graph(String name) {
		this.name = name;
	}

	public Graph(String name, int connectionDistance) {
		this.name = name;
		this.connectionDistance = connectionDistance;
	}
	
	public Graph(Graph graph) {
		this(graph.getName(), graph.getConnectionDistance());
		
		for(INode node : graph.getNodes()) {
			this.addNode(node);
		}
	}
	
	@Override
	public String toString() {
		return name;
		//return nodes.toString();
	}
	
	@Override
	public INode clone(INode node) {
		if(node instanceof Graph) {
			return new Graph((Graph) node);
		} else {
			return null;
		}
	}

	@Override
	public int distanceTo(INode node) {
		if(node instanceof Graph) {
			return distanceTo((Graph) node);
		} else {
			return -1;
		}
	}
	
	public int getSize() {
		return nodes.size();
	}
	
	/*
	 * n is zero based
	 */
	public INode getNode(int n) {
		int i=0;
		Iterator<INode> iterator = nodes.iterator();
		while(iterator.hasNext()) {
			INode node = iterator.next();
			if(i == n) {
				return node;
			}
			i++;
		}
		return null;
	}
	
	public boolean addNode(INode newNode) {
		if(nodes.contains(newNode)) return false;
		
		for(INode node : nodes) {
			int distance = node.distanceTo(newNode);
			if(distance >= 0 && distance <= connectionDistance) {
				node.connect(newNode);
			}
		}
		
		nodes.add(newNode);
		
		return true;
	}
	
	public void removeNode(INode nodeToRemove) {
		nodes.remove(nodeToRemove);
		
		for(INode node : nodes) {
			node.getConnectedNodes().remove(nodeToRemove);
		}
	}
	
	public void replaceNode(INode oldNode, INode newNode) {
		removeNode(oldNode);
		addNode(newNode);
	}
	
	public int distanceTo(Graph graph) {
		int intersection = 0;
		
		for(INode node : graph.getNodes()) {
			if(getNodes().contains(node)) {
				intersection++;
			}
		}

		boolean foundConnectedNodes = false;
		if(intersection == 0) {
			for(INode node1 : graph.getNodes()) {
				for(INode node2 : getNodes()) {
					if(node1.isConnectedTo(node2)) {
						foundConnectedNodes = true;
						break;
					}
				}	
				if(foundConnectedNodes) break;
			}			
		}
		
		return intersection <= 0 && !foundConnectedNodes ? -1 : intersection;
	}
	
	public Set<Graph> getCliques() {
		BronKerbosch2 algorithm = new BronKerbosch2();
		
		Set<Graph> cliques = algorithm.findMaxCliques(this);
	
		Map<Integer, Integer> countBySize = new HashMap<>();
		for(Graph clique : cliques) {
			int size = clique.getSize();
			Integer count = countBySize.get(size);
			if(count == null) count = 0;
			countBySize.put(size, ++count);
			
			clique.setName("C"+size+"-"+count);
		}
		
		return cliques;
	}

	public int getDiameter() {
		Dijkstra algorithm = new Dijkstra();
		return algorithm.getDiameter(this);
	}

	public double getDensityAdjacentMatrix() {
		double dam = 0;
		for(INode node : nodes) {
			dam += node.getConnectedNodes().size();
		}
		
		dam /= (getSize() * (getSize() - 1));
		
		return dam;
	}
	
	public double getClusteringCoefficientUsingTriangles() {
		double total = 0.0;
        for (INode node : getNodes()) {
        	total += node.getClusteringCoefficientUsingTriangles();

        }

        return total / getSize();
	}
	
 	public double getClusteringCoefficientUsingMatrix() {
		List<INode> nodes = new ArrayList<>(getNodes());
 		boolean[][] matrix = new boolean[nodes.size()][nodes.size()];

		for(int i=0; i<getSize(); i++) {
			for(int j=0; j<getSize(); j++) {
				matrix[i][j] = nodes.get(i).isConnectedTo(nodes.get(j));
			}
		}
 		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		AtomicInteger Ntr = new AtomicInteger();
		int[] ntrs = new int[nodes.size()];
		for(int i=0; i<nodes.size(); i++) {
			final int iFinal = i;
			executor.submit(new Callable<Void>() {
				public Void call() {
					for(int j=iFinal+1; j<nodes.size(); j++) {
						int jFinal = j;
						for(int k=j+1; k<nodes.size(); k++) {
							int kFinal = k;
							ntrs[iFinal] += (matrix[iFinal][jFinal] ? 
											matrix[iFinal][kFinal] ? 
												matrix[jFinal][kFinal] ? 1 : 0
											: 0
									: 0);
				        }
			        }
					Ntr.updateAndGet(x -> x +ntrs[iFinal]);
					
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

		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		AtomicInteger N3 = new AtomicInteger();
		int[] n3s = new int[nodes.size()];
		for(int i=0; i<nodes.size(); i++) {
			final int iFinal = i;
			executor.submit(new Callable<Void>() {
				public Void call() {
					for(int j=iFinal+1; j<nodes.size(); j++) {
						int jFinal = j;
						for(int k=j+1; k<nodes.size(); k++) {
							int kFinal = k;
							n3s[iFinal] += ((matrix[iFinal][jFinal] ? 1 : 0)*(matrix[iFinal][kFinal] ? 1 : 0) +
									(matrix[jFinal][iFinal] ? 1 : 0)*(matrix[jFinal][kFinal] ? 1 : 0) +
									(matrix[kFinal][iFinal] ? 1 : 0)*(matrix[kFinal][jFinal] ? 1 : 0));
						}
			        }
					N3.updateAndGet(x -> x + n3s[iFinal]);
					
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

		return N3.get() > 0 ? 3.0 * Ntr.get() / N3.get() : 0;
 	} 

	public Map<Integer, AtomicInteger> getNodeDegreeCount() {
		Map<Integer, AtomicInteger> nodeDegreeCount = new TreeMap<>();
		for(INode node : nodes) {
			int degree = node.getDegree();
			
			AtomicInteger degreeCnt = nodeDegreeCount.get(degree);
			if(degreeCnt == null) {
				degreeCnt = new AtomicInteger();
				nodeDegreeCount.put(degree, degreeCnt);
			}
			degreeCnt.incrementAndGet();
		}
		
		return nodeDegreeCount;
	}

	public Map<Integer, Double> getNodeDegreeDistribution() {
		return getNodeDegreeDistribution(getNodeDegreeCount());
	}
	
	public Map<Integer, Double> getNodeDegreeDistribution(Map<Integer, AtomicInteger> nodeDegreeCount) {
		Map<Integer, Double> nodDegreeDistribution = new TreeMap<>();
		for(int degree : nodeDegreeCount.keySet()) {
			nodDegreeDistribution.put(degree, 1.0*nodeDegreeCount.get(degree).intValue()/getSize());
		}
		
		return nodDegreeDistribution;
	}
	
	public int getCoalitionResource() {
		int coalitionResource = 0;
		for(INode node : nodes) {
			int sumOfSummonds = 0;
			if(node instanceof PartitionNode) {
				for(int summond : ((PartitionNode) node).getSummands()) {
					sumOfSummonds += summond;
				}
			}

			int degree = node.getDegree();
			coalitionResource += degree * sumOfSummonds;
		}
		
		return coalitionResource;
	}
	
	public int getSumOfDegrees() {
		int sumOfDegrees = 0;
		for(INode node : nodes) {
			int degree = node.getDegree();
			sumOfDegrees += degree;
		}
		return sumOfDegrees;
	}

	public void deleteAllCliquesOfSize(int size) {	    		
		Set<Graph> cliques = getCliques();
	
		for(Graph clique : cliques) {
			if(clique.getSize() != size) continue;
	
			Set<INode> nodes = clique.getNodes();
			for(INode node : nodes) {
				removeNode(node);
			}
		}
	}

	public void deleteNumberOfCliques(int number) {
		List<Graph> cliques = new ArrayList<>(getCliques());

		int iteration = 0;
		Random random = new Random();
		while(iteration < number && !cliques.isEmpty()) {
			int n = random.nextInt(cliques.size());
			Graph clique = cliques.get(n);

			Set<INode> nodes = clique.getNodes();
			for(INode node : nodes) {
				removeNode(node);
			}

			cliques.remove(n);
			
			iteration++;
		}
	}

	public org.graphstream.graph.Graph asGsGraph() {
		org.graphstream.graph.Graph gsGraph = new SingleGraph("");

		// add nodes
		for(INode node : getNodes()) {
			String nodeId = node.toString();
			org.graphstream.graph.Node gsNode = gsGraph.addNode(nodeId);
			gsNode.addAttribute("ui.label", nodeId);
		}

		// add edges
		for(INode node : getNodes()) {
			for(INode connectedNode : node.getConnectedNodes()) {
				if(gsGraph.getEdge(connectedNode.toString()+" - "+node.toString()) != null) {
					continue;
				}
				
				gsGraph.addEdge(node.toString()+" - "+connectedNode.toString(), node.toString(), connectedNode.toString());
			}
		}
		
		gsGraph.addAttribute("ui.stylesheet", "node {" +
	    	    "	fill-color: green;" +
	            "	text-color: red;" +
	            "	text-size: 15;" +
	            "}");
		gsGraph.addAttribute("ui.quality");
		gsGraph.addAttribute("ui.antialias");

		return gsGraph;
	}
}
