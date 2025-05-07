# CustomAPI_reCAPTCHA_v3: reCAPTCHA v3 Form Protection & PDF Access Gate

## Description

This project demonstrates how to integrate Google reCAPTCHA v3 with a web form and, as a specific use case, protect access to a PDF link based on the verification. Users fill out a simple form (Name, Email, Company). Upon submission, reCAPTCHA v3 invisibly assesses the interaction. If the server-side verification (checking the reCAPTCHA score and action) is successful, the user is granted access to a PDF link.

This serves as a practical example for:
*   Implementing reCAPTCHA v3 for general form submissions to prevent spam and abuse.
*   Gating access to specific resources (like a downloadable file) based on successful reCAPTCHA verification.

The backend is built with Spring Boot, and the frontend is a simple HTML, CSS, and JavaScript application.

## Features

*   **reCAPTCHA v3 Form Integration:** Demonstrates how to secure a standard web form using invisible reCAPTCHA v3.
*   **Score-Based Access Control:** Server-side validation of the reCAPTCHA score against a configurable threshold.
*   **Action Tag Verification:** Ensures the reCAPTCHA token was generated for the specific form submission action, enhancing security.
*   **Use Case: Secure PDF Link Gating:** Provides an example of protecting a resource (PDF link) post-verification.
*   **Simple Form Submission:** Collects basic user information.
*   **Clear Frontend Feedback:** Provides status messages to the user during the verification process.
*   **Spring Boot Backend:** Robust and scalable server-side logic for reCAPTCHA verification.
*   **Vanilla JavaScript Frontend:** No complex frontend frameworks for this demo.

## Technology Stack

*   **Backend:**
    *   Java (Version 17+ recommended)
    *   Spring Boot (Web, WebFlux for `WebClient`)
    *   Maven (for dependency management)
*   **Frontend:**
    *   HTML5
    *   CSS3
    *   Vanilla JavaScript (ES6+)
*   **reCAPTCHA:**
    *   Google reCAPTCHA v3

## Prerequisites

*   **Java Development Kit (JDK):** Version 17 or higher.
*   **Apache Maven:** For building the Spring Boot application.
*   **Google reCAPTCHA v3 Keys:**
    *   Go to the [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin/).
    *   Register a new site, selecting **reCAPTCHA v3**.
    *   Add your domains (e.g., `localhost`, `127.0.0.1` for local testing, and your production domain).
    *   Note down the **Site Key** and **Secret Key**.
*   **A PDF File URL (for the PDF gating example):** The URL of the PDF you want to protect.

## Setup and Configuration

### 1. Clone the Repository

```bash
git clone https://github.com/NVSRahul/CustomAPI_reCAPTCHA_v3.git
cd CustomAPI_reCAPTCHA_v3
```

### 2. Backend Configuration (Spring Boot)

The main configuration file is `src/main/resources/application.properties`.

You **MUST** update the following properties:

```properties
# src/main/resources/application.properties

# --- reCAPTCHA v3 Settings ---
# 🚨 Replace with YOUR actual v3 Secret Key
google.recaptcha.secret=YOUR_V3_SECRET_KEY_HERE

# Define your score threshold (0.0 to 1.0) - Adjust based on testing
google.recaptcha.v3.threshold=0.5
# Define the expected action name from the frontend (should match JS)
google.recaptcha.v3.action=submit_form

# --- PDF Content URL (for the PDF gating example) ---
# 🚨 Replace with YOUR actual PDF URL
app.pdf.url=YOUR_ACTUAL_PDF_URL_HERE
```

*   `google.recaptcha.secret`: Your reCAPTCHA v3 **Secret Key**. **Keep this secure and never expose it publicly.**
*   `google.recaptcha.v3.threshold`: The minimum score (0.0 to 1.0) required for verification to pass. Start with `0.5` and adjust based on logs and testing.
*   `app.pdf.url`: The direct URL to the PDF file you want to serve after successful verification (specific to the PDF gating use case).

The backend server port is set to `8080` by default but can be changed in this file (`server.port=8080`).

### 3. Frontend Configuration

The main frontend files are `index.html`, `style.css`, and `script.js` (located in the project root for this simple setup).

You **MUST** update the following in `index.html`:

```html
<!-- index.html -->

<!-- 🚨 reCAPTCHA v3 API Script - Replace YOUR_V3_SITE_KEY_HERE -->
<script src="https://www.google.com/recaptcha/api.js?render=YOUR_V3_SITE_KEY_HERE"></script>
<script>
  // 🚨 Replace YOUR_V3_SITE_KEY_HERE again
  const RECAPTCHA_V3_SITE_KEY = 'YOUR_V3_SITE_KEY_HERE';
</script>
```

*   Replace `YOUR_V3_SITE_KEY_HERE` in **both** places with your reCAPTCHA v3 **Site Key**.

If your backend runs on a different URL or port, update `BACKEND_VERIFY_URL` in `script.js`:
```javascript
// script.js
const BACKEND_VERIFY_URL = 'http://localhost:8080/api/verify-v3-submit';
```

## Running the Application

### 1. Run the Backend (Spring Boot)

Navigate to the project's root directory (where `pom.xml` is located) and run:

```bash
./mvnw spring-boot:run
```
(On Windows, use `mvnw.cmd spring-boot:run`)

The backend server should start, typically on `http://localhost:8080`. Check the console logs for the exact port and any startup messages.

### 2. Run the Frontend

Open the `index.html` file directly in your web browser (e.g., by double-clicking it or using a live server extension in your code editor like VS Code's "Live Server").

*   Ensure your browser can make requests to `http://localhost:8080` (or wherever your backend is running). The `@CrossOrigin` annotation in `RecaptchaController.java` is configured for common local development origins, but you might need to adjust it for your specific setup or production environment.

## How it Works

1.  The user visits `index.html`.
2.  The user fills in the Name, Email, and Company fields.
3.  Upon clicking "Submit & Get PDF":
    a.  JavaScript prevents the default form submission.
    b.  `grecaptcha.execute()` is called with your v3 Site Key and an action `'submit_form'`. This returns a reCAPTCHA token.
    c.  The frontend sends the reCAPTCHA `token` and the form data (`name`, `email`, `company`) to the backend API endpoint (`/api/verify-v3-submit`).
4.  The Spring Boot backend:
    a.  Receives the token and form data.
    b.  Sends the `token` and your **v3 Secret Key** to Google's `siteverify` API.
    c.  Google responds with a JSON object containing `success` (true/false), `score` (0.0-1.0), `action`, and `hostname`.
    d.  The backend checks if:
        i.  `success` is true.
        ii. The `score` is greater than or equal to the configured `google.recaptcha.v3.threshold`.
        iii. The `action` in Google's response matches the expected `google.recaptcha.v3.action` (e.g., `'submit_form'`).
    e.  If all checks pass, the backend logs the form data (optional). For the PDF gating use case, it then responds to the frontend with `{ "success": true, "pdfUrl": "YOUR_CONFIGURED_PDF_URL" }`. If only form protection is needed, it could just respond with `{ "success": true }`.
    f.  If any check fails, it responds with `{ "success": false, "reason": "Detailed error message" }` and an appropriate HTTP status code (e.g., 403 Forbidden).
5.  The frontend JavaScript:
    a.  If the backend response indicates success (and a `pdfUrl` is provided for the PDF use case), it displays the PDF viewer (`<iframe>`) and an active download link.
    b.  If verification fails, it shows an error message to the user.

## Important Notes & Security

*   **Secret Key Security:** Your `google.recaptcha.secret` (v3 Secret Key) **must be kept confidential**. Do not commit it to public repositories. Use environment variables or a secure secrets management system for production deployments.
*   **Site Key Exposure:** The reCAPTCHA Site Key is public and will be visible in your frontend JavaScript. This is by design.
*   **Threshold Tuning:** The `google.recaptcha.v3.threshold` (default `0.5`) is crucial. Monitor the scores from legitimate users and potential bots (visible in your backend logs) to fine-tune this value. Too low might let bots through; too high might block legitimate users.
*   **Action Specificity:** Using and verifying `action` tags provides context for the reCAPTCHA score and helps mitigate token theft or misuse across different parts of a site.
*   **CORS:** The `@CrossOrigin` annotation in `RecaptchaController.java` allows requests from specified origins. Ensure this is configured correctly for your development and production frontend URLs.
*   **HTTPS:** For production, always use HTTPS for both your frontend and backend to protect data in transit, including the reCAPTCHA token. reCAPTCHA itself works best over HTTPS.
*   **PDF Security (Specific Use Case):** This example gates access to the *link*. If the PDF URL is guessable or becomes known, this system won't prevent direct access to that URL. For higher security on the PDF itself, consider:
    *   Serving the PDF through a protected backend endpoint that checks for a valid session/token *before* streaming the file.
    *   Using temporary, signed URLs if your PDF is hosted on a cloud storage service (like AWS S3, Google Cloud Storage).

## (Optional) Future Enhancements

*   Store submitted form data in a database.
*   Send an email notification upon successful form submission.
*   Implement more sophisticated error handling and user feedback.
*   Add user authentication for an additional layer of security.
*   If gating files, serve the file directly through a secured Spring Boot endpoint instead of just providing a URL.

---

Feel free to contribute or report issues via the [Issues tab](https://github.com/NVSRahul/CustomAPI_reCAPTCHA_v3/issues)!
```
