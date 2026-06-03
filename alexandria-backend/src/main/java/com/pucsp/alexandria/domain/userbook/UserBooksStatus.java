package com.pucsp.alexandria.domain.userbook;

public enum UserBooksStatus {
    TOREAD("toread"),
    READING("reading"),
    DONE("done");

    private final String value;

    UserBooksStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserBooksStatus fromString(String value) {
        for (UserBooksStatus status : UserBooksStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid UserBooksStatus: " + value);
    }
}
