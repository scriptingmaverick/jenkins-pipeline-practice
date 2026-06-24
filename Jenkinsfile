        import groovy.json.JsonOutput
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

                    def payload = JsonOutput.toJson([
                        model: "qwen2.5:7b",
                        prompt: "Summarize this git diff:\n${diff}",
                        stream: false
                    ])

                    writeFile file: 'payload.json', text: payload

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