package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.UserRegisterRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRegisterRequestDao {
    private static final Logger log = LoggerFactory.getLogger(UserRegisterRequestDao.class);
    private EntityManager em;

    public UserRegisterRequestDao(EntityManager em) {
        this.em = em;
    }

    public void create(UserRegisterRequest request) {
        em.getTransaction().begin();
        em.persist(request);
        em.getTransaction().commit();
    }

    public UserRegisterRequest findById(String id) {
        return em.find(UserRegisterRequest.class, id);
    }

    public List<UserRegisterRequest> findAllPending() {
        TypedQuery<UserRegisterRequest> query = em.createQuery(
            "SELECT r FROM UserRegisterRequest r WHERE r.status = :status ORDER BY r.createDate ASC",
            UserRegisterRequest.class);
        query.setParameter("status", "PENDING");
        List<UserRegisterRequest> result = query.getResultList();
        log.info("[findAllPending] 查询到注册请求数量: {}", result.size());
        for (UserRegisterRequest req : result) {
            log.info("[findAllPending] 注册请求: 用户名={}, 状态={}", req.getUsername(), req.getStatus());
        }
        return result;
    }

    public void update(UserRegisterRequest request) {
        em.getTransaction().begin();
        em.merge(request);
        em.getTransaction().commit();
    }
}
