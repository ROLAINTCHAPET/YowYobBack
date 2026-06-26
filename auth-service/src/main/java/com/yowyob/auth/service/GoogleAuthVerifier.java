package com.yowyob.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@Slf4j
public class GoogleAuthVerifier {

    @org.springframework.beans.factory.annotation.Value("${google.client-id}")
    private String clientId;

    public GoogleIdToken.Payload verify(String tokenString) throws GeneralSecurityException, IOException {
        String actualClientId = (clientId != null && !clientId.isEmpty()) ? clientId
                : "763004243989-g1fftlketknf2ip32f0fsi39ukcqqkq3.apps.googleusercontent.com";
        log.info("Verifying Google Token with Client ID: '{}'", actualClientId);

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(actualClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                throw new IllegalArgumentException("Invalid ID token (verify returned null).");
            }
        } catch (Exception e) {
            log.error("Error in GoogleAuthVerifier: ", e);
            throw e;
        }
    }
}
