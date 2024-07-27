package com.vigilonix.jaanch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Rank {
    CONSTABLE(1),
    HEAD(2),
    HEAD_CONSTABLE(3),
    PTC(4),
    ASSISTANT_SUB_INSPECTOR(5),
    SUB_INSPECTOR(6),
    INSPECTOR_OF_POLICE(7),
    DSP(8),
    ASP(9),
    SP(10),
    SSP(11),
    DIG(12),
    IG(13),
    ADG(14),
    DGP(15),
    DG(16);
    private final int level;
}
