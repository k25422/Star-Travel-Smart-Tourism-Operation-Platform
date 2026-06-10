package com.example.tourism.security;

import java.util.Collections;
import com.example.tourism.domain.AccountUser;
import com.example.tourism.repository.AccountUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountUserRepository userRepository;

    public CustomUserDetailsService(AccountUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountUser user = userRepository.findByUsername(username)
                .orElseThrow(new java.util.function.Supplier<UsernameNotFoundException>() { public UsernameNotFoundException get() { return new UsernameNotFoundException("User not found"); } });
        return new User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
