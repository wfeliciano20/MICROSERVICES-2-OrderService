package com.williamfeliciano.orderservice.external.intercept;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

@Configuration
@AllArgsConstructor
public class OAuthRequestInterceptor implements RequestInterceptor {

    private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " +
                oAuth2AuthorizedClientManager
                        .authorize(OAuth2AuthorizeRequest
                                .withClientRegistrationId("internal-client")
                                .principal("internal")
                                .build()).getAccessToken().getTokenValue());
    }
}
