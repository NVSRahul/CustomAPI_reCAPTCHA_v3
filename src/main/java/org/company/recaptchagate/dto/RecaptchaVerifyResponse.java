package org.company.recaptchagate.dto;

public class RecaptchaVerifyResponse {
    private boolean success;
    private String pdfUrl;
    private String reason; // Optional reason for failure

    public RecaptchaVerifyResponse() {}

    public RecaptchaVerifyResponse(boolean success, String pdfUrl, String reason) {
        this.success = success;
        this.pdfUrl = pdfUrl;
        this.reason = reason;
    }

    public RecaptchaVerifyResponse(boolean success, String pdfUrl) {
        this(success, pdfUrl, null);
    }

    public RecaptchaVerifyResponse(boolean success, String reason, boolean isFailureReason) {
        this(success, null, reason);
    }

    public RecaptchaVerifyResponse(boolean success) {
        this(success, null, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}