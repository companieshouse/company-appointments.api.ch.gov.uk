
package uk.gov.companieshouse.company_appointments.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import uk.gov.companieshouse.api.filter.CustomCorsFilter;

@Configuration
public class CorsConfiguration {
@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR - Disabling CSRF protection for REST APIs poses no security risk
                .addFilterBefore(new CustomCorsFilter(List.of(HttpMethod.GET.name())), CsrfFilter.class);
        return http.build();
    }

}

