package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.GutendexClient;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexBookResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexFormatsResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexSearchResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.mapper.GutendexMapper;
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
    void shouldSearchByTitle() {
        GutendexSearchResponse response = new GutendexSearchResponse(1,
                List.of(new GutendexBookResponse(100L, "Title", List.of(), null, List.of(), List.of(), 0)));
        when(gutendexClient.searchByTitle("Dom")).thenReturn(response);
        BookData bookData = new BookData(-1L, 100L, "Title", "Unknown", null, null, null, null, 0);
        when(gutendexMapper.toBookData(any())).thenReturn(bookData);

        List<BookData> result = bookApiClient.searchByTitle("Dom");

        assertEquals(1, result.size());
        assertEquals("Title", result.get(0).title());
    }

    @Test
    void shouldReturnEmptyListWhenSearchResponseIsNull() {
        when(gutendexClient.searchByTitle("")).thenReturn(null);

        List<BookData> result = bookApiClient.searchByTitle("");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenSearchResultsAreNull() {
        when(gutendexClient.searchByTitle("none")).thenReturn(new GutendexSearchResponse(0, null));

        List<BookData> result = bookApiClient.searchByTitle("none");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenSearchResultsAreEmpty() {
        when(gutendexClient.searchByTitle("empty")).thenReturn(new GutendexSearchResponse(0, List.of()));

        List<BookData> result = bookApiClient.searchByTitle("empty");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetPage() {
        GutendexSearchResponse response = new GutendexSearchResponse(2,
                List.of(
                    new GutendexBookResponse(100L, "B1", List.of(), null, List.of(), List.of(), 0),
                    new GutendexBookResponse(200L, "B2", List.of(), null, List.of(), List.of(), 0)
                ));
        when(gutendexClient.getPage(1)).thenReturn(response);
        when(gutendexMapper.toBookData(any())).thenReturn(
                new BookData(-1L, 100L, "B1", "Unknown", null, null, null, null, 0),
                new BookData(-1L, 200L, "B2", "Unknown", null, null, null, null, 0)
        );

        List<BookData> result = bookApiClient.getPage(1);

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenGetPageResponseIsNull() {
        when(gutendexClient.getPage(99)).thenReturn(null);

        List<BookData> result = bookApiClient.getPage(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenGetPageResultsAreEmpty() {
        when(gutendexClient.getPage(1)).thenReturn(new GutendexSearchResponse(0, List.of()));

        List<BookData> result = bookApiClient.getPage(1);

        assertTrue(result.isEmpty());
    }
}
