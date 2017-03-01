/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.externalservice;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.microservice.database.MainDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExternalServiceTokenRepository {

    private final MongoOperations mainDataBase;

    private final CurrentUser currentUser;

    @Autowired
    public ExternalServiceTokenRepository(CurrentUser currentUser, @MainDatabase MongoOperations mainDataBase) {
        this.currentUser = currentUser;
        this.mainDataBase = mainDataBase;
    }

    @BiqaAddObject
    public ExternalServiceToken addExternalServiceToken(ExternalServiceToken token) {
        token.setDomain(currentUser.getDomain().getDomain());
        token.setCreatedInfo(new CreatedInfo(new Date(), currentUser.getCurrentUser().getId()));
        mainDataBase.insert(token);
        return token;
    }

    public List<ExternalServiceToken> findAllStorageTokens() {
        List<ExternalServiceToken> externalServiceTokens = new ArrayList<>();

        externalServiceTokens.addAll(findExternalServiceTokensByType(TOKEN_TYPES.DROPBOX));
        externalServiceTokens.addAll(findExternalServiceTokensByType(TOKEN_TYPES.DEFAULT_STORAGE));
        externalServiceTokens.addAll(findExternalServiceTokensByType(TOKEN_TYPES.GOOGLE_DRIVE));
        externalServiceTokens.addAll(findExternalServiceTokensByType(TOKEN_TYPES.WEBDAV));
        externalServiceTokens.addAll(findExternalServiceTokensByType(TOKEN_TYPES.S3_COMPATIBLE));

        return externalServiceTokens;
    }

    /**
     * this method is ONLY internal
     * if we call it, we should know that
     * in new token we have 'token'
     * and maybe refreshToken fields
     * because it will override in dataBase
     *
     * @param newToken token to force update
     * @return
     */
    public ExternalServiceToken updateExternalServiceToken(ExternalServiceToken newToken) {
        ExternalServiceToken tokenWithSame = findExternalServiceTokenByIdIgnoreExpired(newToken.getId());

        if (tokenWithSame != null) {
            mainDataBase.save(newToken);
        }
        return newToken;
    }

    /**
     * used when user want update some token info
     * meta data such as name
     * @param newToken
     * @return
     */
    public ExternalServiceToken updateExternalServiceTokenForUser(ExternalServiceToken newToken) {
        ExternalServiceToken tokenWithSame = findExternalServiceTokenByIdIgnoreExpired(newToken.getId());

        if (tokenWithSame != null) {
            // because we hide token - sensitive info to user in API
            // we should get it
            newToken.setRefreshToken(tokenWithSame.getRefreshToken());
            newToken.setToken(tokenWithSame.getToken());
            mainDataBase.save(newToken);
        }
        return newToken;
    }

    /**
     *
     * @param id token id to delete
     * @return true if token exists, otherwise return false
     */
    public boolean deleteExternalServiceTokenById(String id) {
        ExternalServiceToken token = findExternalServiceTokenByIdIgnoreExpired(id);
        if (token != null) {
            mainDataBase.remove(token);
            return true;
        }
        return false;
    }

    public ExternalServiceToken findExternalServiceTokenById(String id) {
        ExternalServiceToken token = mainDataBase.findOne(Query.query(Criteria
                .where("id").is(id)
                .and("domain").is(currentUser.getDomain().getDomain())
        ), ExternalServiceToken.class);

        return token;
    }

    /**
     *
     * @param type {@link TOKEN_TYPES}
     * @return all tokens in current domain with type
     */
    public List<ExternalServiceToken> findExternalServiceTokensByType(String type) {
        List<ExternalServiceToken> tokens = mainDataBase.find(Query.query(Criteria
                .where("domain").is(currentUser.getDomain().getDomain())
                .and("type").is(type)
        ), ExternalServiceToken.class);

        return tokens;
    }

    /**
     * Get token from db y id and type and do not try to update access code if expired
     *
     * @param id token id
     * @param type token type
     * @return token
     */
    public ExternalServiceToken findExternalServiceTokenByLoginAndTypeIgnoreExpired(String id, String type) {
        ExternalServiceToken token = mainDataBase.findOne(Query.query(Criteria
                .where("login").is(id)
                .and("domain").is(currentUser.getDomain().getDomain())
                .and("type").is(type)
        ), ExternalServiceToken.class);
        return token;
    }

    /**
     * Get token from db y id and type and do not try to update access code if expired
     *
     * @param id token id
     * @return token
     */
    public ExternalServiceToken findExternalServiceTokenByIdIgnoreExpired(String id) {
        ExternalServiceToken token = mainDataBase.findOne(Query.query(Criteria
                .where("id").is(id)
                .and("domain").is(currentUser.getDomain().getDomain())
        ), ExternalServiceToken.class);

        return token;
    }

    /**
     *
     * @return all tokens in current domain
     */
    public List<ExternalServiceToken> findAll() {
        return mainDataBase.find(Query.query(Criteria.where("domain").is(currentUser.getDomain().getDomain())), ExternalServiceToken.class);
    }

    /**
     * WARNING: only for internal USE, not user
     * @return all tokens
     */
    public List<ExternalServiceToken> findAllTokensInAllDomains() {
        return mainDataBase.findAll(ExternalServiceToken.class);
    }

}
