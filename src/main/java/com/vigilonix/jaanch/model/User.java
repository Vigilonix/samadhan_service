package com.vigilonix.jaanch.model;


import com.vigilonix.jaanch.enums.Rank;
import com.vigilonix.jaanch.enums.Role;
import jakarta.persistence.*;
import lombok.*;
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
    @Column
    private Role role;
    @Column
    private Rank rank;
    @Column
    private String deviceToken;
    @Column
    private String secret;
    @Column
    private String phoneNumber;
}
