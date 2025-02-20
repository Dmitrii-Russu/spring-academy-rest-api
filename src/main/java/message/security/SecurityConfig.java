package message.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class to set up the application's security settings.
 * This configuration enables web security, configures user authentication,
 * and sets up HTTP request authorization rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the {@link SecurityFilterChain} for HTTP security.
     * Defines URL authorization rules, login settings, and CSRF and CORS configurations.
     *
     * @param http the {@link HttpSecurity} instance for configuring security settings
     * @return a configured {@link SecurityFilterChain} bean
     * @throws Exception if any security-related exception occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/messages/**")
                        .hasRole("USER")  // Restricts access to /messages/** to users with 'USER' role
                        .anyRequest().authenticated())  // Requires authentication for any other request
                .httpBasic(Customizer.withDefaults())  // Configures basic HTTP authentication
                .formLogin(Customizer.withDefaults())  // Configures form-based login
                .csrf(AbstractHttpConfigurer::disable)  // Disables CSRF protection (not recommended for production)
                .cors(AbstractHttpConfigurer::disable);  // Disables CORS (Cross-Origin Resource Sharing)

        return http.build();  // Builds and returns the configured SecurityFilterChain
    }

    /**
     * Provides an in-memory {@link UserDetailsService} for user authentication.
     * Defines users with usernames, passwords, and roles.
     *
     * @param encoder the {@link PasswordEncoder} used to encode user passwords
     * @return a configured {@link UserDetailsService} bean
     */
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {

        UserDetails jack = User
                .withUsername("jack")
                .password(encoder.encode("asd"))
                .roles("USER")  // 'jack' user has the 'USER' role
                .build();

        UserDetails ann = User
                .withUsername("ann")
                .password(encoder.encode("zxc"))
                .roles("USER")  // 'ann' user has the 'USER' role
                .build();

        UserDetails hank = User
                .withUsername("hank")
                .password(encoder.encode("qwe"))
                .roles("NON-USER")  // 'hank' user has the 'NON-USER' role
                .build();

        return new InMemoryUserDetailsManager(jack, ann, hank);  // Returns the in-memory user details manager
    }

    /**
     * Provides a {@link PasswordEncoder} bean for encoding passwords.
     * Uses {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()} to create a delegating encoder.
     *
     * @return a configured {@link PasswordEncoder} bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();  // Returns a delegating password encoder
    }
}
