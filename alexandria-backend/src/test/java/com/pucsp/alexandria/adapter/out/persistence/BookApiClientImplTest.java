package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.GutendexClient;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexAuthorResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexBookResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexSearchResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.mapper.GutendexMapper;
import com.pucsp.alexandria.domain.book.external.AuthorData;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookApiClientImplTest {

    @Mock
    private GutendexClient gutendexClient;

    @Mock
    private GutendexMapper gutendexMapper;

    @InjectMocks
    private BookApiClientImpl bookApiClient;

    @Test
    void shouldReturnBooksWhenPageFound() {
        GutendexSearchResponse response = new GutendexSearchResponse(1, List.of(
                new GutendexBookResponse(100L, "Dom Casmurro",
                        List.of(new GutendexAuthorResponse("Machado de Assis", null, null)),
                        null, List.of("pt"), List.of("Fiction"), 5000)
        ));
        when(gutendexClient.getPage(1)).thenReturn(response);
        BookData bookData = new BookData(-1L, 100L, "Dom Casmurro", "Machado de Assis",
                List.of(new AuthorData("Machado de Assis", null, null)),
                null, null, "pt", "Fiction", 5000);
        when(gutendexMapper.toBookData(any())).thenReturn(bookData);

        List<BookData> result = bookApiClient.getPage(1);

        assertEquals(1, result.size());
        assertEquals("Dom Casmurro", result.get(0).title());
    }

    @Test
    void shouldReturnEmptyListWhenPageNotFound() {
        when(gutendexClient.getPage(99)).thenReturn(null);

        List<BookData> result = bookApiClient.getPage(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenResponseHasNoResults() {
        GutendexSearchResponse response = new GutendexSearchResponse(0, List.of());
        when(gutendexClient.getPage(1)).thenReturn(response);

        List<BookData> result = bookApiClient.getPage(1);

        assertTrue(result.isEmpty());
    }
}
