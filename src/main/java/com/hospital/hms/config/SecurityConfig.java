package com.hospital.hms.config;

import com.hospital.hms.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central Spring Security configuration.
 * - BCrypt password hashing
 * - Role based access control (ADMIN, DOCTOR, RECEPTIONIST)
 * - Session based login/logout
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/login", "/register", "/error", "/error/**").permitAll()
                        .requestMatchers("/dashboard/**", "/search/**")
                            .hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                        // Patients: Admin manages fully, Receptionist registers/updates,
                        // Doctor may only view their assigned patients (enforced via @PreAuthorize
                        // + doctor-scoped queries in PatientController).
                        .requestMatchers("/patients/**")
                            .hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                        // Doctor directory (clinical profiles): Admin manages it, Receptionist
                        // needs read access to book appointments. Doctors themselves don't browse it.
                        .requestMatchers("/doctors/**")
                            .hasAnyRole("ADMIN", "RECEPTIONIST")
                        // Appointments: all three roles interact with appointments in some way;
                        // per-action rules (book/cancel/complete/delete) are enforced with @PreAuthorize.
                        .requestMatchers("/appointments/**")
                            .hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                        // Medical records contain clinical diagnoses/prescriptions —
                        // Receptionists have no access to this module at all.
                        .requestMatchers("/medical-records/**")
                            .hasAnyRole("ADMIN", "DOCTOR")
                        // Billing — Doctors never touch invoices.
                        .requestMatchers("/invoices/**")
                            .hasAnyRole("ADMIN", "RECEPTIONIST")
                        // Admin-only modules: user accounts, reports, hospital settings.
                        .requestMatchers("/users/**", "/reports/**", "/settings/**")
                            .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // No custom accessDeniedPage() here: filter-chain-level 403s (a role blocked
                // from an entire URL, e.g. DOCTOR hitting /doctors) are resolved by Spring
                // Boot's default status-code error handling, which finds templates/error/403.html
                // automatically. @PreAuthorize-triggered 403s (action-level, inside an allowed
                // URL) are caught by GlobalExceptionHandler's AccessDeniedException handler,
                // which also renders error/403. Both paths land on the same page.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
