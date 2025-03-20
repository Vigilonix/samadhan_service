package com.vigilonix.samadhan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Post {
    ASSOCIATE(50),
    OFFICER(100),
    MANAGER(600),

    SDM(900),
    BDO(800),
    CO(700),
    BPRO(600),
    EO(500),
    EM(900),
    DCLR(900);
    private final int level;
}
