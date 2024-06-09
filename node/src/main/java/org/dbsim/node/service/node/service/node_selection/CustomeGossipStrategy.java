package org.dbsim.node.service.node.service.node_selection;

import org.dbsim.node.model.event.DBEvent;
import org.dbsim.node.model.node.NeighborNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomeGossipStrategy implements NodeSelectionStrategy{

    // This method selects the nodes to gossip with based on the main node and the list of nodes
    @Override
    public List<NeighborNode> selectNodes(DBEvent event, NeighborNode mainNodeNeighbor, List<NeighborNode> nodes) {
        List<NeighborNode> selectedNodes = new ArrayList<>();

        if (nodes.size() < 2) {
            return nodes;
        }

        nodes.sort(Comparator.comparingInt(NeighborNode::hashCode));

        if (event.getNodeID() == mainNodeNeighbor.hashCode()) {
            selectedNodes.addAll(selectFirstNodesGossip(nodes, mainNodeNeighbor));
        } else {
            selectedNodes.addAll(selectRestNodesGossip(nodes, mainNodeNeighbor));
        }

        return selectedNodes;
    }

    // This method selects the first nodes to gossip with based on the main node and the list of nodes
    private List<NeighborNode> selectFirstNodesGossip(List<NeighborNode> nodes, NeighborNode mainNodeNeighbor) {
        List<NeighborNode> selectedNodes = new ArrayList<>();

        if (nodes.size() < 4) {
            return nodes;
        }

        nodes.sort(Comparator.comparingInt(node -> Math.abs(node.hashCode() - mainNodeNeighbor.hashCode())));

        int mainNodeIndex = nodes.indexOf(mainNodeNeighbor);

        if (mainNodeIndex > 1) {
            selectedNodes.add(nodes.get(mainNodeIndex - 1));
            selectedNodes.add(nodes.get(mainNodeIndex - 2));
        }

        if (mainNodeIndex < nodes.size() - 2) {
            selectedNodes.add(nodes.get(mainNodeIndex + 1));
            selectedNodes.add(nodes.get(mainNodeIndex + 2));
        }

        return selectedNodes;
    }

    private List<NeighborNode> selectRestNodesGossip(List<NeighborNode> nodes, NeighborNode mainNodeNeighbor) {
        List<NeighborNode> selectedNodes = new ArrayList<>();

        if (mainNodeNeighbor.hashCode() > nodes.get(nodes.size() - 1).hashCode()) {
            selectedNodes.add(nodes.get(nodes.size() - 1));
        }

        if (mainNodeNeighbor.hashCode() < nodes.get(0).hashCode()) {
            selectedNodes.add(nodes.get(0));
        }

        return selectedNodes;
    }
}
