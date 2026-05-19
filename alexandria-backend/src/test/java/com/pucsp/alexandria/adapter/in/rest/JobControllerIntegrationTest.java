package com.pucsp.alexandria.adapter.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pucsp.alexandria.adapter.in.job.SyncGutendexJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
    private SyncGutendexJobService syncGutendexJobService;

    @Test
    void shouldReturn202WhenTriggered() throws Exception {
        mockMvc.perform(post("/api/jobs/sync-gutendex"))
                .andExpect(status().isAccepted());
    }
}
