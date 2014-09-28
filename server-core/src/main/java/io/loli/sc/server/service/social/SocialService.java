package io.loli.sc.server.service.social;

import io.loli.sc.server.dao.social.SocialDao;
import io.loli.sc.server.entity.Social;
import io.loli.sc.server.entity.User;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;

@Named
public class SocialService {

    private static final Logger logger = Logger.getLogger(SocialService.class);

    @Inject
    private SocialDao sd;

    @Transactional
    public void save(Social s) {
        s.setCreateDate(new Date());
        sd.save(s);
    }

    @Transactional
    public void save(String userId, String token, String name, String type, long expried) {
        Social s = null;
        try {
            s = sd.findByUserIdAndType(userId, type);
            this.updateToken(userId, token, type, expried);
        } catch (NoResultException e) {
            s = new Social();
            s.setAccessToken(token);
            s.setUid(userId);
            s.setCreateDate(new Date());
            s.setExpired(expried);
            s.setName(name);
            s.setType(type);
            this.save(s);
        }

    }

    public Social findByUserIdAndType(String userId, String type) {
        return sd.findByUserIdAndType(userId, type);
    }

    @Transactional
    public Social updateToken(String userId, String token, String type, long expried) {
        Social s = sd.findByUserIdAndType(userId, type);
        s.setCreateDate(new Date());
        s.setAccessToken(token);
        return s;
    }
}
