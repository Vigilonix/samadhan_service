package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.enums.GeoHierarchyType;
import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.FieldGeoNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FieldGeoService {
    private final FieldGeoNode rootNode;
    private final Map<UUID, FieldGeoNode> fieldGeoNodeIndexByUuid;
    private final Map<FieldGeoNode, FieldGeoNode> childParentMap;


    @Autowired
    public FieldGeoService(FieldGeoNode rootNode) {
        this.rootNode = rootNode;
        fieldGeoNodeIndexByUuid = new HashMap<>();
        childParentMap = new HashMap<>();

        Queue<FieldGeoNode> bfsQueue = new LinkedList<>();
        bfsQueue.offer(rootNode);
        while (!bfsQueue.isEmpty()) {
            FieldGeoNode fieldGeoNode = bfsQueue.poll();
            fieldGeoNodeIndexByUuid.put(fieldGeoNode.getUuid(), fieldGeoNode) ;
            fieldGeoNode.getChildren().forEach(child-> {
                childParentMap.put(child, fieldGeoNode);
                bfsQueue.offer(child);
            });
        }
    }

    public Optional<String> getParentFieldNode(FieldGeoNode fieldGeoNode, GeoHierarchyType geoHierarchyType) {
        if(geoHierarchyType.equals(fieldGeoNode.getType()))
            return Optional.of(fieldGeoNode.getName());
        if(childParentMap.containsKey(fieldGeoNode)) return getParentFieldNode(childParentMap.get(fieldGeoNode), geoHierarchyType);
        return Optional.empty();
    }

    public List<FieldGeoNode> getAllReachableNodes(FieldGeoNode queryNode) {
        List<FieldGeoNode> children = new ArrayList<>();
        Queue<FieldGeoNode> bfsQueue = new LinkedList<>();
        bfsQueue.offer(queryNode);
        while (!bfsQueue.isEmpty()) {
            FieldGeoNode fieldGeoNode = bfsQueue.poll();
            children.add(fieldGeoNode);
            fieldGeoNode.getChildren().forEach(bfsQueue::offer);
        }
        return children;
    }

    public FieldGeoNode resolveFieldGeoNode(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return highestPostGeoNode(postFieldGeoNodeUuidMap);
    }

    public List<FieldGeoNode> getOwnershipGeoNodes(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return postFieldGeoNodeUuidMap.entrySet().stream().filter(e->e.getKey().getLevel()>1)
                .flatMap(e->e.getValue().stream())
                .map(fieldGeoNodeIndexByUuid::get)
                .distinct()
                .collect(Collectors.toList());
    }

    public FieldGeoNode highestPostGeoNode(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        Post post = highestPost(postFieldGeoNodeUuidMap);
        return fieldGeoNodeIndexByUuid.get(postFieldGeoNodeUuidMap.get(post).get(0));
    }

    public List<UUID> getAllOwnershipChildren(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return getOwnershipGeoNodes(postFieldGeoNodeUuidMap).stream()
                .flatMap(f-> getAllReachableNodes(f).stream())
                .filter(Objects::nonNull)
                .map(FieldGeoNode::getUuid)
                .collect(Collectors.toList());
    }

    public List<UUID> getAllGeoUuids(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return postFieldGeoNodeUuidMap.entrySet().stream()
                .flatMap(e->e.getValue().stream())
                .map(fieldGeoNodeIndexByUuid::get)
                .flatMap(f-> getAllReachableNodes(f).stream())
                .filter(Objects::nonNull)
                .map(FieldGeoNode::getUuid)
                .distinct()
                .collect(Collectors.toList());
    }

    public Post highestPost(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        Set<Post> post = postFieldGeoNodeUuidMap.keySet() ;
        TreeSet<Post> set = new TreeSet<>(Comparator.comparingInt(Post::getLevel));
        set.addAll(post);
        return set.last();
    }

    public FieldGeoNode getFieldGeoNode(UUID uuid) {
        return fieldGeoNodeIndexByUuid.get(uuid);
    }

    public List<UUID> getAllFieldGeoNode(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return postFieldGeoNodeUuidMap.values().stream().flatMap(Collection::stream)
                .distinct().collect(Collectors.toList());
    }

    List<UUID> getSameOrBelowGeoNodeUuids(User principal) {
        List<UUID> fieldGeoNodes = getAllGeoUuids(principal.getPostFieldGeoNodeUuidMap());
        List<UUID> allFeildNode = getAllFieldGeoNode(principal.getPostFieldGeoNodeUuidMap());
        Set<UUID> sameOrBelowGeoNodes = new HashSet<>();
        sameOrBelowGeoNodes.addAll(fieldGeoNodes);
        return sameOrBelowGeoNodes.stream().toList();
    }

    public boolean hasGeoAuthority(UUID fieldGeoNodeUuid, User principalUser) {
        Map<UUID, Post> resultMap = principalUser.getPostFieldGeoNodeUuidMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(uuid -> Map.entry(uuid, entry.getKey())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existingValue, newValue) -> existingValue.getLevel()>newValue.getLevel()?existingValue:newValue// Always use the latest value
                ));
        return resultMap.getOrDefault(fieldGeoNodeUuid, Post.BEAT).getLevel()>1;
    }

    public FieldGeoNode clean(FieldGeoNode fieldGeoNode) {
        return FieldGeoNode.builder()
                .uuid(fieldGeoNode.getUuid())
                .name(fieldGeoNode.getName())
                .geofence(fieldGeoNode.getGeofence())
                .type(fieldGeoNode.getType())
                .build();
    }
}
