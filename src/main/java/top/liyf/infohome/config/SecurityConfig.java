package top.liyf.infohome.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author liyf
 * Created in 2021-06-26
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    final CustomSavedRequestAwareAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomSavedRequestAwareAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().authorizeRequests()
                .antMatchers("/error", "/movie/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .oauth2Login().successHandler(successHandler);
    }
}
