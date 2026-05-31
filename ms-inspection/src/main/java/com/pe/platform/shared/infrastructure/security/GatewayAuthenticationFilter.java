package com.pe.platform.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que lee la identidad inyectada por el API Gateway en los headers
 * (X-User-Id, X-User-Name, X-User-Role) y arma el contexto de Spring Security
 *
 * @PreAuthorize("hasAuthority('ROLE_SELLER')") sigue funcionando
 *
 * Seguridad: el unico camino hacia el microservicio es a traves del Gateway
 */
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String role = request.getHeader("X-User-Role");

        if (userId != null && role != null) {
            var currentUser = new CurrentUser(Long.valueOf(userId), username, role);
            var authority = new SimpleGrantedAuthority(role);

            var authentication = new UsernamePasswordAuthenticationToken(
                    currentUser, null, List.of(authority));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
