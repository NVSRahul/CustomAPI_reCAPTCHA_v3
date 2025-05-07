package org.company.recaptchagate.service;

import org.company.recaptchagate.dto.GoogleRecaptchaResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;

    public RecaptchaService(WebClient.Builder webClientBuilder) {
        log.debug("RecaptchaService constructor called.");
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    private void initializeWebClient() {
        log.debug("Initializing WebClient in @PostConstruct.");
        log.info("Value of google.recaptcha.verify-url injected: '{}'", recaptchaVerifyUrl);

        if (recaptchaVerifyUrl == null || recaptchaVerifyUrl.isEmpty()) {
            log.error("!!! CRITICAL: google.recaptcha.verify-url is NULL or EMPTY. Check application.properties. !!!");
            this.webClient = webClientBuilder.baseUrl("http://invalid-url-due-to-missing-property.local").build();
        } else {
            this.webClient = webClientBuilder.baseUrl(recaptchaVerifyUrl).build();
            log.info("WebClient base URL set to: '{}'", recaptchaVerifyUrl);
        }
    }

    public Mono<GoogleRecaptchaResponse> verifyRecaptcha(String clientToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", recaptchaSecret);
        formData.add("response", clientToken);
        // Optional: formData.add("remoteip", userIpAddress);

        log.debug("Inside verifyRecaptcha method.");
        log.info("Attempting POST to Google verify URL: '{}'", (webClient != null ? recaptchaVerifyUrl : "WebClient is NULL"));

        if (webClient == null) {
            log.error("WebClient was not initialized. Cannot proceed with verification.");
            return Mono.error(new IllegalStateException("WebClient not initialized, check startup logs."));
        }

        log.info("Verifying reCAPTCHA token with Google...");

        return webClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GoogleRecaptchaResponse.class)
                .doOnSuccess(response -> {
                    if (response != null) {
                        log.info("Google reCAPTCHA verification response: success={}, score={}, action={}, hostname={}, timestamp={}, errors={}",
                                response.isSuccess(), response.getScore(), response.getAction(),
                                response.getHostname(), response.getChallengeTs(), response.getErrorCodes());
                    } else {
                        log.warn("Received null response object from Google verification.");
                    }
                })
                .doOnError(error -> {
                    log.error("Error during reCAPTCHA verification request to Google: {} ({})",
                            (error != null ? error.getMessage() : "Unknown error"),
                            (error != null ? error.getClass().getName() : "N/A"),
                            error);
                });
    }
}