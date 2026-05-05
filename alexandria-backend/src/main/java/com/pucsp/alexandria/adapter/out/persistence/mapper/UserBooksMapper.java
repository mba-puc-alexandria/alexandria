package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import org.springframework.stereotype.Component;

@Component
public class UserBooksMapper {

  public UserBooks toDomain(UserBooksEntity entity) {
    if (entity == null) return null;
    return UserBooks.restore(
        entity.getId(),
        entity.getUser().getId(),
        entity.getBook().getId(),
        entity.getStatus(),
        entity.getProgress(),
        entity.getRating(),
        entity.getCreatedAt()
    );
  }

  public UserBooksEntity toPersistence(UserBooks userBooks, UserEntity userEntity, BookEntity bookEntity) {
    if (userBooks == null) return null;
    return new UserBooksEntity(
        userBooks.getId(),
        userEntity,
        bookEntity,
        userBooks.getStatus().getValue(),
        userBooks.getProgress(),
        userBooks.getRating(),
        userBooks.getCreatedAt()
    );
  }
}
