package com.vigilonix.jaanch.helper;

import com.dt.beyond.model.QcValAttribute;
import com.dt.beyond.model.UserFoodMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChangeDetector {


    public boolean isChanged(Object old, Object current) {
        return !(Objects.equals(old, current));
    }

}
