package com.vigilonix.applicationnadministrativeservice.transformer;

import com.vigilonix.applicationnadministrativeservice.model.OAuthToken;
import com.vigilonix.applicationnadministrativeservice.request.OAuth2Response;
import org.apache.commons.collections4.Transformer;
import org.springframework.stereotype.Component;

import static com.vigilonix.applicationnadministrativeservice.config.Constant.GRANT;

@Component
public class AuthTokenTransformer implements Transformer<OAuthToken, OAuth2Response> {

    @Override
    public OAuth2Response transform(OAuthToken oAuthToken) {
        return OAuth2Response.builder()
                .accessToken(oAuthToken.getToken())
                .refreshToken(oAuthToken.getRefreshToken())
                .expireOn(oAuthToken.getExpireTime())
                .tokenType(GRANT)
                .scope(GRANT)
                .build();
    }
}
