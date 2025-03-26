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
    EM(800),
    EO(700),
    DCLR(800),
    CO(600),
    BDO(500),
    BPRO(400);
    private final int level;
}
