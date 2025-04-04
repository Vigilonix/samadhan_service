package com.vigilonix.samadhan.model;


import com.vigilonix.samadhan.enums.Post;
import com.vigilonix.samadhan.enums.Rank;
import com.vigilonix.samadhan.enums.Role;
import com.vigilonix.samadhan.enums.State;
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
@Table(indexes = {
        @Index(name = "user_uuid", columnList = "uuid"),
})
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
    @ToString.Exclude
    private String deviceToken;
    @Column
    @ToString.Exclude
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

    private Gender gender;
}
