package rs.ac.uns.ftn.eventhub.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import rs.ac.uns.ftn.eventhub.service.implementation.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }


    private TokenUtils tokenUtils;

    @Autowired
    public void setTokenUtils(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    public void setRestAuthenticationEntryPoint(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Server ne cuva sesiju: identitet korisnika stize u tokenu uz svaki zahtev
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Neautentifikovan zahtev dobija 401, bez preusmeravanja na stranicu za prijavu
        http.exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint);
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/users/signup").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/verify").permitAll()

                // gost moze da pregleda javne dogadjaje i zajednice bez prijave na sistem
                .antMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/communities/**").permitAll()
                // slike moraju biti dostupne svima, inace se ne prikazuju na javnim stranicama
                .antMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/images/profile/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/reactions/event/**", "/api/reactions/comment/**").permitAll()

                .anyRequest().authenticated().and()
                .cors().and()

                // Token se proverava pre nego sto zahtev dodje do BasicAuthenticationFilter-a,
                // jer se prijava ne radi korisnickim imenom i lozinkom nego tokenom
                .addFilterBefore(new AuthenticationTokenFilter(userDetailsService(), tokenUtils), BasicAuthenticationFilter.class);

        // CSRF zastita nije potrebna: token se salje u zaglavlju Authorization, ne u kolacicu,
        // pa ga pregledac ne prilaze sam uz zahtev sa tudje stranice
        http.csrf().disable();


        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Putanje navedene ovde uopste ne prolaze kroz bezbednosni lanac, pa u njima
        // nema podatka o prijavljenom korisniku
        return (web) -> web.ignoring().antMatchers(HttpMethod.POST, "/api/users/login")

                .antMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "favicon.ico",
                        "/**/*.html", "/**/*.css", "/**/*.js");

    }
}
