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
        
		List<Node> potentialClique = new ArrayList<>();
        ArrayList<Node> candidates = new ArrayList<>();
        ArrayList<Node> alreadyFound = new ArrayList<>();
        
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
	
    private void findCliques(Set<Set<Node>> cliques, List<Node> potentialClique, List<Node> candidates, List<Node> alreadyFound) {
    	List<Node> candidatesArray = new ArrayList<>(candidates);
        if (!end(candidates, alreadyFound)) {
            // for each candidate_node in candidates do
            for (Node candidate : candidatesArray) {
                List<Node> newCandidates = new ArrayList<>();
                List<Node> newAlreadyFound = new ArrayList<>();

                // move candidate node to potential_clique
                potentialClique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (Node newCandidate : candidates) {
                    if (candidate.getConnectedNodes().contains(newCandidate)) {
                        newCandidates.add(newCandidate);
                    }
                }

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (Node newFound : alreadyFound) {
                    if (candidate.getConnectedNodes().contains(newFound)) {
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
	
    private boolean end(List<Node> candidates, List<Node> alreadyFound) {
        // if a node in alreadyFound is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for(Node found : alreadyFound) {
            edgecounter = 0;
            for (Node candidate : candidates) {
                if(found.getConnectedNodes().contains(candidate)) {
                    edgecounter++;
                }
            }
            if (edgecounter == candidates.size()) {
                end = true;
            }
        }
        return end;
    }
	/*
	public Set<Set<Node>> maxCliques(Set<Node> people){
        cliques = new HashSet();
        ArrayList<Node> potential_clique = new ArrayList<Node>();
        ArrayList<Node> candidates = new ArrayList<Node>();
        ArrayList<Node> already_found = new ArrayList<Node>();
        candidates.addAll(people);
        findCliques(potential_clique,candidates,already_found);
        return cliques;
    }
    
    private void findCliques(ArrayList<Node> potential_clique, ArrayList<Node> candidates, ArrayList<Node> already_found) {
    	ArrayList<Node> candidates_array = new ArrayList(candidates);
        if (!end(candidates, already_found)) {
            // for each candidate_node in candidates do
            for (Node candidate : candidates_array) {
                ArrayList<Node> new_candidates = new ArrayList<Node>();
                ArrayList<Node> new_already_found = new ArrayList<Node>();

                // move candidate node to potential_clique
                potential_clique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (Node new_candidate : candidates) {
                    if (candidate.Nodes.containsKey(new_candidate))
                    {
                        new_candidates.add(new_candidate);
                    }
                }

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (Node new_found : already_found) {
                    if (candidate.Nodes.containsKey(new_found)) {
                        new_already_found.add(new_found);
                    }
                }

                // if new_candidates and new_already_found are empty
                if (new_candidates.isEmpty() && new_already_found.isEmpty()) {
                    // potential_clique is maximal_clique
                    cliques.add(new HashSet<Node>(potential_clique));
                }
                else {
                    findCliques(
                        potential_clique,
                        new_candidates,
                        new_already_found);
                }

                // move candidate_node from potential_clique to already_found;
                already_found.add(candidate);
                potential_clique.remove(candidate);
            }
        }
    }
    
    private boolean end(ArrayList<Node> candidates, ArrayList<Node> already_found) {
        // if a node in already_found is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for (Node found : already_found) {
            edgecounter = 0;
            for (Node candidate : candidates) {
                if (found.Nodes.containsKey(candidate)) {
                    edgecounter++;
                }
            }
            if (edgecounter == candidates.size()) {
                end = true;
            }
        }
        return end;
    }
    */
}
