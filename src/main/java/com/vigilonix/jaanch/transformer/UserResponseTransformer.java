package com.vigilonix.jaanch.transformer;

import com.dt.beyond.config.Constant;
import com.dt.beyond.enums.MediaType;
import com.dt.beyond.helper.ReadAuthorHelper;
import com.dt.beyond.model.*;
import com.dt.beyond.pojo.MovieResponseRequest;
import com.dt.beyond.pojo.ReadRequestResponse;
import com.dt.beyond.pojo.RecipeRequestResponse;
import com.dt.beyond.pojo.UserResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class UserResponseTransformer implements Transformer<User, UserResponse> {
    public static final long MILLISECONDS_IN_ONE_YEAR = 365 * 24 * 3600 * 1000L;
    private final UserMediaTransformer userMediaTransformer;
    private final BarrierTransformer barrierTransformer;
    private final UserSocialMediaTransformer userSocialMediaTransformer;
    private final ReadAuthorHelper readAuthorHelper;

    @Override
    public UserResponse transform(User principal) {
        List<UserMediaMapping> medias = new ArrayList<>(principal.getMediaMappings());
        medias.sort(Comparator.comparingInt(UserMediaMapping::getPosition));
        if (CollectionUtils.isEmpty(medias)) {
            medias.add(UserMediaMapping.builder()
                    .mediaType(MediaType.IMAGE)
                    .createdOn(System.currentTimeMillis())
                    .qcState(Constant.DEFAULT_STATE)
                    .position(0)
                    .uri(Constant.DUMMY_IMAGE)
                    .uuid(UUID.randomUUID())
                    .build());
        }
        return UserResponse.builder()
                .username(principal.getUsername())
                .email(principal.getEmail())
                .name(principal.getQcName() == null ? principal.getName() : principal.getQcName().getName())
                .qcName(principal.getQcName() == null
                        ? principal.getName() == null ? null : QcName.builder().name(principal.getName()).qcState(Constant.DEFAULT_STATE).build()
                        : principal.getQcName())
                .country(principal.getCountry())
                .age(principal.getDobInMillis() == null ? null : (System.currentTimeMillis() - principal.getDobInMillis()) / MILLISECONDS_IN_ONE_YEAR)
                .dobInMillis(principal.getQcDobInMillis() == null ? principal.getDobInMillis() : principal.getQcDobInMillis().getDobInMillis())
                .qcDobInMillis(principal.getQcDobInMillis() == null
                        ? principal.getDobInMillis() == null ? null : QcDobInMillis.builder().dobInMillis(principal.getDobInMillis()).qcState(Constant.DEFAULT_STATE).build()
                        : principal.getQcDobInMillis())
                .latitude(principal.getLatitude())
                .longitude(principal.getLongitude())
                .description(principal.getQcDescription() == null ? principal.getDescription() : principal.getQcDescription().getDescription())
                .qcDescription(principal.getQcDescription() == null
                        ? principal.getDescription() == null ? null : QcDescription.builder().description(principal.getDescription()).qcState(Constant.DEFAULT_STATE).build()
                        : principal.getQcDescription())
                .education(principal.getQcEducation() == null ? principal.getEducation() : principal.getQcEducation().getEducation())
                .qcEducation(principal.getQcEducation() == null
                        ? principal.getEducation() == null ? null : QcEducation.builder().education(principal.getEducation()).qcState(Constant.DEFAULT_STATE).build()
                        : principal.getQcEducation())
                .gender(principal.getGender())
                .qcGender(principal.getQcGender() == null
                        ? principal.getGender() == null ? null : QcGender.builder().gender(principal.getGender()).qcState(Constant.DEFAULT_STATE).build()
                        : principal.getQcGender())
                .createdOn(principal.getCreatedOn())
                .live(principal.getLive())
                .lastLive(principal.getLastLive())
                .state(principal.getState())
                .work(principal.getWork())
                .uuid(principal.getUuid())
                .popularity(principal.getPopularity())
                .locationRangeInMeters(principal.getLocationRangeInMeters())
                .referralCode(principal.getReferralCode())
                .address(principal.getAddress())
                .jobCompany(principal.getQcJobCompany() == null ? principal.getJobCompany() : principal.getQcJobCompany().getJobCompany())
                .qcJobCompany(principal.getQcJobCompany() == null
                        ? principal.getJobCompany() == null ? null : QcJobCompany.builder().jobCompany(principal.getJobCompany()).qcState(Constant.DEFAULT_STATE).build()
                        : principal.getQcJobCompany())
                .jobTitle(principal.getQcJobTitle() == null ? principal.getJobTitle() : principal.getQcJobTitle().getJobTitle())
                .qcJobTitle(principal.getQcJobTitle() == null
                        ? principal.getJobTitle() == null ? null : QcJobTitle.builder().jobTitle(principal.getJobTitle()).qcState(Constant.DEFAULT_STATE).build()
                        : principal.getQcJobTitle())
                .minInterestedAge(principal.getMinInterestedAge())
                .maxInterestedAge(principal.getMaxInterestedAge())
                .newMatchNotification(principal.isNewMatchNotification())
                .messageNotification(principal.isMessageNotification())
                .messageLikeNotification(principal.isMessageLikeNotification())
                .barrierCrossedNotification(principal.isBarrierCrossedNotification())
                .profileUri(principal.getProfileUri())
                .invisible(principal.getInvisible())
                .doNotDisturb(principal.getDoNotDisturb())
                .mediaResponses(medias
                        .stream()
                        .map(userMediaTransformer::transform).collect(Collectors.toList()))
                .read(principal.getReadMappings().stream()
                        .map(r -> ReadRequestResponse.builder()
                                .title(r.getTitle())
                                .posterUri(r.getPosterUri())
                                .isbn10(r.getIsbn10())
                                .isbn13(r.getIsbn13())
                                .authorTitle(readAuthorHelper.getAuthors(r.getAuthorTitles()))
                                .uuid(r.getUuid().toString())
                                .build())
                        .collect(Collectors.toList()))
                .recipeRequestResponse(principal.getFoodMappings().stream()
                        .map(f -> RecipeRequestResponse.builder()
                                .uuid(f.getUuid().toString())
                                .qcState(f.getQcState() == null ? Constant.DEFAULT_STATE : f.getQcState())
                                .recipeName(f.getRecipeName()).build())
                        .collect(Collectors.toList()))
                .movieResponseRequests(principal.getMovieMappings().stream().map(m ->
                        MovieResponseRequest.builder()
                                .uuid(m.getUuid().toString())
                                .movieId(m.getMovieId())
                                .title(m.getTitle())
                                .posterUri(m.getPosterUri())
                                .adult(m.isAdult())
                                .genreNames(StringUtils.isEmpty(m.getGenreName()) ?
                                        Collections.emptyList() : Arrays.asList(m.getGenreName().split(Constant.SEPARATOR)))
                                .genreIds(StringUtils.isEmpty(m.getGenreIds()) ? Collections.emptyList()
                                        : Arrays.stream(m.getGenreIds().split(Constant.SEPARATOR))
                                        .map(Long::valueOf)
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .hobby(principal.getHobbyMappings().stream()
                        .map(UserHobbyMapping::getHobby)
                        .collect(Collectors.toList()))
                .barrier(principal.getBarrierMappings().stream().map(barrierTransformer::transform).collect(Collectors.toList()))
                .interestedGenders(principal.getInterestedGenderMappings().stream().map(UserInterestedGenderMapping::getGender).collect(Collectors.toList()))
                .socialMediaResponses(principal.getSocialMediaMappings().stream().map(userSocialMediaTransformer::transform)
                        .collect(Collectors.toList()))
                .build();
    }
}
