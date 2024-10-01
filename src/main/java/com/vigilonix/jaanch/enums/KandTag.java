package com.vigilonix.jaanch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KandTag {
    CHAIN_SNATCHING("Chain Snatching"),
    MOBILE_SNATCHING("Mobile Snatching"),
    TWO_WHEELER_THEFT("Two Wheeler Theft"),
    THREE_WHEELER_THEFT("Three Wheeler Theft"),
    FOUR_WHEELER_THEFT("Four Wheeler Theft");
//    LOOT("LOOT");

    private final String name;
}
