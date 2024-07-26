package com.vigilonix.jaanch.request;

import com.dt.beyond.enums.Country;
import com.dt.beyond.enums.State;
import com.dt.beyond.model.*;
import com.dt.beyond.request.UserMediaResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private final UUID uuid;
    private final String name;
    @Setter
    @JsonProperty("qc_name")
    private QcName qcName;
    @Deprecated
    @Setter
    private String work;
    @Deprecated
    @Setter
    private String description;
    @JsonProperty("qc_description")
    @Setter
    private QcDescription qcDescription;
    @Deprecated
    @Setter
    private String education;
    @Setter
    @JsonProperty("qc_education")
    private QcEducation qcEducation;
    @JsonProperty("gender")
    private final String gender;
    @Setter
    @JsonProperty("qc_gender")
    private QcGender qcGender;
    private final Boolean live;
    @JsonProperty("last_live")
    private final Long lastLive;
    @JsonProperty("media")
    private final List<UserMediaResponse> mediaResponses;
    private final List<String> hobby;
    @JsonProperty("music")
    private final List<String> spotifyId;
    @JsonProperty("movie")
    private final List<MovieResponseRequest> movieResponseRequests;
    @JsonProperty("recipe")
    private final List<RecipeRequestResponse> recipeRequestResponse;
    @JsonProperty("read")
    private final List<ReadRequestResponse> read;
    @JsonProperty("social_media")
    private final List<SocialMediaResponse> socialMediaResponses;
    @Setter
    private List<BarrierResponseRequest> barrier;
    @Deprecated
    @JsonProperty("job_title")
    private final String jobTitle;
    @Setter
    @JsonProperty("qc_job_title")
    private QcJobTitle qcJobTitle;
    @Deprecated
    @Setter
    @JsonProperty("job_company")
    private String jobCompany;
    @Setter
    @JsonProperty("qc_job_company")
    private QcJobCompany qcJobCompany;
    @Setter
    @JsonProperty("min_interested_age")
    private Long minInterestedAge;
    @Setter
    @JsonProperty("max_interested_age")
    private Long maxInterestedAge;
    @JsonProperty("profile_uri")
    private final String profileUri;
    @Setter
    @JsonProperty("new_match_notification")
    private Boolean newMatchNotification;
    @Setter
    @JsonProperty("message_notification")
    private Boolean messageNotification;
    @Setter
    @JsonProperty("message_like_notification")
    private Boolean messageLikeNotification;
    @Setter
    @JsonProperty("barrier_crossed_notification")
    private Boolean barrierCrossedNotification;
    @Setter
    private String username;
    @Setter
    private String email;
    @Setter
    private Country country;
    @Setter
    private Integer popularity;
    @Setter
    @JsonProperty("interested_genders")
    private List<String> interestedGenders;
    @Setter
    private State state;
    @Setter
    @JsonProperty("membership")
    private UserMembershipResponse userMembershipResponse;
    @Setter
    @JsonProperty("referral_code")
    private String referralCode;
    @JsonProperty("dob")
    @Setter
    private Long dobInMillis;
    @JsonProperty("qc_dob")
    private QcDobInMillis qcDobInMillis;
    @Setter
    private Double latitude;
    @Setter
    private Double longitude;
    @Setter
    @JsonProperty("created_on")
    private Long createdOn;
    @Setter
    @JsonProperty("location_range")
    private Integer locationRangeInMeters;
    @Setter
    private Long age;
    @Setter
    private String address;
    @Setter
    @JsonProperty("do_not_disturb")
    private Boolean doNotDisturb;
    @Setter
    private Boolean invisible;
}
