package no.fintlabs;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final ProviderProperties properties;

    public SecurityConfiguration(ProviderProperties properties) {
        this.properties = properties;
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/**").hasAnyAuthority(getScopeAuthority())
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(withDefaults())
                );
        return http.build();
    }

    private String getScopeAuthority() {
        return String.format("SCOPE_%s", properties.getScope());
    }

}