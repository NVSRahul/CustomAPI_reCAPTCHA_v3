package org.company.recaptchagate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GoogleRecaptchaResponse {
    private boolean success;
    private double score;
    private String action;
    @JsonProperty("challenge_ts")
    private String challengeTs;
    private String hostname;
    @JsonProperty("error-codes")
    private List<String> errorCodes;

    public GoogleRecaptchaResponse() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getChallengeTs() { return challengeTs; }
    public void setChallengeTs(String challengeTs) { this.challengeTs = challengeTs; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public List<String> getErrorCodes() { return errorCodes; }
    public void setErrorCodes(List<String> errorCodes) { this.errorCodes = errorCodes; }

    @Override
    public String toString() {
        return "GoogleRecaptchaResponse{" +
                "success=" + success +
                ", score=" + score +
                ", action='" + action + '\'' +
                ", challengeTs='" + challengeTs + '\'' +
                ", hostname='" + hostname + '\'' +
                ", errorCodes=" + errorCodes +
                '}';
    }
}