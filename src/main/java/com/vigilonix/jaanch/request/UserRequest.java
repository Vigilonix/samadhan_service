package com.vigilonix.jaanch.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {
    private String uuid;
    @Deprecated
    private String name;
    private String password;
    private String email;
    private String username;
    @Deprecated
    private Long dob;
    //    private Double latitude;
//    private Double longitude;
    @Deprecated
    private String description;
    @Deprecated
    private String education;
    private String gender;
    private Long lastLive;

    @JsonProperty("location_range")
    private Integer locationRangeInMeters;
    @JsonProperty("sso_id")
    private String ssoId;
    private String work;
    @JsonProperty("apply_referral_code")
    private String referralCode;
    private String address;
    @Deprecated
    @JsonProperty("job_title")
    private String jobTitle;
    @Deprecated
    @JsonProperty("job_company")
    private String jobCompany;

    @JsonProperty("min_interested_age")
    private Long minInterestedAge;
    @JsonProperty("max_interested_age")
    private Long maxInterestedAge;
    @JsonProperty("new_match_notification")
    private Boolean newMatchNotification;
    @JsonProperty("message_notification")
    private Boolean messageNotification;
    @JsonProperty("message_like_notification")
    private Boolean messageLikeNotification;
    @JsonProperty("barrier_crossed_notification")
    private Boolean barrierCrossedNotification;
    @JsonProperty("do_not_disturb")
    private Boolean doNotDisturb;
    private Boolean invisible;
}
