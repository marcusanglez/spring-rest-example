package com.spring.example.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers("/cashcards/**")
                .hasRole("CARD-OWNER")// enable RBAC: Replace the .authenticated() with this line
                .and()
                .csrf().disable()
                .httpBasic();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService testerOnlyUsers(PasswordEncoder passwordEncoder){

        User.UserBuilder users = User.builder();

        UserDetails marco = users
                .username("marco")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER")
                .build();

        UserDetails sergio = users
                .username("sergio")
                .password(passwordEncoder.encode("53r610rocks"))
                .roles("CARD-OWNER")
                .build();

        UserDetails mangoOwnsNoCards = users
                .username("mango-owns-no-cards")
                .password(passwordEncoder.encode("dontcare"))
                .roles("NO-OWNER")
                .build();

        return new InMemoryUserDetailsManager(marco, sergio, mangoOwnsNoCards);
    }
}