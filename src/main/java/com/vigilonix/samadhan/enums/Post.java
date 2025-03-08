package com.vigilonix.samadhan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Post {
    ASSOCIATE(50),
    OFFICER(100),
    MANAGER(600);
    private final int level;
}
