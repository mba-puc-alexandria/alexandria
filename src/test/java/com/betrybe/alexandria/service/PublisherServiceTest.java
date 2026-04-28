package com.betrybe.alexandria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.betrybe.alexandria.controller.dto.PublisherCreationDto;
import com.betrybe.alexandria.entity.Book;
import com.betrybe.alexandria.entity.Publisher;
import com.betrybe.alexandria.exception.EmptyPublisherListException;
import com.betrybe.alexandria.exception.PublisherNotFoundException;
import com.betrybe.alexandria.repository.PublisherRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("PublisherService Tests")
@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

  @Mock
  private PublisherRepository publisherRepository;

  @InjectMocks
  private PublisherService publisherService;

  @Test
  @DisplayName("Should find publisher by id successfully")
  void testFindByIdSuccess() {
    // Arrange
    Long publisherId = 1L;
    Publisher publisher = new Publisher("Penguin", "London");
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));

    // Act
    Publisher result = publisherService.findById(publisherId);

    // Assert
    assertNotNull(result);
    assertEquals("Penguin", result.getName());
    assertEquals("London", result.getAddress());
    verify(publisherRepository).findById(publisherId);
  }

  @Test
  @DisplayName("Should throw PublisherNotFoundException when publisher not found")
  void testFindByIdNotFound() {
    // Arrange
    Long publisherId = 999L;
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(PublisherNotFoundException.class, () -> publisherService.findById(publisherId));
    verify(publisherRepository).findById(publisherId);
  }

  @Test
  @DisplayName("Should find all publishers with pagination")
  void testFindAllSuccess() {
    // Arrange
    Publisher publisher1 = new Publisher("Penguin", "London");
    Publisher publisher2 = new Publisher("HarperCollins", "New York");
    Pageable pageable = PageRequest.of(0, 10);
    Page<Publisher> publisherPage = new PageImpl<>(List.of(publisher1, publisher2), pageable, 2);
    when(publisherRepository.findAll(pageable)).thenReturn(publisherPage);

    // Act
    Page<Publisher> result = publisherService.findAll(pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(publisherRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should create publisher successfully")
  void testCreateSuccess() {
    // Arrange
    Publisher publisher = new Publisher("Oxford", "Oxford, UK");
    when(publisherRepository.save(publisher)).thenReturn(publisher);

    // Act
    Publisher result = publisherService.create(publisher);

    // Assert
    assertNotNull(result);
    assertEquals("Oxford", result.getName());
    assertEquals("Oxford, UK", result.getAddress());
    verify(publisherRepository).save(publisher);
  }

  @Test
  @DisplayName("Should create batch of publishers successfully")
  void testCreateBatchSuccess() {
    // Arrange
    PublisherCreationDto dto1 = new PublisherCreationDto("Publisher1", "Address1");
    PublisherCreationDto dto2 = new PublisherCreationDto("Publisher2", "Address2");
    List<PublisherCreationDto> publishersDto = List.of(dto1, dto2);

    Publisher publisher1 = new Publisher("Publisher1", "Address1");
    Publisher publisher2 = new Publisher("Publisher2", "Address2");
    when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher1, publisher2);

    // Act
    List<Publisher> result = publisherService.createBatch(publishersDto);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(publisherRepository, times(2)).save(any(Publisher.class));
  }

  @Test
  @DisplayName("Should throw EmptyPublisherListException when list is empty")
  void testCreateBatchEmptyList() {
    // Arrange
    List<PublisherCreationDto> emptyList = List.of();

    // Act & Assert
    assertThrows(EmptyPublisherListException.class, () -> publisherService.createBatch(emptyList));
    verify(publisherRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw EmptyPublisherListException when list is null")
  void testCreateBatchNullList() {
    // Arrange & Act & Assert
    assertThrows(EmptyPublisherListException.class, () -> publisherService.createBatch(null));
    verify(publisherRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update publisher successfully")
  void testUpdateSuccess() {
    // Arrange
    Long publisherId = 1L;
    Publisher existingPublisher = new Publisher("Old Publisher", "Old Address");
    Publisher updateData = new Publisher("New Publisher", "New Address");

    when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(existingPublisher));
    when(publisherRepository.save(existingPublisher)).thenReturn(existingPublisher);

    // Act
    Publisher result = publisherService.update(publisherId, updateData);

    // Assert
    assertNotNull(result);
    assertEquals("New Publisher", result.getName());
    assertEquals("New Address", result.getAddress());
    verify(publisherRepository).findById(publisherId);
    verify(publisherRepository).save(existingPublisher);
  }

  @Test
  @DisplayName("Should throw PublisherNotFoundException when updating non-existent publisher")
  void testUpdateNotFound() {
    // Arrange
    Long publisherId = 999L;
    Publisher updateData = new Publisher("Publisher", "Address");
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(PublisherNotFoundException.class,
        () -> publisherService.update(publisherId, updateData));
    verify(publisherRepository).findById(publisherId);
    verify(publisherRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete publisher successfully")
  void testDeleteByIdSuccess() {
    // Arrange
    Long publisherId = 1L;
    Publisher publisher = new Publisher("Publisher", "Address");
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));

    // Act
    publisherService.deleteById(publisherId);

    // Assert
    verify(publisherRepository).findById(publisherId);
    verify(publisherRepository).delete(publisher);
  }

  @Test
  @DisplayName("Should throw PublisherNotFoundException when deleting non-existent publisher")
  void testDeleteByIdNotFound() {
    // Arrange
    Long publisherId = 999L;
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(PublisherNotFoundException.class, () -> publisherService.deleteById(publisherId));
    verify(publisherRepository).findById(publisherId);
    verify(publisherRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should get publisher books successfully")
  void testGetPublisherBooksSuccess() {
    // Arrange
    Long publisherId = 1L;
    Publisher publisher = new Publisher("Publisher", "Address");
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));

    // Act
    List<Book> result = publisherService.getPublisherBooks(publisherId);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.size()); // Publisher has no books initially
    verify(publisherRepository).findById(publisherId);
  }

  @Test
  @DisplayName("Should return empty list when publisher has no books")
  void testGetPublisherBooksEmpty() {
    // Arrange
    Long publisherId = 1L;
    Publisher publisher = new Publisher("Publisher", "Address");
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));

    // Act
    List<Book> result = publisherService.getPublisherBooks(publisherId);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(publisherRepository).findById(publisherId);
  }

  @Test
  @DisplayName("Should throw PublisherNotFoundException when getting books of non-existent publisher")
  void testGetPublisherBooksNotFound() {
    // Arrange
    Long publisherId = 999L;
    when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(PublisherNotFoundException.class,
        () -> publisherService.getPublisherBooks(publisherId));
    verify(publisherRepository).findById(publisherId);
  }
}

