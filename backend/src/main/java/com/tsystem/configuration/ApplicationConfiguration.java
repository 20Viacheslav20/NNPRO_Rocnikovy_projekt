package com.tsystem.configuration;


import com.tsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    /** Достаём пользователя по username ИЛИ email */
    @Bean
    public UserDetailsService userDetailsService() {
        return usernameOrEmail -> userRepository.findByUsername(usernameOrEmail)
                .<UserDetails>map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getUsername())
                        .password(u.getPassword())
                        .authorities("USER")
                        .accountLocked(false).disabled(false).build())
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .<UserDetails>map(u -> org.springframework.security.core.userdetails.User
                                .withUsername(u.getUsername())
                                .password(u.getPassword())
                                .authorities("USER")
                                .accountLocked(false).disabled(false).build())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found")));
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}