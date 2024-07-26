package com.vigilonix.jaanch.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@ToString
@AllArgsConstructor
public enum ValidationErrorEnum implements ValidationError {
    INVALID_TOKEN(1, "Invalid TOKEN", Collections.emptyList()),
    NAME_ATTRIBUTE_LENGTH_MORE_THAN_EXPECTED(2, "Empty or greater than 64 chars name.", Arrays.asList("first_name", "last_Name")),
    INVALID_EMAIL_FORMAT(3, "Invalid email format.", Collections.singletonList("email")),
    EMPTY_USERNAME(4, "Username can not be empty.", Collections.singletonList("username")),
    UNAUTHORIZED_REQUEST(5, "Unauthorized request", Collections.emptyList()),
    IMAGE_POSITION_ALREADY_EXIST(6, "Image position Already exist", Collections.emptyList()),
    USER_ALREADY_EXISTS(7, "User already exists with the same emailId", Collections.emptyList()),
    REFERRAL_MAPPING_ALREADY_EXIST(8, "You have already signed up using referral code.", Collections.singletonList("referral_code")),
    INVALID_REFERRER_TOKEN(9, "Invalid Referral Code", Collections.singletonList("referral_code")),
    INVALID_HOBBY(10, "Invalid Hobby", Collections.singletonList("hobby")),
    DISABLED_USER(11, "Disabled User", Collections.emptyList()),
    EMPTY_FILE(12, "Empty file", Collections.singletonList("file")),
    INVALID_FILE_REQUEST(13, "Invalid file", Collections.singletonList("file")),
    WRONG_CREDENTIALS(14, "Invalid Credentials", Collections.emptyList()),
    INVALID_GENDER(15, "Invalid Gender", Collections.singletonList("gender")),
    FAILED_TO_FETCH_POSTER(16, "Movie Title Search Failed. Please Retry after sometime.", Collections.singletonList("movie")),
    INSTAGRAM_LONG_LIVE_TOKEN_FAILURE(17, "Failed to fetch Instagram Long lived token", Collections.emptyList()),
    INSTAGRAM_REFRESH_TOKEN_FAILURE(18, "Failed to refresh Instagram access token", Collections.emptyList()),
    SPOTIFY_LONG_LIVE_TOKEN_FAILURE(19, "Failed to fetch Spotify Long lived token", Collections.emptyList()),
    SPOTIFY_REFRESH_TOKEN_FAILURE(20, "Failed to refresh Spotify access token", Collections.emptyList()),
    BOOST_RENEW_BUFFER_EXPIRED(21, "You recently used Boost, Please wait to re use it.", Collections.emptyList()),
    BOOST_IS_ACTIVE(22, "Boost is Active", Collections.emptyList()),
    FAILED_TO_GOOD_READS_FETCH_POSTER(23, "Books Search Failed. Please Retry after sometime.", Collections.singletonList("book")),
    INVALID_ID(24, "Invalid id", Collections.singletonList("uuid")),
    CAN_NOT_ACTIVATE_DISABLED_ACCOUNT(25, "Your account has been disabled. to activate your account contact us.", Collections.emptyList()),
    NUMBER_FORMAT_EXCEPTION(26, "Failed To serialize UUID", Collections.singletonList("uuid")),
    INVALID_GRANT(27, "Invalid Grant", Collections.emptyList()),
    NO_VALID_CHAT_EXIST(28, "You have been unmatched.", Collections.emptyList()),
    EMPTY_PASSWORD(29, "Password is Empty", Collections.emptyList()),
    READ_MORE_THAN_ALLOWED(30, "Maximum 9 Books are allowed.", Collections.emptyList()),
    MOVIE_MORE_THAN_ALLOWED(31, "Maximum 9 Movies are allowed.", Collections.emptyList()),
    FOOD_MORE_THAN_ALLOWED(32, "Maximum 9 Recipes are allowed.", Collections.emptyList()),
    HOBBY_MORE_THAN_ALLOWED(33, "Maximum 9 Hobbies are allowed.", Collections.emptyList()),
    INTERESTED_GENDER_MORE_THAN_ALLOWED(34, "Maximum 30 Genders are allowed.", Collections.emptyList()),
    INVALID_MEDIA_URI(35, "Media is invalid", Collections.emptyList()),
    EMPTY_MEDIA_TYPE(36, "Empty Media Type", Collections.emptyList()),
    FOOD_NAME_EMPTY(37, "Recipe Name should not be empty", Collections.emptyList()),
    MOVIE_NAME_EMPTY(37, "Movie Name should not be empty", Collections.emptyList()),
    MOVIE_POSTER_EMPTY(37, "Movie poster image is invalid", Collections.emptyList()),
    BOOK_NAME_EMPTY(38, "Book Name should not be empty", Collections.emptyList()),
    BOOK_POSTER_EMPTY(39, "Book poster image is invalid", Collections.emptyList()),
    INVALID_UUID(40, "invalid id", Collections.emptyList()),
    FAILED_TO_FETCH_REMOTE_FILE(41, "Failed to Fetch image from Google/Fb/Insta", Collections.emptyList()),
    REMOTE_INVALID_FILE(42, "Invalid Image From Google/Fb/Insta", Collections.emptyList()),
    REVERSE_GEOCODE_LOCATION_FAILURE(43, "Failed to fetch address", Collections.emptyList()),
    DELETED_ACCOUNT(44, "This account has been deleted", Collections.emptyList()),
    INSTAGRAM_SHORT_LIVE_TOKEN_FAILURE(45, "Failed to fetch Instagram short lived token", Collections.emptyList()),
    INVALID_REQUEST(46, "Invalid Request", Collections.emptyList()),
    PLEASE_TRY_AGAIN(47, "Please try again after SomeTime", Collections.emptyList()),
    DATA_INTEGRITY_VIOLATION(48, "Invalid Request.", Collections.emptyList()),
    DESCRIPTION_SIZE_MORE(49, "Description size is greater than 512 chars", Collections.emptyList()),
    FAILED_TO_REGISTER_SHORT_URL(50, "failed to register short link", Collections.emptyList()),
    SWIPE_LIMIT_OVER(51, "Your swipe limit has reached. Please wait %s to swipe again", Collections.emptyList()),
    INVALID_BATCH_SIZE(52, "Invalid batch size", Collections.emptyList()),
    MEDIA_POSITION_NULL(53, "Last Image can not be deleted", Collections.emptyList()),
    LAST_MEDIA_CANT_BE_DELETED(53, "Last Media can not be deleted.", Collections.emptyList()),
    NULL_LAT_LONG(54, "Failed to fetch location. Please check if gps permissions are granted.", Collections.emptyList()),
    BARRIER_ALREADY_EXIST(55, "There is already one barrier.", Collections.emptyList()),
    BARRIER_LIMIT_OVER(56, "Barrier Limit crossed", Collections.emptyList()),
    UNMATCHED_NOW(57, "This match is not valid anymore", Collections.emptyList()),
    MAX_AGE_SHOULD_BE_GREATER_THAN_MIN_AGE(58, "Max Age should be less than Min Age", Collections.emptyList()),
    MEDIA_MORE_THAN_ALLOWED(59, "Maximum 9 media are allowed.", Collections.emptyList()),
    JOB_TITLE_SIZE_MORE(60, "Job title size is greater than 64 chars", Collections.emptyList()),
    JOB_COMPANY_SIZE_MORE(61, "Job company size is greater than 64 chars", Collections.emptyList()),
    EDUCATION_SIZE_MORE(62, "Education size is greater than 64 chars", Collections.emptyList()),
    INVALID_BARRIER_REQUEST(63, "Invalid Barrier Request", Collections.emptyList());


    private final int code;
    private final String messageFormat;
    private final List<String> attributes;

}
