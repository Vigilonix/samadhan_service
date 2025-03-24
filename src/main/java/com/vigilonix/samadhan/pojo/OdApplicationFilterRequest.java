package com.vigilonix.samadhan.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vigilonix.samadhan.enums.ApplicationCategory;
import com.vigilonix.samadhan.enums.ApplicationFilterRequestStatus;
import com.vigilonix.samadhan.enums.OdApplicationStatus;
import lombok.*;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OdApplicationFilterRequest {
    private ApplicationFilterRequestStatus status;
    private List<ApplicationCategory> categories;
    private String searchKeyword;
}
