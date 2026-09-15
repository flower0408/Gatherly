package rs.ac.uns.ftn.eventhub.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.ac.uns.ftn.eventhub.service.BannedService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    private final TokenUtils tokenUtils;

    private final BannedService bannedService;

    @Autowired
    public AuthenticationTokenFilter(UserDetailsService userDetailsService, TokenUtils tokenUtils,
                                     BannedService bannedService) {
        this.userDetailsService = userDetailsService;
        this.tokenUtils = tokenUtils;
        this.bannedService = bannedService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest =  request;
        String token = httpServletRequest.getHeader("Authorization");
        if(token != null){
            if(token.startsWith("Bearer ")){
                token = token.substring(7);
            }
        }

        String username = tokenUtils.getUsernameFromToken(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // Blokada se proverava uz svaki zahtev, a ne samo pri prijavi. Inace bi onaj ko je
            // vec prijavljen nastavio da radi sve do isteka tokena.
            if (tokenUtils.validateToken(token, userDetails) && !isBanned(username)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isBanned(String username) {
        return bannedService.isBannedFromSystem(username);
    }
}
