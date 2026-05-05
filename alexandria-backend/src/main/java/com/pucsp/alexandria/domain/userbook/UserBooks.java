package com.pucsp.alexandria.domain.userbook;

import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserBooks {

    private final Long id;
    private final UserId userId;
    private final Long bookId;
    private final UserBooksStatus status;
    private final Integer progress;
    private final Integer rating;
    private final LocalDateTime createdAt;

    private UserBooks(Long id, UserId userId, Long bookId, UserBooksStatus status,
                      Integer progress, Integer rating, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.status = status;
        this.progress = progress;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public static UserBooks create(UserId userId, Long bookId, UserBooksStatus status) {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(bookId, "bookId is required");
        UserBooksStatus finalStatus = status != null ? status : UserBooksStatus.TOREAD;
        validateByStatus(finalStatus, null, null);
        return new UserBooks(null, userId, bookId, finalStatus, null, null, LocalDateTime.now());
    }

    public static UserBooks restore(Long id, Long userId, Long bookId, String status,
                                    Integer progress, Integer rating, LocalDateTime createdAt) {
        UserId userIdVO = UserId.from(userId);
        UserBooksStatus statusEnum = UserBooksStatus.fromString(status);
        validateByStatus(statusEnum, progress, rating);
        return new UserBooks(id, userIdVO, bookId, statusEnum, progress, rating, createdAt);
    }

    public UserBooks updateWith(UserBooksStatus newStatus, Integer newProgress, Integer newRating) {
        UserBooksStatus finalStatus = newStatus != null ? newStatus : this.status;
        Integer finalProgress = newProgress != null ? newProgress : this.progress;
        Integer finalRating = newRating != null ? newRating : this.rating;
        validateByStatus(finalStatus, finalProgress, finalRating);
        return new UserBooks(this.id, this.userId, this.bookId, finalStatus, finalProgress, finalRating, this.createdAt);
    }

    private static void validateByStatus(UserBooksStatus status, Integer progress, Integer rating) {
        switch (status) {
            case TOREAD:
                if (progress != null) throw new InvalidUserBooksException("Progress must be null for TOREAD status");
                if (rating != null) throw new InvalidUserBooksException("Rating must be null for TOREAD status");
                break;
            case READING:
                if (progress == null || progress < 0 || progress > 100)
                    throw new InvalidUserBooksException("Progress (0-100) is required for READING status");
                if (rating != null)
                    throw new InvalidUserBooksException("Rating must be null for READING status");
                break;
            case DONE:
                if (progress != null)
                    throw new InvalidUserBooksException("Progress must be null for DONE status");
                if (rating == null || rating < 0 || rating > 5)
                    throw new InvalidUserBooksException("Rating (0-5) is required for DONE status");
                break;
        }
    }

    public Long getId() { return id; }
    public UserId getUserId() { return userId; }
    public Long getBookId() { return bookId; }
    public UserBooksStatus getStatus() { return status; }
    public Integer getProgress() { return progress; }
    public Integer getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserBooks that = (UserBooks) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
