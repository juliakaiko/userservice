package com.mymicroservice.userservice.unit.filter;

import com.mymicroservice.userservice.filter.GatewayAuthFilter;
import com.mymicroservice.userservice.util.JwtTestHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static com.mymicroservice.userservice.util.CommonConstants.GATEWAY_SERVICE_NAME;
import static com.mymicroservice.userservice.util.CommonConstants.INTERNAL_CALL_HEADER;
import static com.mymicroservice.userservice.util.CommonConstants.SOURCE_SERVICE_HEADER;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GatewayAuthFilterTest {

    private GatewayAuthFilter gatewayAuthFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        gatewayAuthFilter = new GatewayAuthFilter();
        ReflectionTestUtils.setField(gatewayAuthFilter, "publicEndpoints", List.of("/actuator/**"));
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenGatewayCallWithValidJwt() throws ServletException, IOException {
        request.addHeader(INTERNAL_CALL_HEADER, "true");
        request.addHeader(SOURCE_SERVICE_HEADER, GATEWAY_SERVICE_NAME);
        request.addHeader("Authorization", JwtTestHelper.createBearerToken(USER_EMAIL, List.of("USER", "ADMIN")));

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(USER_EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldClearContext_WhenInternalServiceCall() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", null, List.of()));

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenGatewayCallWithoutBearerToken() throws ServletException, IOException {
        request.addHeader(INTERNAL_CALL_HEADER, "true");
        request.addHeader(SOURCE_SERVICE_HEADER, GATEWAY_SERVICE_NAME);

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenBearerTokenIsInvalid() throws ServletException, IOException {
        request.addHeader(INTERNAL_CALL_HEADER, "true");
        request.addHeader(SOURCE_SERVICE_HEADER, GATEWAY_SERVICE_NAME);
        request.addHeader("Authorization", JwtTestHelper.createInvalidBearerToken());

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenInternalCallHeaderIsFalse() throws ServletException, IOException {
        request.addHeader(INTERNAL_CALL_HEADER, "false");
        request.addHeader(SOURCE_SERVICE_HEADER, GATEWAY_SERVICE_NAME);
        request.addHeader("Authorization", JwtTestHelper.createBearerToken(USER_EMAIL, List.of("USER")));

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldClearContextAndContinue_WhenJwtPayloadIsMalformed() throws ServletException, IOException {
        request.addHeader(INTERNAL_CALL_HEADER, "true");
        request.addHeader(SOURCE_SERVICE_HEADER, GATEWAY_SERVICE_NAME);
        request.addHeader("Authorization", "Bearer header.!!!invalid-base64!!!.signature");

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSetAuthenticationWithEmptyRoles_WhenRolesClaimIsMissing() throws ServletException, IOException {
        request.addHeader(INTERNAL_CALL_HEADER, "true");
        request.addHeader(SOURCE_SERVICE_HEADER, GATEWAY_SERVICE_NAME);
        request.addHeader("Authorization", JwtTestHelper.createBearerToken(USER_EMAIL, List.of()));

        gatewayAuthFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(USER_EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
        verify(filterChain).doFilter(request, response);
    }
}
