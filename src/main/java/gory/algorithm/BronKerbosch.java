package gory.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.Node;

public class BronKerbosch {
	public Set<Graph> findMaxCliques(Graph graph) {
		Set<Set<Node>> cliques = new HashSet<>();
        
		Set<Node> potentialClique = new HashSet<>();
        Set<Node> candidates = new HashSet<>();
        Set<Node> alreadyFound = new HashSet<>();
        
        candidates.addAll(graph.getNodes());
        findCliques(cliques, potentialClique,candidates,alreadyFound);
        
        Set<Graph> graphs = new LinkedHashSet<>();
        int cnt = 0;
        for(Set<Node> clique : cliques) {
        	if(clique.size()<3) continue;
        	
        	cnt++;
        	Graph g = new Graph("Clique "+cnt, graph.getConnectionDistance());
        	for(Node node : clique) {
        		g.addNode(new Node(node.getPartition()));
        	}
        	graphs.add(g);
        }
        
        return graphs;
	}
	
    private void findCliques(Set<Set<Node>> cliques, Set<Node> potentialClique, Set<Node> candidates, Set<Node> alreadyFound) {
    	List<Node> candidatesArray = new ArrayList<>(candidates);
        if (!end(candidates, alreadyFound)) {
            // for each candidate_node in candidates do
            for (Node candidate : candidatesArray) {
                Set<Node> newCandidates = new HashSet<>();
                Set<Node> newAlreadyFound = new HashSet<>();

                // move candidate node to potential_clique
                potentialClique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (Node newCandidate : candidates) {
                    if (candidate.getConnectedPartitions().contains(newCandidate.getPartition())) {
                        newCandidates.add(newCandidate);
                    }
                }

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (Node newFound : alreadyFound) {
                    if (candidate.getConnectedPartitions().contains(newFound.getPartition())) {
                        newAlreadyFound.add(newFound);
                    }
                }

                // if new_candidates and new_already_found are empty
                if (newCandidates.isEmpty() && newAlreadyFound.isEmpty()) {
                    // potential_clique is maximal_clique
                    cliques.add(new HashSet<Node>(potentialClique));
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
	
    private boolean end(Set<Node> candidates, Set<Node> alreadyFound) {
        // if a node in alreadyFound is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for(Node found : alreadyFound) {
            edgecounter = 0;
            for (Node candidate : candidates) {
                if(found.getConnectedPartitions().contains(candidate.getPartition())) {
                    edgecounter++;
                }
            }
            if (edgecounter == candidates.size()) {
                end = true;
            }
        }
        return end;
    }
}
