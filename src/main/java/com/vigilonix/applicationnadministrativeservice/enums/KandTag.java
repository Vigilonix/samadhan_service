package com.vigilonix.applicationnadministrativeservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KandTag {
    ALL("ALL",0),
    CHAIN_SNATCHING("Chain", 1),
    MOBILE_SNATCHING("Mobile", 2),
    TWO_WHEELER_THEFT("2 Wheeler", 3),
    THREE_WHEELER_THEFT("3 Wheeler", 4),
    FOUR_WHEELER_THEFT("4 Wheeler", 5);
//    LOOT("LOOT");

    private final String label;
    private final int order;
}
