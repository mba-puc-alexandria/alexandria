package com.pucsp.alexandria.adapter.out.persistence.external.gutendex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexBookResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexSearchResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class GutendexClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GutendexClient gutendexClient;

    @Test
    void shouldSearchByTitle() {
        GutendexSearchResponse expectedResponse = new GutendexSearchResponse(1, List.of());
        when(restTemplate.getForObject(anyString(), eq(GutendexSearchResponse.class)))
                .thenReturn(expectedResponse);

        GutendexSearchResponse response = gutendexClient.searchByTitle("Dom Casmurro");

        assertNotNull(response);
        assertEquals(1, response.count());
    }

    @Test
    void shouldGetPage() {
        GutendexSearchResponse expectedResponse = new GutendexSearchResponse(10, List.of());
        when(restTemplate.getForObject(anyString(), eq(GutendexSearchResponse.class)))
                .thenReturn(expectedResponse);

        GutendexSearchResponse response = gutendexClient.getPage(2);

        assertNotNull(response);
        assertEquals(10, response.count());
    }

    @Test
    void shouldReturnNullWhenApiReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(GutendexSearchResponse.class)))
                .thenReturn(null);

        assertNull(gutendexClient.searchByTitle("unknown"));
    }
}
