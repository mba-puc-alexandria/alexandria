package com.betrybe.alexandria.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.betrybe.alexandria.controller.dto.PublisherCreationDto;
import com.betrybe.alexandria.entity.Publisher;
import com.betrybe.alexandria.service.PublisherService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("PublisherController Tests")
@ExtendWith(MockitoExtension.class)
class PublisherControllerTest {

  @Mock
  private PublisherService publisherService;

  @InjectMocks
  private PublisherController publisherController;

  @Test
  @DisplayName("Should call publisherService.findById when getPublisherById is called")
  void testGetPublisherByIdCallsService() {
    // Arrange
    Long publisherId = 1L;
    Publisher publisher = new Publisher("Name", "Address");
    when(publisherService.findById(publisherId)).thenReturn(publisher);

    // Act
    publisherController.getPublisherById(publisherId);

    // Assert
    verify(publisherService).findById(publisherId);
  }

  @Test
  @DisplayName("Should call publisherService.findAll when getAllPublishers is called")
  void testGetAllPublishersCallsService() {
    // Arrange
    var pageable = PageRequest.of(0, 10);
    var emptyPage = new PageImpl<Publisher>(List.of(), pageable, 0);
    when(publisherService.findAll(pageable)).thenReturn(emptyPage);

    // Act
    publisherController.getAllPublishers(pageable);

    // Assert
    verify(publisherService).findAll(pageable);
  }

  @Test
  @DisplayName("Should call publisherService.create when createPublisher is called")
  void testCreatePublisherCallsService() {
    // Arrange
    var dto = new PublisherCreationDto("Name", "Address");
    Publisher publisher = new Publisher("Name", "Address");
    when(publisherService.create(any(Publisher.class))).thenReturn(publisher);

    // Act
    publisherController.createPublisher(dto);

    // Assert
    verify(publisherService).create(any(Publisher.class));
  }

  @Test
  @DisplayName("Should call publisherService.createBatch when createPublishers is called")
  void testCreatePublishersCallsService() {
    // Arrange
    var dtos = List.of(
        new PublisherCreationDto("Pub1", "Addr1"),
        new PublisherCreationDto("Pub2", "Addr2")
    );
    when(publisherService.createBatch(any(List.class))).thenReturn(List.of(
        new Publisher("Pub1", "Addr1"),
        new Publisher("Pub2", "Addr2")
    ));

    // Act
    publisherController.createPublishers(dtos);

    // Assert
    verify(publisherService).createBatch(any(List.class));
  }

  @Test
  @DisplayName("Should call publisherService.update when updatePublisher is called")
  void testUpdatePublisherCallsService() {
    // Arrange
    Long publisherId = 1L;
    var dto = new PublisherCreationDto("Updated", "Address");
    Publisher publisher = new Publisher("Updated", "Address");
    when(publisherService.update(eq(publisherId), any(Publisher.class))).thenReturn(publisher);

    // Act
    publisherController.updatePublisher(publisherId, dto);

    // Assert
    verify(publisherService).update(eq(publisherId), any(Publisher.class));
  }

  @Test
  @DisplayName("Should call publisherService.deleteById when deletePublisherById is called")
  void testDeletePublisherByIdCallsService() {
    // Arrange
    Long publisherId = 1L;
    doNothing().when(publisherService).deleteById(publisherId);

    // Act
    publisherController.deletePublisherById(publisherId);

    // Assert
    verify(publisherService).deleteById(publisherId);
  }

  @Test
  @DisplayName("Should call publisherService.getPublisherBooks when getPublisherBooks is called")
  void testGetPublisherBooksCallsService() {
    // Arrange
    Long publisherId = 1L;
    var books = List.of(new com.betrybe.alexandria.entity.Book("Book1", "Genre1"));
    when(publisherService.getPublisherBooks(publisherId)).thenReturn(books);

    // Act
    publisherController.getPublisherBooks(publisherId);

    // Assert
    verify(publisherService).getPublisherBooks(publisherId);
  }
}

