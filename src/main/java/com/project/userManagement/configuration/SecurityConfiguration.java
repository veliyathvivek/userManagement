package com.project.userManagement.configuration;

import com.project.userManagement.constants.SecurityConstant;
import com.project.userManagement.filters.JwtAccessDeniedHandler;
import com.project.userManagement.filters.JwtAuthenticationEntryPoint;
import com.project.userManagement.filters.JwtAuthorizationFilter;
import com.project.userManagement.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((request) -> request
                        .requestMatchers(SecurityConstant.PUBLIC_URLS).permitAll())
                .authorizeHttpRequests((request) -> request
                        .requestMatchers("/user/find/**", "/user/findAll/**").hasAuthority("user:read"))
                .authorizeHttpRequests((request) -> request
                        .requestMatchers("/user/updateUser/**", "/user/updateProfileImg/**").hasAuthority("user:update"))
                .authorizeHttpRequests((request) -> request
                        .requestMatchers("/user/addNewUser/**").hasAuthority("user:create"))
                .authorizeHttpRequests((request) -> request
                        .requestMatchers("/user/delete/**").hasAuthority("user:delete"))
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .httpBasic((basic) -> basic
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}