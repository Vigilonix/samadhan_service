package com.vigilonix.jaanch.model;


import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.enums.Rank;
import com.vigilonix.jaanch.enums.Role;
import com.vigilonix.jaanch.enums.State;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity(name = "users")
@Getter
@AllArgsConstructor
@Builder
@Setter
@NoArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    @Column(nullable = false)
    private String name;
    @Column
    private Long createdOn;
    @Column
    private Long modifiedOn;
    @Column
    private UUID uuid;
    @Column
    private Long stateChangedOn;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;
    @Column
    @Enumerated(EnumType.STRING)
    private Rank rank;
    @Column
    @Enumerated(EnumType.STRING)
    private State state;
    @Column
    private String deviceToken;
    @Column
    private String secret;
    @Column
    private String phoneNumber;
    @Column
    private Long lastLive;
    @Column
    private Long lastLocationUpdateTimeInMillis;
    @Column
    private Double latitude;
    @Column
    private Double longitude;
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<Post, List<UUID>> postGeoHierarchyNodeUuidMap;
}
