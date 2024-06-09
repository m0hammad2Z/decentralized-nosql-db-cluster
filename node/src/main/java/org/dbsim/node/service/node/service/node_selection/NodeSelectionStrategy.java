package org.dbsim.node.service.node.service.node_selection;

import org.dbsim.node.model.event.DBEvent;
import org.dbsim.node.model.node.NeighborNode;

import java.util.List;

public interface NodeSelectionStrategy {
    List<NeighborNode> selectNodes(DBEvent event, NeighborNode referenceNode, List<NeighborNode> nodes);
}