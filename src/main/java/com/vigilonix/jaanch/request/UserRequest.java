package com.vigilonix.jaanch.request;

import com.dt.beyond.enums.Country;
import com.dt.beyond.enums.SsoProvider;
import com.dt.beyond.enums.State;
import com.dt.beyond.model.*;
import com.dt.beyond.pojo.BarrierResponseRequest;
import com.dt.beyond.pojo.MovieResponseRequest;
import com.dt.beyond.pojo.ReadRequestResponse;
import com.dt.beyond.pojo.RecipeRequestResponse;
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
    private Country country;
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
    @JsonProperty("media")
    private List<MediaRequest> mediaRequests;
    private Set<String> hobby;
    @JsonProperty("movie")
    private List<MovieResponseRequest> movieResponseRequests;
    @JsonProperty("spotify_music")
    private Set<String> spotifyMusicIds;
    @JsonProperty("recipe")
    private Set<RecipeRequestResponse> foods;
    @JsonProperty("read")
    private Set<ReadRequestResponse> reads;
    @JsonProperty("interested_genders")
    private Set<String> interestedGenders;
    @JsonProperty("location_range")
    private Integer locationRangeInMeters;
    @JsonProperty("sso_id")
    private String ssoId;
    @JsonProperty("sso_auth_provider")
    private SsoProvider ssoProvider;
    private State state;
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

    @JsonProperty("qc_description")
    private QcDescription qcDescription;
    @JsonProperty("qc_education")
    private QcEducation qcEducation;
    @JsonProperty("qc_job_title")
    private QcJobTitle qcJobTitle;
    @JsonProperty("qc_name")
    private QcName qcName;
    @JsonProperty("qc_job_company")
    private QcJobCompany qcJobCompany;
    @JsonProperty("qc_dob")
    private QcDobInMillis qcDobInMillis;
    @JsonProperty("qc_gender")
    private QcGender qcGender;

    private List<BarrierResponseRequest> barrier;
}
