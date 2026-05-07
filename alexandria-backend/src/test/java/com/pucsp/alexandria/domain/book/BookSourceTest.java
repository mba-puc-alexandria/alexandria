package com.pucsp.alexandria.domain.book;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BookSourceTest {

    @Test
    void localShouldHaveCorrectLabel() {
        assertEquals("Local", BookSource.LOCAL.getLabel());
    }

    @Test
    void gutendexShouldHaveCorrectLabel() {
        assertEquals("Gutendex", BookSource.GUTENDEX.getLabel());
    }

    @Test
    void shouldHaveTwoValues() {
        assertEquals(2, BookSource.values().length);
    }
}
