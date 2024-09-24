package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Section {
    private Integer sectionNumber;
    private Integer clause;
    //generally lower case a b, c,d
    //TODO on Little: to get back with children of sub cluase
    private String subClause;
}
