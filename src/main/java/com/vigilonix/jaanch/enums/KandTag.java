package com.vigilonix.jaanch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KandTag {
    ALL("ALL"),
    CHAIN_SNATCHING("Chain"),
    MOBILE_SNATCHING("Mobile"),
    TWO_WHEELER_THEFT("2 Wheeler"),
    THREE_WHEELER_THEFT("3 Wheeler"),
    FOUR_WHEELER_THEFT("4 Wheeler");
//    LOOT("LOOT");

    private final String name;
}
