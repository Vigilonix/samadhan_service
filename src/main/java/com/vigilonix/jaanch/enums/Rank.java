package com.vigilonix.jaanch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Rank {
    CONSTABLE(100),
    HEAD(200),
    HEAD_CONSTABLE(300),
    PTC(400),
    ASSISTANT_SUB_INSPECTOR(500),
    PROBATIONARY_SUB_INSPECTOR(600),
    SUB_INSPECTOR(700),
    INSPECTOR_OF_POLICE(800),
    DSP(900),
    ASP(1000),
    SP(1100),
    SSP(1200),
    DIG(1300),
    IG(1400),
    ADG(1500),
    DGP(1600),
    DG(1700);
    private final int level;
}
