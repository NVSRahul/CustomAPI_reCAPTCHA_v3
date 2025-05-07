package org.company.recaptchagate.dto;

public class FormDataRecaptchaRequest {
    private String token;
    private String name;
    private String email;
    private String company;
    private String description;

    public FormDataRecaptchaRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "FormDataRecaptchaRequest{" +
                "token='" + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null") + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                '}';
    }
}