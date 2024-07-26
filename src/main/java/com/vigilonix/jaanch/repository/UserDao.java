package com.vigilonix.jaanch.repository;

import com.dt.beyond.enums.SsoProvider;
import com.dt.beyond.enums.State;
import com.dt.beyond.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserDao extends JpaRepository<User, Long> {

    User findBySsoIdAndSsoProvider(String userId, SsoProvider ssoProvider);

    User findByReferralCode(String referralCode);

    User findByUuid(UUID uuid);

    User findByEmail(String email);

    List<User> findByIdIn(List<Long> ids);

    User findByUsername(String username);

    @Modifying
    @Query("UPDATE users as u SET u.popularity= :popularity WHERE u.id= :id")
    void updatePopularity(Long id, int popularity);

    @Query("select id from users where state = :state")
    List<Long> getAllUserIdsByState(State state);

    @Query(nativeQuery = true, value = "select  u from users as u, jsonb_array_elements(social_media_mappings)  as social where (social ->>'expiredOn')::int  <= ?1")
    List<User> findAllByExpiredSocialMediaOnLessThanEqual(long scanTimeRange);


}
