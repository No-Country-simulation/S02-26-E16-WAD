package com.elevideo.backend.security;

import com.elevideo.backend.enums.AccountStatus;
import com.elevideo.backend.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {
    private final User user;
    private final UUID id;
    private final String username;
    private final String password;
    private final AccountStatus accountStatus;

    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.accountStatus = user.getAccountStatus();
    }

    public UUID getId() { return id; }

    public User getUser() {return user;}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }


    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return accountStatus != AccountStatus.BLOCKED; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return accountStatus == AccountStatus.ACTIVE; }
}
