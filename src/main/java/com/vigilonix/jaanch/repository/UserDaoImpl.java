package com.vigilonix.jaanch.repository;

import com.dt.beyond.enums.State;
import com.dt.beyond.model.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserDaoImpl {
    private final EntityManager entityManager;

    public List<User> getQcPendingUsers(int limit, int offset, long expireTime) {
        Session session = entityManager.unwrap(Session.class);


        return session.createNativeQuery("UPDATE users set qc_lock_expire_time = ?0 where id in (select id from users where state = ?1 and (qc_lock_expire_time <= ?2 or qc_lock_expire_time is null) limit ?3 offset ?4) returning *", User.class)
                .setParameter(0, expireTime)
                .setParameter(1, State.QC_PENDING.name())
                .setParameter(2, System.currentTimeMillis())
                .setParameter(3, limit)
                .setParameter(4, offset)
                .list();
    }

}
