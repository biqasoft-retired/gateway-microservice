/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.admin;

import com.biqasoft.common.exceptions.InvalidStateException;
import com.biqasoft.common.utils.RandomString;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.gateway.admin.dto.ExecuteDatabaseCommandRequestDTO;
import com.biqasoft.gateway.admin.dto.ExecuteDatabaseCommandResultDTO;
import com.biqasoft.gateway.configs.MongoTenantConfiguration;
import com.biqasoft.gateway.system.dto.DataBaseCredentialsDao;
import com.biqasoft.microservice.common.MicroserviceDomain;
import com.biqasoft.microservice.database.MongoHelpers;
import com.biqasoft.microservice.database.MongoTenantHelper;
import com.biqasoft.microservice.database.TenantDatabase;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * http://www.programcreek.com/java-api-examples/index.php?api=com.mongodb.CommandResult
 */
@Service
public class SystemActionsService {

    private final MongoTemplate tenant;
    private final CurrentUser currentUser;
    private final MongoTenantConfiguration mongoTenantConfiguration;
    private final MongoClient mongoClient;
    private final MongoTenantHelper mongoTenantHelper;
    private final MicroserviceDomain microserviceDomain;

    private RandomString randomString = new RandomString(45);
    private RandomString randomStringForPassword = new RandomString(45, RandomString.Strategy.ENGLISH_CHARS_WITH_SPECIAL_CHARS);

    @Autowired
    public SystemActionsService(final @TenantDatabase MongoTemplate tenant, CurrentUser currentUser,
                                final MongoTenantConfiguration mongoTenantConfiguration, final MongoClient mongoClient, MongoTenantHelper mongoTenantHelper,
                                final MicroserviceDomain microserviceDomain) {
        this.currentUser = currentUser;
        this.tenant = tenant;
        this.mongoTenantConfiguration = mongoTenantConfiguration;
        this.mongoClient = mongoClient;
        this.mongoTenantHelper = mongoTenantHelper;
        this.microserviceDomain = microserviceDomain;
    }

    /**
     * http://docs.mongodb.org/manual/reference/command/createUser/#dbcmd.createUser
     * http://docs.mongodb.org/manual/tutorial/add-user-administrator/
     * http://docs.mongodb.org/manual/reference/built-in-roles/
     * <p>
     * <p>
     * CommandResult result =  tenant.executeCommand("db.createUser( { \"user\" : \"" + login + "\", \"pwd\": \"" + password + "\", \"roles\" : [\"readWrite\"] } )");
     **/
    private boolean addUserToDomainDataBase(String login, String password) {
        String dbName = tenant.getDb().getName();

        DBObject cmd = new BasicDBObject();
        cmd.put("createUser", login);
        cmd.put("pwd", password);

        BasicDBList roles = new BasicDBList();
        roles.add(new BasicDBObject("role", "readWrite").append("db", dbName));

        cmd.put("roles", roles);

        CommandResult result = tenant.getDb().command(cmd);

        return result.ok();
    }

    public void createNewDataBaseUser(DataBaseCredentialsDao dataBaseCredentialsDao) {
        String login = randomString.nextString();
        String password = randomStringForPassword.nextString();

        dataBaseCredentialsDao.setSuccess(addUserToDomainDataBase(login, password));

        dataBaseCredentialsDao.setLogin(login);
        dataBaseCredentialsDao.setPassword(password);
        dataBaseCredentialsDao.setAuthDataBase(currentUser.getCurrentUserDomain().getId());
    }

    public CommandResult getAllUsersInDomainDataBase() {
        DBObject cmd = new BasicDBObject();
        cmd.put("usersInfo", 1);

        CommandResult result = tenant.getDb().command(cmd);
        return result;
    }

    public CommandResult dropUserInDomainDataBase(String username) {
        DBObject cmd = new BasicDBObject();
        cmd.put("dropUser", username);
        CommandResult result = tenant.getDb().command(cmd);

        return result;
    }

    /**
     * very dangerous function
     * use carefully
     * system administration use only
     *
     * @param databaseCommandRequestDto
     * @param database
     * @return
     */
    public ExecuteDatabaseCommandResultDTO executeDatabaseCommandAsRootUser(ExecuteDatabaseCommandRequestDTO databaseCommandRequestDto, String database) {
        ExecuteDatabaseCommandResultDTO resultDto = new ExecuteDatabaseCommandResultDTO();

        DBObject dbObject = (DBObject) JSON.parse(databaseCommandRequestDto.getCommand());
        resultDto.setResult(mongoTenantHelper.domainDataBaseUnsafeGet(database).getDb().command(dbObject));
        return resultDto;
    }

    public ExecuteDatabaseCommandResultDTO executeDatabaseCommandAsUserAdmin(ExecuteDatabaseCommandRequestDTO databaseCommandRequestDto) {
        ExecuteDatabaseCommandResultDTO resultDto = new ExecuteDatabaseCommandResultDTO();

        DBObject dbObject = (DBObject) JSON.parse(databaseCommandRequestDto.getCommand());
        resultDto.setResult(this.getDomainUnsafeAsUser(currentUser.getDomain().getDomain()).getDb().command(dbObject));
        return resultDto;
    }

    /**
     * Get mongoTemplate with authenticating user with
     * mongodb roles {@link SystemActionsService#addUserToDomainDataBase }
     * If this.domainDataBaseUnsafeGet method return MongoTemplate with superadmin mongodb user
     * that allow to use command such as hostInfo etc...
     * actions and operations on this mongo template is limited to user database and
     * it is safe to give user access to it
     *
     * @param domain
     * @return
     */
    public MongoTemplate getDomainUnsafeAsUser(String domain) {
        Map<String, MongoTemplate> allDomainsMap = mongoTenantConfiguration.getAllDomainsUsersAccountMap();

        if (allDomainsMap.containsKey(domain)) return allDomainsMap.get(domain);

        Domain domainInCRM = microserviceDomain.unsafeFindDomainById(domain);

        if (domainInCRM == null) {
            throw new InvalidStateException("No such domain: " + domain);
        }

        Domain.DatabaseCredentials databaseCredentials = domainInCRM.getMainDatabaseCredentials();

        // if we have not default user - create it
        if (databaseCredentials == null) {
            DataBaseCredentialsDao dao = new DataBaseCredentialsDao();
            this.createNewDataBaseUser(dao);

            databaseCredentials = new Domain.DatabaseCredentials();

            databaseCredentials.setPassword(dao.getPassword());
            databaseCredentials.setUsername(dao.getLogin());

            domainInCRM.setMainDatabaseCredentials(databaseCredentials);
            microserviceDomain.updateDomain(domainInCRM);
        }

        MongoCredential mongoCredentials = MongoCredential.createScramSha1Credential(
                databaseCredentials.getUsername(),
                domain,
                databaseCredentials.getPassword().toCharArray());

        List<MongoCredential> mongoCredentialsList = new LinkedList<>();
        mongoCredentialsList.add(mongoCredentials);

        String host = null;
        Integer port = null;

        host = mongoClient.getAddress().getHost();
        port = mongoClient.getAddress().getPort();


//        if (databaseCredentials.getTenant() == null){
//            host = mongoClient.getAddress().getHost();
//        }

        MongoClient client = new MongoClient(new ServerAddress(host, port), mongoCredentialsList);

        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(client, domain);

        // if we have not - create connection
        MongoTemplate mongoTemplate = MongoHelpers.MongoConverter(mongoDbFactory);
        allDomainsMap.put(domain, mongoTemplate);

        return mongoTemplate;
    }

}
