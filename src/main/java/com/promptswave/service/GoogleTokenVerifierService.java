package com.promptswave.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Handles the server-side half of Google's OAuth2 "Authorization Code" flow.
 *
 * The frontend opens Google's consent screen, Google redirects back with a
 * short-lived {@code code}, and the frontend POSTs that code to our backend.
 * This service then:
 * 1. Exchanges the code for Google tokens (token endpoint).
 * 2. Uses the access token to fetch the user's profile (userinfo endpoint).
 *
 * Everything is done backend-side; the frontend never integrates a Google SDK.
 */
@Service
public class GoogleTokenVerifierService {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient restClient;

    public GoogleTokenVerifierService(
            @Value("${app.google.client-id}") String clientId,
            @Value("${app.google.client-secret}") String clientSecret,
            @Value("${app.google.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.restClient = RestClient.create();
    }

    /**
     * Exchanges an authorization code for the Google user's profile.
     *
     * @param code the authorization code returned to the frontend by Google
     * @return a {@link GoogleUserInfo} with the verified email, name and picture
     */
    @SuppressWarnings("unchecked")
    public GoogleUserInfo exchangeCode(String code) {
        // --- Step 1: exchange the authorization code for an access token ---
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to exchange Google authorization code: " + ex.getMessage());
        }

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new IllegalArgumentException("Google did not return an access token");
        }
        String accessToken = (String) tokenResponse.get("access_token");

        // --- Step 2: fetch the user's profile from the userinfo endpoint ---
        Map<String, Object> userInfo;
        try {
            userInfo = restClient.get()
                    .uri(USERINFO_ENDPOINT)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to fetch Google user info: " + ex.getMessage());
        }

        if (userInfo == null || userInfo.get("email") == null) {
            throw new IllegalArgumentException("Google profile did not contain an email");
        }

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");
        Object verified = userInfo.get("email_verified");
        boolean emailVerified = (verified instanceof Boolean b) ? b
                : Boolean.parseBoolean(String.valueOf(verified));

        return new GoogleUserInfo(email, name, picture, emailVerified);
    }

    /**
     * Builds the Google consent-screen URL the frontend should open.
     * Optional helper so the client ID/redirect stay entirely backend-owned.
     */
    public String buildAuthorizationUrl(String state) {
        StringBuilder url = new StringBuilder("https://accounts.google.com/o/oauth2/v2/auth");
        url.append("?client_id=").append(encode(clientId));
        url.append("&redirect_uri=").append(encode(redirectUri));
        url.append("&response_type=code");
        url.append("&scope=").append(encode("openid email profile"));
        url.append("&access_type=offline");
        url.append("&prompt=select_account");
        if (state != null && !state.isBlank()) {
            url.append("&state=").append(encode(state));
        }
        return url.toString();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Minimal projection of the Google profile fields we care about. */
    public record GoogleUserInfo(String email, String name, String picture, boolean emailVerified) {
    }
}