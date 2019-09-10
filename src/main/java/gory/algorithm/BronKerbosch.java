package gory.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gory.domain.Graph;
import gory.domain.INode;

public class BronKerbosch {
	public Set<Graph> findMaxCliques(Graph graph) {
		Set<Set<INode>> cliques = new HashSet<>();
        
		Set<INode> potentialClique = new HashSet<>();
        Set<INode> candidates = new HashSet<>();
        Set<INode> alreadyFound = new HashSet<>();
        
        candidates.addAll(graph.getNodes());
        findCliques(cliques, potentialClique,candidates,alreadyFound);
        
        Set<Graph> graphs = new LinkedHashSet<>();
        int cnt = 0;
        for(Set<INode> clique : cliques) {
        	if(clique.size()<3) continue;
        	
        	cnt++;
        	Graph g = new Graph("Clique "+cnt, graph.getConnectionDistance());
        	for(INode node : clique) {
        		g.addNode(node.cloneIt());
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
                Set<INode> newCandidates = new HashSet<>();
                Set<INode> newAlreadyFound = new HashSet<>();

                // move candidate node to potential_clique
                potentialClique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (INode newCandidate : candidates) {
                    if (candidate.isConnectedTo(newCandidate)) {
                        newCandidates.add(newCandidate);
                    }
                }

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (INode newFound : alreadyFound) {
                    if (candidate.isConnectedTo(newFound)) {
                        newAlreadyFound.add(newFound);
                    }
                }

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
                if(found.isConnectedTo(candidate)) {
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
