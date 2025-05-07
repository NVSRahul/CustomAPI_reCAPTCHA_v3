package org.company.recaptchagate.controller;

import org.company.recaptchagate.dto.FormDataRecaptchaRequest;
import org.company.recaptchagate.dto.RecaptchaVerifyResponse;
import org.company.recaptchagate.service.RecaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:63343", "http://127.0.0.1:63343", "http://localhost:5500", "http://localhost:63342", "http://127.0.0.1:63342", "null"})
public class RecaptchaController {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaController.class);

    private final RecaptchaService recaptchaService;

    @Value("${app.pdf.url}")
    private String pdfUrl;

    @Value("${google.recaptcha.v3.threshold}")
    private double recaptchaV3Threshold;

    @Value("${google.recaptcha.v3.action}")
    private String recaptchaV3Action;

    public RecaptchaController(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @PostMapping("/verify-v3-submit")
    public Mono<ResponseEntity<RecaptchaVerifyResponse>> verifyRecaptchaV3AndSubmit(@RequestBody FormDataRecaptchaRequest request) {
        log.info("Handling v3 verification and form submit request: {}", request);

        if (request == null || request.getToken() == null || request.getToken().isEmpty()) {
            log.warn("v3: Received empty or invalid reCAPTCHA token.");
            return Mono.just(ResponseEntity.badRequest().body(new RecaptchaVerifyResponse(false, "Missing token"))); // 400
        }

        return recaptchaService.verifyRecaptcha(request.getToken())
                .map(googleResponse -> {
                    if (googleResponse == null) {
                        log.error("v3: Received null response from Google verification service.");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RecaptchaVerifyResponse(false, "Internal verification error")); // 500
                    }

                    String failureReason = null;
                    boolean verificationPassed = false;

                    if (!googleResponse.isSuccess()) {
                        failureReason = "Google verification failed. Errors: " + googleResponse.getErrorCodes();
                    } else if (!recaptchaV3Action.equals(googleResponse.getAction())) {
                        failureReason = "Action mismatch. Expected '" + recaptchaV3Action + "' but got '" + googleResponse.getAction() + "'";
                    } else if (googleResponse.getScore() < recaptchaV3Threshold) {
                        failureReason = "Score " + googleResponse.getScore() + " below threshold " + recaptchaV3Threshold;
                    } else {
                        verificationPassed = true; // All checks passed
                    }

                    if (verificationPassed) {
                        log.info("v3: Verification SUCCEEDED (Score: {}, Action: '{}'). Processing form data...",
                                googleResponse.getScore(), googleResponse.getAction());
                        log.info("v3: Form Data Received - Name: {}, Email: {}, Company: {}, Description: {}",
                                request.getName(), request.getEmail(), request.getCompany(), request.getDescription());
                        // Optional: Add form data processing logic here (DB save, email, etc.)
                        return ResponseEntity.ok(new RecaptchaVerifyResponse(true, pdfUrl)); // 200 OK
                    } else {
                        log.warn("v3: Verification FAILED. Reason: {}", failureReason);
                        // Return 403 Forbidden for score/action/verification failures
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new RecaptchaVerifyResponse(false, failureReason));
                    }
                })
                .onErrorResume(e -> {
                    log.error("v3: Error occurred during communication with Google verification service: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RecaptchaVerifyResponse(false, "Internal verification error"))); // 500
                });
    }
}