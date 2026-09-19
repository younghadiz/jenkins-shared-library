#!/usr/bin/env groovy

def call(
    String appDir = '.',
    String credentialsId = 'github-token',
    String repositoryUrl = '',
    String targetBranch = '',
    String commitMessage = 'ci: version bump',
    String gitUserName = 'jenkins',
    String gitUserEmail = 'jenkins@example.com'
) {
    if (!repositoryUrl?.trim()) {
        error 'Git repository URL is required.'
    }

    def branch = targetBranch?.trim()
        ? targetBranch.trim()
        : env.BRANCH_NAME

    if (!branch?.trim()) {
        error 'Unable to determine the Git branch for version commit.'
    }

    dir(appDir) {
        echo 'Committing application version update...'
        echo "Target branch: ${branch}"

        withCredentials([
            usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'GIT_USER',
                passwordVariable: 'GIT_PASS'
            )
        ]) {
            withEnv([
                "GIT_REPOSITORY_URL=${repositoryUrl}",
                "GIT_TARGET_BRANCH=${branch}",
                "GIT_COMMIT_MESSAGE=${commitMessage}",
                "GIT_COMMITTER_NAME=${gitUserName}",
                "GIT_COMMITTER_EMAIL=${gitUserEmail}"
            ]) {
                sh '''
                    set -e

                    git config user.email "$GIT_COMMITTER_EMAIL"
                    git config user.name "$GIT_COMMITTER_NAME"

                    echo "Git status before version commit:"
                    git status --short

                    echo "Current Git branch information:"
                    git branch --show-current || true

                    git add pom.xml

                    if git diff --cached --quiet; then
                        echo "No version change to commit."
                        exit 0
                    fi

                    git commit -m "$GIT_COMMIT_MESSAGE"

                    echo "Pushing version commit to $GIT_TARGET_BRANCH..."

                    GIT_ASKPASS="$(mktemp)"
                    export GIT_ASKPASS
                    export GIT_TERMINAL_PROMPT=0

                    cat > "$GIT_ASKPASS" <<'EOF'
#!/bin/sh

case "$1" in
    *Username*)
        printf '%s\n' "$GIT_USER"
        ;;
    *Password*)
        printf '%s\n' "$GIT_PASS"
        ;;
esac
EOF

                    chmod 700 "$GIT_ASKPASS"

                    trap 'rm -f "$GIT_ASKPASS"' EXIT

                    git push \
                        "$GIT_REPOSITORY_URL" \
                        "HEAD:$GIT_TARGET_BRANCH"
                '''
            }
        }
    }
}
