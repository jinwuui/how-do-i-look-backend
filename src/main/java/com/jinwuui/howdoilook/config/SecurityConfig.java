package com.jinwuui.howdoilook.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinwuui.howdoilook.config.filter.EmailPasswordAuthFilter;
import com.jinwuui.howdoilook.config.filter.JwtAuthFilter;
import com.jinwuui.howdoilook.config.handler.Http401Handler;
import com.jinwuui.howdoilook.config.handler.Http403Handler;
import com.jinwuui.howdoilook.config.handler.LoginFailHandler;
import com.jinwuui.howdoilook.config.handler.LoginSuccessHandler;
import com.jinwuui.howdoilook.domain.User;
import com.jinwuui.howdoilook.repository.UserRepository;
import com.jinwuui.howdoilook.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Slf4j
@Configuration
@EnableWebSecurity(debug = false)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/favicon.ico")
                .requestMatchers("/error")
                .requestMatchers(toH2Console());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
//                                .requestMatchers("/api/v1/auth/login").permitAll()
//                                .requestMatchers("/api/v1/auth/signup").permitAll()
//                                .requestMatchers("/api/v1/auth/refresh").permitAll()
                                .anyRequest().permitAll()
                )
                .addFilterBefore(emailPasswordAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .anonymous(
                        anonymous -> anonymous
                                .principal("anonymousUser")
                                .authorities("ROLE_ANONYMOUS")
                )
                .exceptionHandling(e -> {
                    e.accessDeniedHandler(new Http403Handler(objectMapper));
                    e.authenticationEntryPoint(new Http401Handler(objectMapper));
                })
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public EmailPasswordAuthFilter emailPasswordAuthFilter() throws Exception {
        EmailPasswordAuthFilter filter = new EmailPasswordAuthFilter("/api/v1/auth/login", objectMapper);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(new LoginSuccessHandler(objectMapper, jwtUtil()));
        filter.setAuthenticationFailureHandler(new LoginFailHandler(objectMapper));
        return filter;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() throws Exception {
        return new JwtAuthFilter(jwtUtil(), userDetailsService(userRepository));
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService(userRepository));
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username + "을 찾을 수 없습니다."));
            return new UserPrincipal(user);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
}
