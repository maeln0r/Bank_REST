package com.example.bankcards.service;


import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.JpaUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final JpaUserRepository users;


    public UserDetailsServiceImpl(JpaUserRepository users) {
        this.users = users;
    }


    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        UserEntity u = users.findByUsername(usernameOrEmail)
                .or(() -> users.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new User(
                u.getUsername(),
                u.getPasswordHash(),
                u.isEnabled(), true, true, true,
                u.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).collect(Collectors.toSet())
        );
    }
}