package com.pucsp.alexandria.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.application.userbooks.AddUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.RemoveUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.UpdateUserBooksUseCase;
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
class UserBooksControllerIntegrationTest {

        @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddUserBooksUseCase addUserBooksUseCase;

    @MockitoBean
    private ListUserBooksUseCase listUserBooksUseCase;

    @MockitoBean
    private UpdateUserBooksUseCase updateUserBooksUseCase;

    @MockitoBean
    private RemoveUserBooksUseCase removeUserBooksUseCase;

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }
}

