package gory.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
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

import Jama.Matrix;

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

	@Getter
	private Set<Integer> prohibitedOddness = new HashSet<>();
	
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
			if(distance >= 0 && distance <= connectionDistance && testOddness(node, newNode)) {
				node.connect(newNode);
			}
		}
		
		nodes.add(newNode);
		
		return true;
	}
	
	protected boolean testOddness(INode node1, INode node2) {
		if(prohibitedOddness.isEmpty()) return true;
		if(!(node1 instanceof PartitionNode)) return true;
		if(!(node2 instanceof PartitionNode)) return true;
		
		if(prohibitedOddness.contains(((PartitionNode) node1).getPartition().getOddness())) return false;
		if(prohibitedOddness.contains(((PartitionNode) node2).getPartition().getOddness())) return false;
		
		return true;
	}
	
	public double getAverageRank() {
		double averageRank = 0;
		
		for(INode node : getNodes()) {
			if(!(node instanceof PartitionNode)) continue;
			
			averageRank += ((PartitionNode) node).getPartition().getRank();
		}
		averageRank /= getSize();
		
		return averageRank;
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
		boolean foundCommonNode = false;
		for(INode node : graph.getNodes()) {
			if(getNodes().contains(node)) {
				foundCommonNode = true;
				break;
			}
		}

		if(foundCommonNode) {
			return 1;
		} else {
			boolean foundConnectedNodes = false;
			for(INode node1 : graph.getNodes()) {
				for(INode node2 : getNodes()) {
					if(node1.isConnectedTo(node2)) {
						foundConnectedNodes = true;
						break;
					} else {
						int distance = node1.distanceTo(node2);
						if(distance >= 0 && distance <= connectionDistance) {
							foundConnectedNodes = true;
							break;
						}
					}
				}	
				if(foundConnectedNodes) break;
			}			
		
			return foundConnectedNodes ? 1 : -1;
		}
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

	public double getCharacteristicPathLength() {
		Dijkstra algorithm = new Dijkstra();
		
		int size = 0;
		int sumOfDistances = 0;
		for(INode nodeStart : getNodes()) {
			if(nodeStart.getConnectedNodes().isEmpty()) continue;
			
			size ++;
			
			Map<INode, Integer> shortestDistances = algorithm.getShortestDistances(this, nodeStart);
			
			for(INode nodeEnd : shortestDistances.keySet()) {
				if(nodeEnd.equals(nodeStart)) continue;
			
				int shortestDistance = shortestDistances.get(nodeEnd);
				if(shortestDistance == Integer.MAX_VALUE) {
					shortestDistance = 0;
				}

				sumOfDistances += shortestDistance;
			}
		}
		if(size <= 1) return 0;

		return 1.0 * sumOfDistances/(size * (size - 1));
	}

	public double getAverageEfficiency() {
		int size = getSize();
		if(size <= 1) return 0;
		
		Dijkstra algorithm = new Dijkstra();
		
		double sumOfDistances = 0;
		for(INode nodeStart : getNodes()) {
			Map<INode, Integer> shortestDistances = algorithm.getShortestDistances(this, nodeStart);
			
			for(INode nodeEnd : shortestDistances.keySet()) {
				if(nodeEnd.equals(nodeStart)) continue;
			
				int shortestDistance = shortestDistances.get(nodeEnd);
				if(shortestDistance == Integer.MAX_VALUE) {
					continue;
				}

				sumOfDistances += 1.0/shortestDistance;
			}
		}

		return 1.0 * sumOfDistances/(size * (size - 1));
	}

	public double getDensityAdjacentMatrix() {
		return getDensityAdjacentMatrix(false);
	}

	public double getDensityAdjacentMatrix(boolean onlyConnected) {
		int size = 0;
		double dam = 0;
		for(INode node : nodes) {
			if(onlyConnected && node.getConnectedNodes().isEmpty()) continue;
			
			size++;
			dam += node.getConnectedNodes().size();
		}
		if(size <= 1) return 0;
		
		dam /= (size * (size - 1));
		
		return dam;
	}
	
	public double getClusteringCoefficientUsingTriangles() {
		double total = 0.0;
        for (INode node : getNodes()) {
        	total += node.getClusteringCoefficientUsingTriangles();

        }

        return total / getSize();
	}
	
	public double getCheegerConstant() {
 		double[][] a = getAdjacencyMatrix();
		Matrix am = new Matrix(a);

 		double[][] d = getDegreeMatrix();
		Matrix dm = new Matrix(d);

		Matrix lm = dm.minus(am);
		
		double[] eigs = lm.eig().getRealEigenvalues();
		
		if(eigs.length<2) {
			return 0;
		} else {
			Arrays.sort(eigs);
			return eigs[1];
		}
	}
	
	public double getEnergy() {
 		double[][] matrix = getAdjacencyMatrix();
		
 		if(matrix.length == 0) return 0;
 				
		Matrix m = new Matrix(matrix);
		
		double[] eigs = m.eig().getRealEigenvalues();
		
		double energy = 0;
		for(double eig : eigs) {
			energy += Math.abs(eig);
		}
		
		return energy;
	}
	
	public double[][] getAdjacencyMatrix() {
		List<INode> nodes = new ArrayList<>(getNodes());
 		double[][] matrix = new double[nodes.size()][nodes.size()];

		for(int i=0; i<getSize(); i++) {
			for(int j=0; j<getSize(); j++) {
				matrix[i][j] = nodes.get(i).isConnectedTo(nodes.get(j)) ? 1 : 0;
			}
		}
		
		return matrix;
	}

	public double[][] getDegreeMatrix() {
		List<INode> nodes = new ArrayList<>(getNodes());
 		double[][] matrix = new double[nodes.size()][nodes.size()];

		for(int i=0; i<getSize(); i++) {
			for(int j=0; j<getSize(); j++) {
				if(i == j) {
					matrix[i][j] = nodes.get(i).getDegree();
				} else {
					matrix[i][j] = 0;
				}
			}
		}
		
		return matrix;
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
	
	public double getGlobalOverlapping(Set<Graph> cliques) {
		int globalOverlapping = 0;
		int nodesInCliques = 0;
		for(INode node : nodes) {
			int localOverlapping = 0;
			for(Graph clique : cliques) {
				if(clique.nodes.contains(node)) {
					localOverlapping ++;
				}
			}
			if(localOverlapping>0) {
				nodesInCliques++;
			}
			
			globalOverlapping += localOverlapping;
		}
		
		return 1.0 * globalOverlapping/nodesInCliques;
	}
	
	public double getHammingDistance(Graph graph) {
		if(getSize() != graph.getSize() || getSize() <= 1) return -1;
		
		double[][] matrixThis = getAdjacencyMatrix();
		double[][] matrixThat = graph.getAdjacencyMatrix();
		
		int size = getSize();
		int cnt = 0;
		for(int i=0; i<size; i++) {
			for(int j=0; j<i; j++) {
				if(matrixThis[i][j] != matrixThat[i][j]) {
					cnt++;
				}
			}
		}
		
		return 1.0*cnt/(size*(size-1)/2); 
	}
}
