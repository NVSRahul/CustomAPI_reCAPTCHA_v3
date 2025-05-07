# 🛡️ CustomAPI_reCAPTCHA_v3: Secure Your Forms & Gate Content with Google reCAPTCHA v3

[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.x-orange.svg)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) <!-- Optional: Add a license if you have one -->

Stop spam and protect your resources! This project provides a clear, practical demonstration of integrating **Google reCAPTCHA v3** with a web form. It showcases how to:
*   Secure any HTML form against bots and abuse using invisible, score-based verification.
*   Gate access to valuable content (like a PDF download) until a user successfully passes the reCAPTCHA challenge.

Built with a **Spring Boot** backend and a clean **Vanilla JavaScript** frontend, it's designed to be easy to understand, set up, and adapt for your own projects.

---

## ✨ Key Features

*   **🤖 Invisible Form Protection:** Seamlessly secure your web forms with Google reCAPTCHA v3, without interrupting legitimate users.
*   **💯 Score-Based Verification:** Leverage Google's risk analysis engine to get a score for each interaction, and make decisions on your server.
*   **🎯 Action-Specific Security:** Use reCAPTCHA actions to tie verifications to specific user behaviors (e.g., `submit_form`), making it harder for attackers to misuse tokens.
*   **📄 Content Gating Example:** A ready-to-run example of how to protect a PDF link, granting access only after successful verification.
*   **📝 Simple Data Collection:** Includes a basic form to gather user details (Name, Email, Company).
*   **💬 User-Friendly Feedback:** Clear status messages guide the user through the verification process.
*   **☕ Robust Java Backend:** Powered by Spring Boot for a reliable and scalable server-side solution.
*   **🌐 Lightweight Frontend:** Pure HTML, CSS, and JavaScript – no complex frameworks needed for this core functionality.

---

## 🛠️ Tech Stack

*   **Backend:** Java (17+), Spring Boot, Spring WebFlux (for `WebClient`), Maven
*   **Frontend:** HTML5, CSS3, Vanilla JavaScript (ES6+)
*   **Verification:** Google reCAPTCHA v3

---

## 🚀 Getting Started

### Prerequisites

1.  **Java Development Kit (JDK):** Version 17 or higher.
2.  **Apache Maven:** To build and run the Spring Boot application.
3.  **Google reCAPTCHA v3 Keys:**
    *   Visit the [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin/).
    *   Register a **new site**, choosing **reCAPTCHA v3**.
    *   Ensure your domains (e.g., `localhost`, `127.0.0.1` for local tests, and your production domain) are added.
    *   Safely store your **Site Key** and **Secret Key**.
4.  **(Optional) PDF URL:** If you're using the PDF gating feature, have a URL for the PDF you wish to protect.

### 1. Clone the Magic ✨

```bash
git clone https://github.com/NVSRahul/CustomAPI_reCAPTCHA_v3.git
cd CustomAPI_reCAPTCHA_v3
```

### 2. Configure Your Backend Secrets 🔒

Edit `src/main/resources/application.properties` with your details:

```properties
# src/main/resources/application.properties

# === Google reCAPTCHA v3 ===
# 🚨 YOUR V3 SECRET KEY - Keep this super secret!
google.recaptcha.secret=YOUR_V3_SECRET_KEY_HERE
# Minimum score (0.0-1.0) to pass. Adjust based on testing.
google.recaptcha.v3.threshold=0.5
# Expected action from frontend (must match JS `grecaptcha.execute` action).
google.recaptcha.v3.action=submit_form

# === PDF Gating Example ===
# 🚨 URL of the PDF you want to protect.
app.pdf.url=YOUR_ACTUAL_PDF_URL_HERE

# Optional: Server port
# server.port=8080
```
**Important:** Your `google.recaptcha.secret` is confidential. Never commit it to a public repository. For production, use environment variables or a secure secret management tool.

### 3. Configure Your Frontend Keys 🔑

Open `index.html` and update it with your reCAPTCHA v3 **Site Key**:

```html
<!-- index.html -->

<!-- 🚨 Replace with YOUR V3 SITE KEY -->
<script src="https://www.google.com/recaptcha/api.js?render=YOUR_V3_SITE_KEY_HERE"></script>
<script>
  // 🚨 Replace with YOUR V3 SITE KEY again
  const RECAPTCHA_V3_SITE_KEY = 'YOUR_V3_SITE_KEY_HERE';
</script>
```
If your backend isn't running on `http://localhost:8080`, also update `BACKEND_VERIFY_URL` in `script.js`.

### 4. Ignite the Engines! 🔥

**Start the Backend (Spring Boot):**
In the project root directory:
```bash
./mvnw spring-boot:run
# Or on Windows: mvnw.cmd spring-boot:run
```
Your backend should be live, typically at `http://localhost:8080`.

**Launch the Frontend:**
Open `index.html` directly in your web browser. (Using a live server extension in your IDE is also a great option!)

---

## ⚙️ How It Works: The Flow

1.  **User Interaction:** User fills the form on `index.html`.
2.  **Token Generation:** On submit, JavaScript calls `grecaptcha.execute()` with your Site Key and an `action` (e.g., `'submit_form'`) to get a secure token.
3.  **API Call:** The frontend sends this token and form data to your Spring Boot backend (`/api/verify-v3-submit`).
4.  **Server-Side Verification (The Crucial Step!):**
    *   Your backend sends the token and your **Secret Key** to Google's `siteverify` API.
    *   Google assesses the interaction and returns a `success` status, a `score` (0.0-1.0), the `action` it observed, and other details.
    *   Your backend rigorously checks:
        *   Is `success` true?
        *   Is the `score` >= your defined `threshold`?
        *   Does the `action` match what you expected?
5.  **Access Granted (or Denied):**
    *   ✅ **If all checks pass:** The backend confirms success. For the PDF example, it returns the `pdfUrl`.
    *   ❌ **If any check fails:** Access is denied, and an error message is provided.
6.  **Frontend Update:** JavaScript updates the page to show the PDF (if applicable) or an error message.

---

## 💡 Important Considerations

*   **Secret Key is SACRED:** Guard your `google.recaptcha.secret`. It's the key to your castle.
*   **Site Key is Public:** It's meant to be in your frontend code.
*   **Tune Your Threshold:** The `google.recaptcha.v3.threshold` is a balancing act. Monitor scores (check backend logs!) to find the sweet spot for your traffic.
*   **Actions Add Context:** Always use and verify `action` tags. They make your reCAPTCHA setup much stronger.
*   **CORS & HTTPS:** Configure Cross-Origin Resource Sharing (`@CrossOrigin`) correctly. Always use HTTPS in production for security.
*   **Beyond the Link (PDF Security):** This example gates the *link*. For truly secure file access, consider serving files through a protected backend endpoint or using temporary signed URLs.

---

## 🌱 Future Possibilities

*   Persist form submissions to a database.
*   Trigger email notifications.
*   Implement full user authentication.
*   Serve files directly and securely via Spring Boot.

---

## 🤝 Contributing & Issues

Found a bug or have an idea? Feel free to:
*   Open an [Issue](https://github.com/NVSRahul/CustomAPI_reCAPTCHA_v3/issues)
*   Submit a Pull Request

We appreciate your contributions!

---

Happy Coding! 🚀
