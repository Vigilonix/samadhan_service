package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.pojo.GeoHierarchyNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeoHierarchyService {
    private final GeoHierarchyNode rootNode;
    private final Map<UUID, GeoHierarchyNode> nodeByUuid;
    private final Map<GeoHierarchyNode, GeoHierarchyNode> parentMap;

    @Autowired
    public GeoHierarchyService(GeoHierarchyNode rootNode) {
        this.rootNode = rootNode;
        this.nodeByUuid = new HashMap<>();
        this.parentMap = new HashMap<>();

        // Initialize the index and parent-child relationships
        initializeFieldGeoNodeMaps();
    }

    // Initialization methods
    private void initializeFieldGeoNodeMaps() {
        Queue<GeoHierarchyNode> bfsQueue = new LinkedList<>();
        bfsQueue.offer(rootNode);
        while (!bfsQueue.isEmpty()) {
            GeoHierarchyNode currentNode = bfsQueue.poll();
            nodeByUuid.put(currentNode.getUuid(), currentNode);
            currentNode.getChildren().forEach(child -> {
                parentMap.put(child, currentNode);
                bfsQueue.offer(child);
            });
        }
    }

    // Node retrieval methods
    public GeoHierarchyNode getNodeById(UUID uuid) {
        return nodeByUuid.get(uuid);
    }

    private List<GeoHierarchyNode> getAllLevelNodes(GeoHierarchyNode startNode) {
        List<GeoHierarchyNode> reachableNodes = new ArrayList<>();
        Queue<GeoHierarchyNode> bfsQueue = new LinkedList<>();
        bfsQueue.offer(startNode);
        while (!bfsQueue.isEmpty()) {
            GeoHierarchyNode node = bfsQueue.poll();
            reachableNodes.add(node);
            node.getChildren().forEach(bfsQueue::offer);
        }
        return reachableNodes;
    }

    public GeoHierarchyNode getHighestPostNode(Map<Post, List<UUID>> postGeoNodeMap) {
        Post highestPost = findHighestPost(postGeoNodeMap);
        return nodeByUuid.get(postGeoNodeMap.get(highestPost).get(0));
    }

    // Node filtering methods
    private List<GeoHierarchyNode> getFirstLevelNodesOfAuthorityPost(Map<Post, List<UUID>> postGeoNodeMap) {
        return postGeoNodeMap.entrySet().stream()
                .filter(entry -> entry.getKey().getLevel() > 1)
                .flatMap(entry -> entry.getValue().stream())
                .map(nodeByUuid::get)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<UUID> getAllLevelNodesOfAuthorityPost(Map<Post, List<UUID>> postGeoNodeMap) {
        return getFirstLevelNodesOfAuthorityPost(postGeoNodeMap).stream()
                .flatMap(node -> getAllLevelNodes(node).stream())
                .map(GeoHierarchyNode::getUuid)
                .collect(Collectors.toList());
    }

    public List<UUID> getAllLevelNodes(Map<Post, List<UUID>> postGeoNodeMap) {
        return postGeoNodeMap.values().stream()
                .flatMap(Collection::stream)
                .map(nodeByUuid::get)
                .flatMap(node -> getAllLevelNodes(node).stream())
                .map(GeoHierarchyNode::getUuid)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<UUID> getFirstLevelNodes(Map<Post, List<UUID>> postGeoNodeMap) {
        return postGeoNodeMap.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public Post findHighestPost(Map<Post, List<UUID>> postGeoNodeMap) {
        return postGeoNodeMap.keySet().stream()
                .max(Comparator.comparingInt(Post::getLevel))
                .orElseThrow(() -> new NoSuchElementException("No posts found"));
    }

    // Authorization methods
    public boolean hasAuthority(UUID fieldGeoNodeUuid, Map<Post, List<UUID>> principalPostMap) {
        return getAllLevelNodesOfAuthorityPost(principalPostMap).contains(fieldGeoNodeUuid);
    }

    // Utility methods
    public GeoHierarchyNode transformWithoutChildren(GeoHierarchyNode node) {
        return GeoHierarchyNode.builder()
                .uuid(node.getUuid())
                .name(node.getName())
                .geofence(node.getGeofence())
                .type(node.getType())
                .build();
    }
}
