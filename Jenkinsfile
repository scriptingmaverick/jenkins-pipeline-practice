import groovy.json.JsonOutput
import groovy.json.JsonSlurper

pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Get Diff') {
            steps {
                script {
                    env.GIT_DIFF = sh(
                        script: 'git diff HEAD~1 HEAD',
                        returnStdout: true
                    ).trim()

                    echo "===== FILES CHANGED ====="
                    sh 'git diff --name-status HEAD~1 HEAD'
                }
            }
        }

        stage('Ask Ollama') {
            steps {
                script {

                    def payload = JsonOutput.toJson([
                        model : "qwen2.5:7b",
                        prompt: """
You are a senior software engineer.

Analyze the following git diff and provide:

1. Summary
2. Files affected
3. Risk level (LOW/MEDIUM/HIGH)
4. Suggested tests

Git Diff:

${env.GIT_DIFF}
""",
                        stream: false
                    ])

                    writeFile file: 'payload.json', text: payload

                    def response = sh(
                        script: '''
                        curl -s http://host.docker.internal:11434/api/generate \
                          -H "Content-Type: application/json" \
                          -d @payload.json
                        ''',
                        returnStdout: true
                    ).trim()

                    def result = new JsonSlurper().parseText(response)

                    echo ""
                    echo "=========================================="
                    echo "AI CHANGE SUMMARY"
                    echo "=========================================="
                    echo result.response
                    echo "=========================================="
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully."
        }

        failure {
            echo "Pipeline failed."
        }
    }
}