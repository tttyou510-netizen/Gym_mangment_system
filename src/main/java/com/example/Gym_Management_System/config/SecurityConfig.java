package com.example.Gym_Management_System.config;

import com.example.Gym_Management_System.security.JsonSecurityErrorHandler;
import com.example.Gym_Management_System.security.JwtAuthenticationFilter;
import com.example.Gym_Management_System.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   UserDetailsService userDetailsService,
                                                   JsonSecurityErrorHandler errorHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // عام (من غير توكن)
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // الخطط: القراءة لأي مستخدم مسجّل، والتعديل للأدمن
                        .requestMatchers(HttpMethod.GET, "/api/v1/plans/**").authenticated()
                        .requestMatchers("/api/v1/plans/**").hasRole("ADMIN")

                        // الأعضاء والاشتراكات: أدمن واستقبال
                        .requestMatchers("/api/v1/members/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers("/api/v1/subscriptions/**").hasAnyRole("ADMIN", "RECEPTIONIST")

                        // أي حاجة تانية لازم تسجيل دخول
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}