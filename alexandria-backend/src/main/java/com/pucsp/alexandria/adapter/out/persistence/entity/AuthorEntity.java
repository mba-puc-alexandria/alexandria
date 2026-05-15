package com.pucsp.alexandria.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
public class AuthorEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(name = "birth_year")
  private Integer birthYear;

  @Column(name = "death_year")
  private Integer deathYear;

  @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
  private Set<BookEntity> books = new HashSet<>();

  public AuthorEntity() {
  }

  public AuthorEntity(Long id, String name, Integer birthYear, Integer deathYear) {
    this.id = id;
    this.name = name;
    this.birthYear = birthYear;
    this.deathYear = deathYear;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getBirthYear() {
    return birthYear;
  }

  public void setBirthYear(Integer birthYear) {
    this.birthYear = birthYear;
  }

  public Integer getDeathYear() {
    return deathYear;
  }

  public void setDeathYear(Integer deathYear) {
    this.deathYear = deathYear;
  }

  public Set<BookEntity> getBooks() {
    return books;
  }

  public void setBooks(Set<BookEntity> books) {
    this.books = books;
  }
}
