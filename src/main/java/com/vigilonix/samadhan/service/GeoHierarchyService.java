package com.vigilonix.samadhan.service;

import com.vigilonix.samadhan.enums.GeoHierarchyType;
import com.vigilonix.samadhan.enums.Post;
import com.vigilonix.samadhan.pojo.GeoHierarchyNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeoHierarchyService {
    private final GeoHierarchyNode rootNode;
    private final Map<UUID, GeoHierarchyNode> nodeByUuid;
    @Getter
    private final Map<GeoHierarchyNode, GeoHierarchyNode> parentMap;
    private final Set<UUID> testNodes;

    @Autowired
    public GeoHierarchyService(GeoHierarchyNode rootNode) {
        this.rootNode = rootNode;
        this.nodeByUuid = new HashMap<>();
        this.parentMap = new HashMap<>();
        testNodes = new HashSet<>();

        // Initialize the index and parent-child relationships
        initializeGeoHierarchyNodeMaps();
    }

    // Initialization methods
    private void initializeGeoHierarchyNodeMaps() {
        Queue<GeoHierarchyNode> bfsQueue = new LinkedList<>();
        bfsQueue.offer(rootNode);
        while (!bfsQueue.isEmpty()) {
            GeoHierarchyNode currentNode = bfsQueue.poll();
            if(nodeByUuid.containsKey(currentNode.getUuid())) {
                log.error("duplicate uuid present {}", currentNode);
                throw new IllegalArgumentException("duplicate uuid" + currentNode);
            }
            nodeByUuid.put(currentNode.getUuid(), currentNode);
            currentNode.getChildren().forEach(child -> {
                parentMap.put(child, currentNode);
                if(BooleanUtils.isTrue(currentNode.getIsTest()) || testNodes.contains(currentNode.getUuid())) {
                    testNodes.add(child.getUuid());
                    testNodes.add(currentNode.getUuid());
                }
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
    public boolean hasAuthority(UUID geoHierarchyNodeUuid, Map<Post, List<UUID>> principalPostMap) {
        return getAllLevelNodesOfAuthorityPost(principalPostMap).contains(geoHierarchyNodeUuid);
    }

    public GeoHierarchyNode cloneWithBeatChildren(GeoHierarchyNode node) {
        // Create a single builder instance for the node
        GeoHierarchyNode.GeoHierarchyNodeBuilder builder = GeoHierarchyNode.builder()
                .uuid(node.getUuid())
                .name(node.getName())
                .geofence(node.getGeofence())
                .type(node.getType());

        // Set children based on node type
        if (GeoHierarchyType.THANA.equals(node.getType())) {
            builder.children(Collections.emptyList());
        } else {
            List<GeoHierarchyNode> updatedChildren = node.getChildren().stream()
                    .map(this::cloneWithBeatChildren)
                    .collect(Collectors.toList());
            builder.children(updatedChildren);
        }

        // Build and return the node
        return builder.build();
    }

    public boolean isTestNode(UUID geoHierarchyNodeUuid) {
        return testNodes.contains(geoHierarchyNodeUuid);
    }

    public List<GeoHierarchyNode> getAllLevelNodes(UUID geoHierarchyNodeUuid) {
        return getAllLevelNodes(nodeByUuid.get(geoHierarchyNodeUuid));
    }

    public Map<Post, List<UUID>> resolveGeoHierarchyNodes(final Map<Post, List<UUID>> postGeoHierarchyNodeUuidMap, List<UUID> geoHierarchyNodeUuids) {
        if(CollectionUtils.isEmpty(geoHierarchyNodeUuids)) return postGeoHierarchyNodeUuidMap;
        Map<Post, List<UUID>> postToFilteredGeoNodeUuidsMap = new HashMap<>();

        // Iterate through posts and their corresponding geo node UUIDs
        postGeoHierarchyNodeUuidMap.forEach((post, geoNodeUuids) -> {
            // Filter the geo node UUIDs by checking if they match any input UUIDs
            List<UUID> matchedGeoNodeUuids = geoNodeUuids.stream()
                    .flatMap(geoNodeUuid -> {
//                         Simulate fetching all-level nodes for each geoNodeUuid
                        return getAllLevelNodes(geoNodeUuid).stream().map(GeoHierarchyNode::getUuid);
                    })
                    .filter(geoHierarchyNodeUuids::contains) // Retain only those in geoHierarchyNodeUuids
                    .distinct()
                    .collect(Collectors.toList());

            // If matches are found, add them to the result map
            if (!matchedGeoNodeUuids.isEmpty()) {
                postToFilteredGeoNodeUuidsMap.put(post, matchedGeoNodeUuids);
            }
        });

        return postToFilteredGeoNodeUuidsMap;
    }
}
