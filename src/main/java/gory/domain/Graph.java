package gory.domain;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import gory.algorithm.BronKerbosch;
import gory.algorithm.Dijkstra;
import lombok.Getter;
import lombok.Setter;

public class Graph {
	@Getter @Setter
	private String name;

	@Getter
	private int connectionDistance = 1;
	
	@Getter
	private Set<Node> nodes = new HashSet<>();

	public Graph(String name) {
		this.name = name;
	}

	public Graph(String name, int connectionDistance) {
		this.name = name;
		this.connectionDistance = connectionDistance;
	}
	
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
			if(distance >= 0 && distance <= connectionDistance) {
				node.connect(newNode);
			}
		}
		
		nodes.add(newNode);
		
		return true;
	}
	
	public void removeNode(Node nodeToRemove) {
		nodes.remove(nodeToRemove);
		
		for(Node node : nodes) {
			node.getConnectedNodes().remove(nodeToRemove);
		}
	}
	
	public void replaceNode(Node oldNode, Node newNode) {
		removeNode(oldNode);
		addNode(newNode);
	}
	
	public Set<Graph> getCliques() {
		BronKerbosch algorithm = new BronKerbosch();
		return algorithm.findMaxCliques(this);
	}

	public int getDiameter() {
		Dijkstra algorithm = new Dijkstra();
		return algorithm.getDiameter(this);
	}

	public double getDensityAdjacentMatrix() {
		double dam = 0;
		for(Node node : nodes) {
			dam += node.getConnectedNodes().size();
		}
		
		dam /= (getSize() * (getSize() - 1));
		
		return dam;
	}
	
	public double getClusteringCoefficientUsingTriangles() {
		double total = 0.0;
        for (Node v : getNodes()) {
        	total += v.getClusteringCoefficientUsingTriangles();

        }

        return total / getSize();
	}
	
 	public double getClusteringCoefficientUsingMatrix() {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		List<Node> nodes = new ArrayList<>(getNodes());
		AtomicInteger Ntr = new AtomicInteger();
		for(int i=0; i<nodes.size(); i++) {
			final int iFinal = i;
			executor.submit(new Callable<Void>() {
				public Void call() {
					for(int j=iFinal+1; j<nodes.size(); j++) {
						int jFinal = j;
						for(int k=j+1; k<nodes.size(); k++) {
							int kFinal = k;
							Ntr.updateAndGet(x -> x + (nodes.get(iFinal).isConnectedTo(nodes.get(jFinal)) ? 
										nodes.get(iFinal).isConnectedTo(nodes.get(kFinal)) ? 
												nodes.get(jFinal).isConnectedTo(nodes.get(kFinal)) ? 1 : 0
											: 0
									: 0));
				        }
			        }
					
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
		for(int i=0; i<nodes.size(); i++) {
			final int iFinal = i;
			executor.submit(new Callable<Void>() {
				public Void call() {
					for(int j=iFinal+1; j<nodes.size(); j++) {
						int jFinal = j;
						for(int k=j+1; k<nodes.size(); k++) {
							int kFinal = k;
							N3.updateAndGet(x -> x + ((nodes.get(iFinal).isConnectedTo(nodes.get(jFinal)) ? 1 : 0)*(nodes.get(iFinal).isConnectedTo(nodes.get(kFinal)) ? 1 : 0) +
									(nodes.get(jFinal).isConnectedTo(nodes.get(iFinal)) ? 1 : 0)*(nodes.get(jFinal).isConnectedTo(nodes.get(kFinal)) ? 1 : 0) +
									(nodes.get(kFinal).isConnectedTo(nodes.get(iFinal)) ? 1 : 0)*(nodes.get(kFinal).isConnectedTo(nodes.get(jFinal)) ? 1 : 0)));
						}
			        }
					
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
	
	public Map<Integer, Double> getNodeDegreeDistribution() {
		Map<Integer, AtomicInteger> nodeDegreeDistribution = new TreeMap<>();
		for(Node node : nodes) {
			int degree = node.getDegree();
			
			AtomicInteger degreeCnt = nodeDegreeDistribution.get(degree);
			if(degreeCnt == null) {
				degreeCnt = new AtomicInteger();
				nodeDegreeDistribution.put(degree, degreeCnt);
			}
			degreeCnt.incrementAndGet();
		}
		
		Map<Integer, Double> result = new TreeMap<>();
		for(int degree : nodeDegreeDistribution.keySet()) {
			result.put(degree, 1.0*nodeDegreeDistribution.get(degree).intValue()/getSize());
		}
		
		return result;
	}
	
	public int getCoalitionResource() {
		int coalitionResource = 0;
		for(Node node : nodes) {
			int sumOfSummonds = 0;
			for(int summond : node.getSummands()) {
				sumOfSummonds += summond;
			}

			int degree = node.getDegree();
			coalitionResource += degree * sumOfSummonds;
		}
		
		return coalitionResource;
	}
	
	public int getSumOfDegrees() {
		int sumOfDegrees = 0;
		for(Node node : nodes) {
			int degree = node.getDegree();
			sumOfDegrees += degree;
		}
		return sumOfDegrees;
	}
}
