package com.vigilonix.samadhan.enums;

public enum State {
    //the account got suspended because of privacy violations and can come back.
    SUSPENDED,
    //User himself deactivated his profile
    DEACTIVATE,
    //was disabled because of privacy violations and he can never return. it is permanent
    DISABLED,
    DELETED,
    ACTIVE,
}
