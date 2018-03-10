/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.diffs;

import com.biqasoft.audit.object.BiqaClassService;
import com.biqasoft.audit.object.ObjectsAuditHistoryService;
import com.biqasoft.audit.object.diffs.ChangedLog;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.objects.CustomObjectData;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.javers.core.diff.Change;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "History objects")
@ApiIgnore
@RestController
@Secured(value = {SystemRoles.HISTORY_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RequestMapping(value = "/v1/diff/history/objects")
public class DiffController {

    private final BiqaClassService biqaClassService;
    private final ObjectsAuditHistoryService objectsAuditHistoryService;
    private final CurrentUser currentUser;
    private final MicroserviceUsersRepository microserviceUsersRepository;
    private final MicroserviceUsersDiffRepository microserviceUsersDiffRepository;

    @Autowired
    public DiffController(BiqaClassService biqaClassService, ObjectsAuditHistoryService objectsAuditHistoryService, CurrentUser currentUser, MicroserviceUsersRepository microserviceUsersRepository, MicroserviceUsersDiffRepository microserviceUsersDiffRepository) {
        this.biqaClassService = biqaClassService;
        this.objectsAuditHistoryService = objectsAuditHistoryService;
        this.currentUser = currentUser;
        this.microserviceUsersRepository = microserviceUsersRepository;
        this.microserviceUsersDiffRepository = microserviceUsersDiffRepository;
    }

    @Secured(value = {SystemRoles.HISTORY_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get history info of customer changes just as string formatted")
    @RequestMapping(value = "class/{className}/id/{id}", method = RequestMethod.GET)
    public Object getChanges(@PathVariable("id") String id, @PathVariable("className") String className) {

        List<Change> changes = null;

        if (className.equals("USERACCOUNT")) {
            return processUserAccountChanges(id, className);
        }

        if (className.equals("CUSTOM_OBJECT")) {
            changes = objectsAuditHistoryService.getChangesByObject(CustomObjectData.class, id);
        } else {
            changes = objectsAuditHistoryService.getChangesByObject(biqaClassService.getClassByName(className), id);
        }

        return ObjectsAuditHistoryService.transformJaversChangesToDTO(changes);
    }

    @Secured(value = {SystemRoles.HISTORY_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get history info of customer changes just as string formatted")
    @RequestMapping(value = "class/{className}/id/{id}/format/string", method = RequestMethod.GET)
    public ChangedLog getChangesStringed(@PathVariable("id") String id, @PathVariable("className") String className) {
        ChangedLog changedLog = new ChangedLog();

        if (className.equals("USERACCOUNT")) {
            return null;
        }

        changedLog.setStringLog(objectsAuditHistoryService.getChangesStringedLog(
                objectsAuditHistoryService.getChangesByObject(biqaClassService.getClassByName(className), id)
        ));
        return changedLog;
    }

    /**
     * Allow admin to view changes of users
     * ONLY in his domain
     *
     * @param id
     * @param className
     * @return
     */
    private JsonNode processUserAccountChanges(String id, String className) {

        if (!currentUser.haveRole(SystemRoles.ROLE_ADMIN)) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("auth.access.deny.useraccount.view.changes");
            return null;
        } else {
            Domain domain = microserviceUsersRepository.findDomainForUserAccountId(id);

            if (domain == null) {
                ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("user.no_such_id");
            }

            if (domain.getDomain() == null || !domain.getDomain().equals(currentUser.getDomain().getDomain())) {
                throw new AccessDeniedException("This user from another domain");
            }

            return microserviceUsersDiffRepository.findChanges(className, id);
        }
    }

}
