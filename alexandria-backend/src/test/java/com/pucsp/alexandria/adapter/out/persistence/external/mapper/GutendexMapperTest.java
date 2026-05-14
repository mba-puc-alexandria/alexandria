package com.pucsp.alexandria.adapter.out.persistence.external.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexAuthorResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexBookResponse;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexFormatsResponse;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.List;
import org.junit.jupiter.api.Test;

class GutendexMapperTest {

    private final GutendexMapper mapper = new GutendexMapper();

    @Test
    void shouldMapGutendexResponseToBookData() {
        GutendexFormatsResponse formats = new GutendexFormatsResponse(
                "http://download.epub", "http://cover.jpg");
        GutendexBookResponse response = new GutendexBookResponse(
                100L, "Dom Casmurro",
                List.of(new GutendexAuthorResponse("Machado de Assis", 1839, 1908)),
                formats, List.of("pt"), List.of("Fiction"), 5000);

        BookData bookData = mapper.toBookData(response);

        assertNotNull(bookData);
        assertEquals(100L, bookData.gutendexId());
        assertEquals("Dom Casmurro", bookData.title());
        assertEquals("Machado de Assis", bookData.authors());
        assertEquals(1, bookData.authorNames().size());
        assertEquals("Machado de Assis", bookData.authorNames().get(0));
        assertEquals(1839, bookData.birthYears().get(0));
        assertEquals(1908, bookData.deathYears().get(0));
        assertEquals("http://download.epub", bookData.downloadUrl());
        assertEquals("http://cover.jpg", bookData.coverUrl());
        assertEquals("pt", bookData.languages());
        assertEquals("Fiction", bookData.subjects());
        assertEquals(5000, bookData.downloadCount());
        assertEquals(-1L, bookData.id());
    }

    @Test
    void shouldMapWithMultipleAuthors() {
        GutendexBookResponse response = new GutendexBookResponse(
                200L, "Book", List.of(
                        new GutendexAuthorResponse("Author One", 1900, 1980),
                        new GutendexAuthorResponse("Author Two", 1910, 1990)
                ), null, List.of("en", "pt"), List.of("Fiction", "Drama"), 100);

        BookData bookData = mapper.toBookData(response);

        assertEquals("Author One, Author Two", bookData.authors());
        assertEquals(2, bookData.authorNames().size());
        assertEquals("Author One", bookData.authorNames().get(0));
        assertEquals("Author Two", bookData.authorNames().get(1));
        assertEquals(2, bookData.birthYears().size());
        assertEquals(1900, bookData.birthYears().get(0));
        assertEquals(1910, bookData.birthYears().get(1));
    }

    @Test
    void shouldReturnUnknownForEmptyAuthors() {
        GutendexBookResponse response = new GutendexBookResponse(
                300L, "Book", List.of(), null, List.of("en"), List.of("Fic"), 10);

        BookData bookData = mapper.toBookData(response);

        assertEquals("Unknown", bookData.authors());
        assertTrue(bookData.authorNames().isEmpty());
    }

    @Test
    void shouldHandleNullFormats() {
        GutendexBookResponse response = new GutendexBookResponse(
                400L, "Book",
                List.of(new GutendexAuthorResponse("Author", null, null)),
                null, List.of("en"), List.of("Fic"), 50);

        BookData bookData = mapper.toBookData(response);

        assertNull(bookData.downloadUrl());
        assertNull(bookData.coverUrl());
    }

    @Test
    void shouldFormatAuthorNameWithComma() {
        GutendexBookResponse response = new GutendexBookResponse(
                500L, "Book",
                List.of(new GutendexAuthorResponse("Assis, Machado de", 1839, 1908)),
                null, List.of("en"), List.of("Fic"), 30);

        BookData bookData = mapper.toBookData(response);

        assertEquals("Machado de Assis", bookData.authors());
    }

    @Test
    void shouldHandleNullLanguages() {
        GutendexBookResponse response = new GutendexBookResponse(
                1L, "Title",
                List.of(new GutendexAuthorResponse("Author", null, null)),
                null, null, null, 10);

        BookData bookData = mapper.toBookData(response);

        assertNull(bookData.languages());
    }

    @Test
    void shouldReturnNullWhenResponseIsNull() {
        assertNull(mapper.toBookData(null));
    }

    @Test
    void shouldHandleAuthorNameWithoutComma() {
        GutendexBookResponse response = new GutendexBookResponse(
                1L, "Book",
                List.of(new GutendexAuthorResponse("  Spaces Around  ", null, null)),
                null, List.of("en"), List.of(), 0);

        BookData bookData = mapper.toBookData(response);

        assertEquals("Spaces Around", bookData.authors());
    }
}
