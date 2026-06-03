package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toDomain(UserEntity entity) {
    if (entity == null) return null;
    return User.restore(
        entity.getId(),
        entity.getUsername(),
        entity.getFirstName(),
        entity.getLastName(),
        entity.getEmail(),
        entity.getPassword(),
        entity.getCreatedAt()
    );
  }

  public UserEntity toPersistence(User user) {
    if (user == null) return null;
    return new UserEntity(
        user.getId() != null ? user.getId().getValue() : null,
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail().getValue(),
        user.getPassword(),
        user.getCreatedAt()
    );
  }
}
