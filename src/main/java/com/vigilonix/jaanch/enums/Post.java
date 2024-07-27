package com.vigilonix.jaanch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Post {
    SDPO(3),
    SHO(2),
    BEAT(1),
    CITY_SP(4),
    SP(5);
    private final int level;
}
