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
                    env.DIFF = sh(
                        script: "git diff HEAD~1 HEAD",
                        returnStdout: true
                    ).trim()
                }
            }
        }

        stage('Ask Ollama') {
            steps {
                sh """
curl http://host.docker.internal:11434/api/generate \
-d '{
  "model":"qwen3:latest",
  "prompt":"Summarize this git diff:\\n${DIFF}",
  "stream":false
}'
"""
            }
        }
    }
}