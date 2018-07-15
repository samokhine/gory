package gory.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gory.domain.Graph;
import gory.domain.INode;

public class BronKerbosch2 {
	private boolean[] connectivityMatrix;
	
	public Set<Graph> findMaxCliques(Graph graph) {
		Set<Set<INode>> cliques = new HashSet<>();
        
		Set<INode> potentialClique = new HashSet<>();
        Set<INode> candidates = new HashSet<>();
        Set<INode> alreadyFound = new HashSet<>();
        
        int N = graph.getSize();
        connectivityMatrix = new boolean[N*N/2-N/2];
        int k=0;
        int i=0;
        for(INode node1 : graph.getNodes()) {
        	i++;
            node1.setId(i);
            if(i==1) continue;
            
        	int j=0;
            for(INode node2 : graph.getNodes()) {
            	connectivityMatrix[k++] = node2.isConnectedTo(node1);
            	j++;
            	if(j>=i-1) break;
            }
        }
        
        candidates.addAll(graph.getNodes());
        findCliques(cliques, potentialClique, candidates, alreadyFound);
        
        Set<Graph> graphs = new LinkedHashSet<>();
        int cnt = 0;
        for(Set<INode> clique : cliques) {
        	if(clique.size()<3) continue;
        	
        	cnt++;
        	Graph g = new Graph("Clique "+cnt, graph.getConnectionDistance());
        	for(INode node : clique) {
        		g.addNode(node.clone(node));
        	}
        	graphs.add(g);
        }
        
        return graphs;
	}
	
    private void findCliques(Set<Set<INode>> cliques, Set<INode> potentialClique, Set<INode> candidates, Set<INode> alreadyFound) {
    	List<INode> candidatesArray = new ArrayList<>(candidates);
        if (!end(candidates, alreadyFound)) {
            // for each candidate_node in candidates do
            for (INode candidate : candidatesArray) {
                // move candidate node to potential_clique
                potentialClique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                Set<INode> newCandidates = candidates.stream().filter(newCandidate -> connected(candidate, newCandidate)).collect(Collectors.toSet());

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                Set<INode> newAlreadyFound = alreadyFound.stream().filter(newFound -> connected(candidate, newFound)).collect(Collectors.toSet());

                // if new_candidates and new_already_found are empty
                if (newCandidates.isEmpty() && newAlreadyFound.isEmpty()) {
                    // potential_clique is maximal_clique
                    cliques.add(new HashSet<>(potentialClique));
                }
                else {
                    findCliques(
                    	cliques,
                        potentialClique,
                        newCandidates,
                        newAlreadyFound);
                }

                // move candidate_node from potential_clique to already_found;
                alreadyFound.add(candidate);
                potentialClique.remove(candidate);
            }
        }
    }
	
    private boolean end(Set<INode> candidates, Set<INode> alreadyFound) {
        // if a node in alreadyFound is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for(INode found : alreadyFound) {
            edgecounter = 0;
            for (INode candidate : candidates) {
                if(connected(found, candidate)) {
                    edgecounter++;
                }
            }
            if (edgecounter == candidates.size()) {
                end = true;
            }
        }
        return end;
    }
    
    private boolean connected(INode node1, INode node2) {
    	int i = node1.getId().intValue();
    	int j = node2.getId().intValue();
    	if(i == j) return false;
    	
    	int max = Math.max(i, j);
    	int min = Math.min(i, j);
    	
    	int index = max*max/2 - max/2 - (max-1) + (min-1);
    	
    	return connectivityMatrix[index];
    }
}
