package com.pucsp.alexandria.adapter.in.rest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pucsp.alexandria.adapter.in.job.SyncGutendexJobService;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SyncGutendexJobService syncGutendexJobService;

    private final String ADMIN_TOKEN = "Bearer admin-token";
    private final String USER_TOKEN = "Bearer user-token";

    @Test
    void shouldSyncAsAdmin() throws Exception {
        when(jwtTokenProvider.validateToken("admin-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("admin-token")).thenReturn("admin");
        when(jwtTokenProvider.getUserIdFromToken("admin-token")).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromToken("admin-token")).thenReturn("ADMIN");

        mockMvc.perform(post("/api/jobs/sync-gutendex")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldNotSyncAsUser() throws Exception {
        when(jwtTokenProvider.validateToken("user-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("user-token")).thenReturn("user");
        when(jwtTokenProvider.getUserIdFromToken("user-token")).thenReturn(2L);
        when(jwtTokenProvider.getRoleFromToken("user-token")).thenReturn("USER");

        mockMvc.perform(post("/api/jobs/sync-gutendex")
                        .header("Authorization", USER_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotSyncWithoutToken() throws Exception {
        mockMvc.perform(post("/api/jobs/sync-gutendex"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSyncAsAdminWithPage() throws Exception {
        when(jwtTokenProvider.validateToken("admin-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("admin-token")).thenReturn("admin");
        when(jwtTokenProvider.getUserIdFromToken("admin-token")).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromToken("admin-token")).thenReturn("ADMIN");

        mockMvc.perform(post("/api/jobs/sync-gutendex")
                        .header("Authorization", ADMIN_TOKEN)
                        .param("page", "582"))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturn202WhenTriggeredWithPage() throws Exception {
        when(jwtTokenProvider.validateToken("admin-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("admin-token")).thenReturn("admin");
        when(jwtTokenProvider.getUserIdFromToken("admin-token")).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromToken("admin-token")).thenReturn("ADMIN");

        mockMvc.perform(post("/api/jobs/sync-gutendex")
                        .header("Authorization", ADMIN_TOKEN)
                        .param("page", "582"))
                .andExpect(status().isAccepted());
    }
}

