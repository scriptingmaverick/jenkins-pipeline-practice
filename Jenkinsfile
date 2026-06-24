pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Ask Ollama') {
            steps {
                script {

                    def diff = sh(
                        script: 'git diff HEAD~1 HEAD',
                        returnStdout: true
                    ).trim()

                    writeFile file: 'payload.json', text: """
        {
            "model":"qwen2.5:7b",
            "prompt":"Summarize the following git diff:\\n${diff.replace('"','\\\\\\"')}",
            "stream":false
        }
        """

                    sh '''
                    curl http://host.docker.internal:11434/api/generate \
                      -H "Content-Type: application/json" \
                      -d @payload.json
                    '''
                }
            }
        }
    }
}