package com.hospital.registration.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hospital.registration.model.entity.UserDO;
import com.hospital.registration.model.enums.UserRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String phone;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(UserDO userDO) {
        String rawRole = userDO.getRole();
        String normalizedRole = UserRoleEnum.normalizeRoleValue(rawRole);
        String authorityValue;
        if (StringUtils.hasText(normalizedRole)) {
            authorityValue = "ROLE_" + normalizedRole;
        } else if (StringUtils.hasText(rawRole) && rawRole.trim().regionMatches(true, 0, "ROLE_", 0, 5)) {
            authorityValue = rawRole.trim().toUpperCase();
        } else if (StringUtils.hasText(rawRole)) {
            authorityValue = "ROLE_" + rawRole.trim().toUpperCase();
        } else {
            authorityValue = "ROLE_PATIENT";
        }
        GrantedAuthority authority = new SimpleGrantedAuthority(authorityValue);
        return new UserDetailsImpl(
                userDO.getId(),
                userDO.getUsername(),
                userDO.getPhone(),
                userDO.getPassword(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
