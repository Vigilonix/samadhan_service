package com.vigilonix.jaanch.validator;

import com.dt.beyond.enums.ValidationError;
import com.dt.beyond.enums.ValidationErrorEnum;
import com.dt.beyond.request.UserRequest;
import com.dt.beyond.service.OnBoardingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserRequestValidator implements Validator<List<ValidationError>, UserRequest> {
    private final OnBoardingService onBoardingService;
    private final int MAX_JOB_LENGTH = 130;
    private final int MAX_EDUCATION_LENGTH = 250;
    @Override
    public List<ValidationError> validate(UserRequest user) {
        List<ValidationError> errors = new ArrayList<>();
        if (StringUtils.isNotEmpty(user.getEmail()) && !EmailValidator.getInstance().isValid(user.getEmail())) {
            errors.add(ValidationErrorEnum.INVALID_EMAIL_FORMAT);
        }

//        if (StringUtils.isEmpty(user.getUsername()) || user.getUsername().length() > 50) {
//            errors.add(ValidationErrorEnum.INVALID_EMAIL_FORMAT);
//        }
        if (CollectionUtils.isNotEmpty(user.getHobby())) {
            if (!onBoardingService.getHobbies().containsAll(user.getHobby())) {
                errors.add(ValidationErrorEnum.INVALID_HOBBY);
            }
        }
        if (StringUtils.isNotEmpty(user.getGender()) && !onBoardingService.getGenders().contains(user.getGender())) {
            errors.add(ValidationErrorEnum.INVALID_GENDER);
        }
        if (user.getInterestedGenders() != null && !onBoardingService.getGenders().containsAll(user.getInterestedGenders())) {
            errors.add(ValidationErrorEnum.INVALID_GENDER);
        }
        if (user.getReads() != null && user.getReads().size() > 9) {
            errors.add(ValidationErrorEnum.READ_MORE_THAN_ALLOWED);
        }
        if (user.getMovieResponseRequests() != null && user.getMovieResponseRequests().size() > 9) {
            errors.add(ValidationErrorEnum.MOVIE_MORE_THAN_ALLOWED);
        }
        if (user.getFoods() != null && user.getFoods().size() > 9) {
            errors.add(ValidationErrorEnum.FOOD_MORE_THAN_ALLOWED);
        }
        if (user.getHobby() != null && user.getHobby().size() > 9) {
            errors.add(ValidationErrorEnum.HOBBY_MORE_THAN_ALLOWED);
        }
        if (user.getInterestedGenders() != null && user.getInterestedGenders().size() > 9) {
            errors.add(ValidationErrorEnum.INTERESTED_GENDER_MORE_THAN_ALLOWED);
        }
        if (user.getInterestedGenders() != null && user.getInterestedGenders().size() > 9) {
            errors.add(ValidationErrorEnum.INTERESTED_GENDER_MORE_THAN_ALLOWED);
        }
        if (CollectionUtils.isNotEmpty(user.getFoods())) {
            user.getFoods().forEach(fr -> {
                if (StringUtils.isEmpty(fr.getRecipeName()) || StringUtils.countMatches(fr.getRecipeName(), '\n') > 0) {
                    errors.add(ValidationErrorEnum.FOOD_NAME_EMPTY);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(user.getReads())) {
            user.getReads().forEach(r -> {
                if (StringUtils.isEmpty(r.getTitle())) {
                    errors.add(ValidationErrorEnum.BOOK_NAME_EMPTY);
                }
                if (!UrlValidator.getInstance().isValid(r.getPosterUri())) {
                    errors.add(ValidationErrorEnum.BOOK_POSTER_EMPTY);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(user.getMovieResponseRequests())) {
            user.getMovieResponseRequests().forEach(m -> {
                if (StringUtils.isEmpty(m.getTitle())) {
                    errors.add(ValidationErrorEnum.MOVIE_NAME_EMPTY);
                }
                if (!UrlValidator.getInstance().isValid(m.getPosterUri())) {
                    errors.add(ValidationErrorEnum.MOVIE_POSTER_EMPTY);
                }
            });
        }
        if (user.getMinInterestedAge() != null && user.getMaxInterestedAge() != null && user.getMinInterestedAge() > user.getMaxInterestedAge()) {
            errors.add(ValidationErrorEnum.MAX_AGE_SHOULD_BE_GREATER_THAN_MIN_AGE);
        }


        if (user.getJobCompany() != null && (user.getJobCompany().length() > 64 || StringUtils.countMatches(user.getJobCompany(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.JOB_COMPANY_SIZE_MORE);
        }
        if (user.getEducation() != null && (user.getEducation().length() > MAX_EDUCATION_LENGTH || StringUtils.countMatches(user.getEducation(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.EDUCATION_SIZE_MORE);
        }
        if (user.getName() != null && (user.getName().length() > 64 || StringUtils.countMatches(user.getName(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.NAME_ATTRIBUTE_LENGTH_MORE_THAN_EXPECTED);
        }
        if (user.getJobTitle() != null && (user.getJobTitle().length() > MAX_JOB_LENGTH || StringUtils.countMatches(user.getJobTitle(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.JOB_TITLE_SIZE_MORE);
        }
        if (user.getDescription() != null && (user.getDescription().length() > 512 || StringUtils.countMatches(user.getDescription(), '\n') > 10)) {
            errors.add(ValidationErrorEnum.DESCRIPTION_SIZE_MORE);
        }


        if (user.getQcEducation() != null && user.getQcEducation().getEducation() != null && (user.getQcEducation().getEducation().length() > MAX_EDUCATION_LENGTH || StringUtils.countMatches(user.getQcEducation().getEducation(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.EDUCATION_SIZE_MORE);
        }
        if (user.getQcName() != null && user.getQcName().getName() != null && (user.getQcName().getName().length() > 64 || StringUtils.countMatches(user.getQcName().getName(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.NAME_ATTRIBUTE_LENGTH_MORE_THAN_EXPECTED);
        }
        if (user.getQcJobTitle() != null && user.getQcJobTitle().getJobTitle() != null && (user.getQcJobTitle().getJobTitle().length() > MAX_JOB_LENGTH || StringUtils.countMatches(user.getQcJobTitle().getJobTitle(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.JOB_TITLE_SIZE_MORE);
        }
        if (user.getQcJobCompany() != null && user.getQcJobCompany().getJobCompany() != null && (user.getQcJobCompany().getJobCompany().length() > 64 || StringUtils.countMatches(user.getQcJobCompany().getJobCompany(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.JOB_COMPANY_SIZE_MORE);
        }
        if (user.getQcDescription() != null && user.getQcDescription().getDescription() != null && (user.getQcDescription().getDescription().length() > 512 || StringUtils.countMatches(user.getQcDescription().getDescription(), '\n') > 10)) {
            errors.add(ValidationErrorEnum.DESCRIPTION_SIZE_MORE);
        }


        return errors;
    }

}
