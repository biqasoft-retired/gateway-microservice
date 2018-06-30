/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.users.domain.useraccount.UserAccount;
import com.biqasoft.entity.core.CurrentUser;

import java.util.Date;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 2/21/2016.
 * All Rights Reserved
 */

@Service
@Primary
@Profile("test")
public class CurrentUserMock implements CurrentUser {
    @Override
    public UserAccount getCurrentUser() {
        UserAccount userAccount = new UserAccount();
        return userAccount;
    }

    @Override
    public DomainSettings getCurrentUserDomain() {
        return null;
    }

    @Override
    public Domain getDomain() {
        Domain domain = new Domain();
        domain.setDomain("biqa_domain_test");
        return domain;
    }

    @Override
    public String printWithDateFormat(Date date) {
        return null;
    }

    @Override
    public String getLanguage() {
        return DEFAULT_LANGUAGE;
    }


    public CurrentUserMock() {
    }

    @Override
    public boolean haveRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority(role));
    }
}

