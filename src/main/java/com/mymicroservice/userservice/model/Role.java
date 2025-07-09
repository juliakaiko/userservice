package com.mymicroservice.userservice.model;

import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Role { //implements GrantedAuthority// GrantedAuthority - это полномочия, которые предоставляются пользователю

    USER ("USER"),
    ADMIN("ADMIN");

    private final String roleName;

   /* @Override
    public String getAuthority() {
        return roleName;
    }*/
}
