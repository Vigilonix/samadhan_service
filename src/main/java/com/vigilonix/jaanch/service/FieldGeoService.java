package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.enums.GeoHierarchyType;
import com.vigilonix.jaanch.enums.Post;
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

    public List<FieldGeoNode> getAllChildren(FieldGeoNode queryNode) {
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
        return null;
    }

    public List<FieldGeoNode> getOwnershipGeoNode(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return postFieldGeoNodeUuidMap.entrySet().stream().filter(e->e.getKey().getLevel()>1)
                .flatMap(e->e.getValue().stream())
                .map(fieldGeoNodeIndexByUuid::get)
                .collect(Collectors.toList());
    }

    public FieldGeoNode highestPostGeoNode(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return null;
    }

    public List<UUID> getAllChildren(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
        return getOwnershipGeoNode(postFieldGeoNodeUuidMap)
                .stream().map(fieldGeoNodeIndexByUuid::get)
                .flatMap(f->getAllChildren(f).stream())
                .map(FieldGeoNode::getUuid)
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
}
