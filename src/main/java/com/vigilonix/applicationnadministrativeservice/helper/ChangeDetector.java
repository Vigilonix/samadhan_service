package com.vigilonix.applicationnadministrativeservice.helper;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ChangeDetector {


    public boolean isChanged(Object old, Object current) {
        return !(Objects.equals(old, current));
    }

}
