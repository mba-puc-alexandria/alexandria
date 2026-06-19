package com.pucsp.alexandria.config.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.domain.shared.valueobject.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

        @Test
    void shouldSetAuthenticationForValidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtTokenProvider.validateToken("valid.token.here")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid.token.here")).thenReturn("john_doe");
        when(jwtTokenProvider.getUserIdFromToken("valid.token.here")).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromToken("valid.token.here")).thenReturn("USER");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(1L, user.id());
        assertEquals("john_doe", user.username());
        Collection<? extends GrantedAuthority> authorities =
            SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSetAuthenticationWithAdminRoleAuthority() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer admin.token.here");
        when(jwtTokenProvider.validateToken("admin.token.here")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("admin.token.here")).thenReturn("admin");
        when(jwtTokenProvider.getUserIdFromToken("admin.token.here")).thenReturn(2L);
        when(jwtTokenProvider.getRoleFromToken("admin.token.here")).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Collection<? extends GrantedAuthority> authorities =
            SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSetAuthenticationWithUserRoleAuthority() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer user.token.here");
        when(jwtTokenProvider.validateToken("user.token.here")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("user.token.here")).thenReturn("user");
        when(jwtTokenProvider.getUserIdFromToken("user.token.here")).thenReturn(3L);
        when(jwtTokenProvider.getRoleFromToken("user.token.here")).thenReturn("USER");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Collection<? extends GrantedAuthority> authorities =
            SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationForInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");
        when(jwtTokenProvider.validateToken("invalid.token.here")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenNoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenHeaderDoesNotStartWithBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic token");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationForEmptyBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + "");
        when(jwtTokenProvider.validateToken("")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
