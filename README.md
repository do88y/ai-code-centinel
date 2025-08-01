# Code Sentinel: AI Code Review Assistant

## Project Overview

**Code Sentinel** is an automated AI-powered code review assistant for GitHub Pull Requests.
It leverages Anthropic's Claude Sonnet 3.7 to provide intelligent, context-aware code reviews, enforce coding standards, and streamline the PR approval process.

## Key Features

*   **Automated AI Code Review:** Triggers AI review on PR open/update.
*   **File-Based Guidelines:** AI reviews are based on a `CENTINEL.md` file located in the root of the target repository.
*   **PR Summary & Sequence Diagram:** Generates PR summaries and Mermaid sequence diagrams.
*   **Conditional Review Status:** Submits `APPROVE` or `REQUEST_CHANGES` based on AI assessment.
*   **Line-Specific Comments:** Provides detailed, line-by-line feedback on code changes.
*   **Anthropic Claude Sonnet 3.7:** Utilizes a powerful LLM for analysis.
*   **GitHub App Integration:** Securely interacts with GitHub API.

## Technology Stack

*   **Backend:** Java 17, Spring Boot 3.x (Web, WebFlux)
*   **AI Model:** Anthropic Claude Sonnet 3.7
*   **GitHub Integration:** GitHub Apps, `java-jwt`
*   **Build Tool:** Gradle
*   **Utilities:** Lombok, Mermaid

## Setup Guide

### Prerequisites

*   JDK 17+
*   Gradle
*   Git
*   Anthropic API Key
*   GitHub Account with permissions to create GitHub Apps

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/ai-code-reviewer.git
cd ai-code-reviewer
```

### 2. GitHub App Configuration

Register a GitHub App:
*   Go to `GitHub Settings` -> `Developer settings` -> `GitHub Apps` -> `New GitHub App`.
*   **Name:** `Code Sentinel`
*   **Webhook URL:** Your Spring Boot app's public URL (e.g., `https://your-server.com/webhook/github`). For local dev, use `ngrok http 8080` to get a public URL.
*   **Webhook secret:** Generate a strong random string.
*   **Permissions:** `Pull requests` (Read & write), `Contents` (Read-only), `Metadata` (Read-only).
*   **Events:** `Pull request`.
*   **Generate Private Key:** Download the `.pem` file.
*   **Note App ID.**

### 3. Application Configuration

#### Using `.envrc` (Recommended)

For secure and easy management of environment variables, we recommend using `direnv` and an `.envrc` file.

1.  **Install `direnv`:** Follow the [official installation guide](https://direnv.net/docs/installation.html).
2.  **Create `.envrc`:** Copy the example file and fill in your credentials.

    ```bash
    cp .envrc.example .envrc
    ```

3.  **Load Environment:** Allow `direnv` to load the variables.

    ```bash
    direnv allow
    ```

Your `.envrc` file should look like this (with your actual values):

```bash
export GITHUB_TOKEN="your_github_token"
export GITHUB_WEBHOOK_SECRET="your_github_webhook_secret"
export ANTHROPIC_API_KEY="your_anthropic_api_key"
export GITHUB_APP_ID="your_github_app_id"
export GITHUB_APP_PRIVATE_KEY="your_github_app_private_key"
```

#### Manual Environment Variables

If you prefer not to use `direnv`, you can set the environment variables manually in your shell configuration file (e.g., `.zshrc`, `.bash_profile`) or export them directly in your terminal session.

### 4. Run the Application

```bash
cd ai-code-reviewer
./gradlew bootRun
```
The app will start on `http://localhost:8080`. Ensure `ngrok` (if used) forwards traffic to this port.

## GitHub Actions Workflow

The `.github/workflows/ai-code-review.yml` defines the workflow. It triggers on PR events, builds the app, and is designed to send webhook events to your publicly accessible Spring Boot application.

## Usage

1.  **Install GitHub App:** Install the `Code Sentinel` app on your desired repositories.
2.  **Create `CENTINEL.md` (Optional):** In the root directory of the repository you want to review, create a `CENTINEL.md` file. Add your project-specific coding guidelines to this file. If this file doesn't exist, the AI will use general clean code principles for the review.
3.  **Create/Update PR:** Open or update a PR in an installed repository.
4.  **Observe Review:** Code Sentinel will post a summary, diagram, and a review (APPROVE/REQUEST_CHANGES) with comments.

---