const BACKEND_VERIFY_URL = 'http://localhost:8080/api/verify-v3-submit';
// RECAPTCHA_V3_SITE_KEY is loaded from a global scope (index.html)

const form = document.getElementById('pdf-request-form');
const submitButton = document.getElementById('submit-button');
const pdfContainer = document.querySelector('.pdf-container');
const downloadArea = document.querySelector('.download-area');
const pdfViewer = document.getElementById('pdf-viewer');
const downloadLink = document.getElementById('download-link');
const statusMessageEl = document.getElementById('status-message');

let pdfUrlFromBackend = null;

function showStatus(message, isError = false) {
    if (statusMessageEl) {
        statusMessageEl.textContent = message;
        statusMessageEl.className = isError ? 'error' : (message ? 'success' : '');
    }
    console.log(`Status: ${message} ${isError ? '(Error)' : ''}`);
}

async function handleFormSubmit(event) {
    event.preventDefault();
    showStatus("Verifying...");
    submitButton.disabled = true;
    submitButton.classList.add('disabled');

    if (typeof grecaptcha === 'undefined' || typeof grecaptcha.execute === 'undefined') {
        showStatus("reCAPTCHA script not loaded correctly. Please refresh.", true);
        submitButton.disabled = false;
        submitButton.classList.remove('disabled');
        return;
    }

    try {
        const token = await grecaptcha.execute(RECAPTCHA_V3_SITE_KEY, { action: 'submit_form' });
        console.log("Generated reCAPTCHA v3 token:", token.substring(0, 10) + "...");

        const formData = new FormData(form);
        const name = formData.get('name');
        const email = formData.get('email');
        const company = formData.get('company');

        const response = await fetch(BACKEND_VERIFY_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                token: token,
                name: name,
                email: email,
                company: company
            })
        });

        const result = await response.json();

        if (!response.ok) {
            const errorReason = result?.reason || `Server responded with ${response.status}`;
            console.error("Server verification failed:", errorReason, result);
            showStatus(`Verification failed: ${errorReason}. Please try again.`, true);
            submitButton.disabled = false;
            submitButton.classList.remove('disabled');
            return;
        }

        if (result.success && result.pdfUrl) {
            showStatus("Verification successful! Loading PDF...");
            pdfUrlFromBackend = result.pdfUrl;

            if (pdfViewer) pdfViewer.src = pdfUrlFromBackend;
            if (pdfContainer) pdfContainer.classList.remove('initially-hidden');
            if (downloadArea) downloadArea.classList.remove('initially-hidden');
            if (downloadLink) {
                downloadLink.href = pdfUrlFromBackend;
                downloadLink.classList.remove('disabled');
                try {
                    const urlParts = new URL(pdfUrlFromBackend);
                    const pathnameParts = urlParts.pathname.split('/');
                    downloadLink.download = pathnameParts[pathnameParts.length - 1] || 'document.pdf';
                } catch (e) {
                    downloadLink.download = 'document.pdf';
                }
            }

        } else {
            showStatus("Verification failed (unexpected response). Please try again.", true);
            console.warn("Verification failed with unexpected success=false:", result);
            submitButton.disabled = false;
            submitButton.classList.remove('disabled');
        }

    } catch (error) {
        showStatus("An error occurred during verification. Check console and try again.", true);
        console.error('Verification process error:', error);
        submitButton.disabled = false;
        submitButton.classList.remove('disabled');
    }
}

function hideContent() {
    if (pdfContainer) pdfContainer.classList.add('initially-hidden');
    if (downloadArea) downloadArea.classList.add('initially-hidden');
    if (downloadLink) {
        downloadLink.classList.add('disabled');
        downloadLink.removeAttribute('href');
        downloadLink.removeAttribute('download');
    }
    if (pdfViewer) pdfViewer.src = '';
    pdfUrlFromBackend = null;
    showStatus('');
}

if (form) {
    form.addEventListener('submit', handleFormSubmit);
} else {
    console.error("Form element #pdf-request-form not found.");
}

document.addEventListener('DOMContentLoaded', () => {
    hideContent();
});

if (typeof grecaptcha !== 'undefined') {
    grecaptcha.ready(function() {
        console.log("reCAPTCHA v3 script is ready.");
        if (!RECAPTCHA_V3_SITE_KEY || RECAPTCHA_V3_SITE_KEY === 'YOUR_V3_SITE_KEY_HERE') {
            console.error("reCAPTCHA Site Key is not set correctly in index.html script tag!");
            showStatus("Configuration error: reCAPTCHA Site Key missing.", true);
            if(submitButton) submitButton.disabled = true; // Disable form if key is missing
        }
    });
} else {
    console.error("grecaptcha object not found. Check script loading order and API key.");
    showStatus("Error loading reCAPTCHA. Please refresh.", true);
}