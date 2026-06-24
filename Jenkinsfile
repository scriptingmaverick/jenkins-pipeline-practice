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

        stage('Find Changed Files') {
            steps {
                script {

                    def changedFiles = sh(
                        script: """
                            git diff --name-only HEAD~1 HEAD \
                            | grep '^src/main/java/.*\\.java$' || true
                        """,
                        returnStdout: true
                    ).trim()

                    if (!changedFiles) {
                        echo "No Java files changed."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.CHANGED_FILES = changedFiles

                    echo "Changed files:"
                    echo changedFiles
                }
            }
        }

        stage('Generate Tests') {
            steps {
                script {

                    def files = env.CHANGED_FILES.split("\\n")

                    files.each { file ->

                        echo "Processing ${file}"

                        def sourceCode = readFile(file)

                        def payload = JsonOutput.toJson([
                                model : "qwen2.5:7b",
                                prompt: """
Generate JUnit 5 tests for the following Java class.

Requirements:
- Spring Boot 3
- JUnit 5
- Mockito where appropriate
- Return ONLY Java code

Class:

${sourceCode}
""",
                                stream: false
                        ])

                        writeFile(
                                file: "payload.json",
                                text: payload
                        )

                        def response = sh(
                                script: '''
                                    curl -s http://host.docker.internal:11434/api/generate \
                                    -H "Content-Type: application/json" \
                                    -d @payload.json
                                ''',
                                returnStdout: true
                        ).trim()

                        def result = new JsonSlurper().parseText(response)

                        echo "================================="
                        echo "Generated Test For ${file}"
                        echo "================================="
                        echo result.response
                        echo "================================="

                        env.TextFile = result.response
                    }
                }
            }
        }

        stage("writing into a file"){
            writeFile(
                file: "src/test/java/.../GeneratedTest.java",
                text: env.TextFile
            )
        }
    }
}