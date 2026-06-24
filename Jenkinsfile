import groovy.json.JsonOutput
import groovy.json.JsonSlurper

pipeline {
    agent any

    environment {
        SKIP_PIPELINE = "false"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Find Files To Process') {
            steps {
                script {

                    def filesToProcess = []

                    echo "===== BOOTSTRAP MODE ====="

                    def allJavaFiles = sh(
                        script: '''
                            find src/main/java -name "*.java"
                        ''',
                        returnStdout: true
                    ).trim()

                    if (allJavaFiles) {

                        allJavaFiles.split("\\n").each { sourceFile ->

                            def testFile = sourceFile
                                    .replace("src/main/java", "src/test/java")
                                    .replace(".java", "Test.java")

                            def exists = sh(
                                script: """
                                    git ls-files '${testFile}'
                                """,
                                returnStdout: true
                            ).trim()

                            if (!exists) {

                                echo "Missing test detected:"
                                echo testFile

                                filesToProcess.add(sourceFile)
                            }
                        }
                    }

                    echo "===== INCREMENTAL MODE ====="

                    def changedFiles = sh(
                        script: '''
                            git diff --name-only HEAD~1 HEAD \
                            | grep '^src/main/java/.*\\.java$' || true
                        ''',
                        returnStdout: true
                    ).trim()

                    if (changedFiles) {

                        changedFiles.split("\\n").each { file ->

                            if (!filesToProcess.contains(file)) {
                                filesToProcess.add(file)
                            }
                        }
                    }

                    if (filesToProcess.isEmpty()) {

                        echo "No files require test generation."

                        env.SKIP_PIPELINE = "true"
                        return
                    }

                    env.FILES_TO_PROCESS =
                            filesToProcess.join("\n")

                    echo "===== FILES TO PROCESS ====="
                    echo env.FILES_TO_PROCESS
                }
            }
        }

        stage('Generate Tests') {

             when {
                    expression {
                        env.FILES_TO_PROCESS?.trim()
                    }
             }

            when {
                expression {
                    env.SKIP_PIPELINE != "true"
                }
            }

            steps {
                script {

                    def files = env.FILES_TO_PROCESS.split("\\n")

                    files.each { sourceFile ->

                        echo "===================================="
                        echo "Processing ${sourceFile}"
                        echo "===================================="

                        def sourceCode = readFile(sourceFile)

                        def testFile = sourceFile
                                .replace("src/main/java", "src/test/java")
                                .replace(".java", "Test.java")

                        def payload = JsonOutput.toJson([
                                model : "qwen2.5:7b",
                                prompt: """
        You are a senior Spring Boot developer.

        Generate a complete JUnit 5 test class.

        Requirements:
        - Spring Boot 3
        - JUnit 5
        - Mockito when necessary
        - Correct package declaration
        - Correct imports
        - Return ONLY Java code
        - No markdown
        - No explanations

        Source Class:

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

                        def generatedCode = new JsonSlurper()
                                .parseText(response)
                                .response
                                .toString()

                        def parentDir =
                                testFile.substring(
                                        0,
                                        testFile.lastIndexOf('/')
                                )

                        sh "mkdir -p '${parentDir}'"

                        writeFile(
                                file: testFile,
                                text: generatedCode
                        )

                        echo "Generated:"
                        echo testFile
                    }
                }
            }
        }

        stage('Compile Generated Tests') {

            when {
                expression {
                    env.FILES_TO_PROCESS?.trim()
                }
            }

            when {
                expression {
                    env.SKIP_PIPELINE != "true"
                }
            }

            steps {
                sh './mvnw test-compile'
            }
        }

        stage('Push Code To Git') {

             when {
                    expression {
                        env.FILES_TO_PROCESS?.trim()
                    }
                }

            when {
                expression {
                    env.SKIP_PIPELINE != "true"
                }
            }

            steps {
                script {

                    sh '''
                        git config user.name "Jenkins AI"
                        git config user.email "jenkins@example.com"

                        git checkout -B ai/tests

                        git add src/test/java

                        git diff --cached --quiet || git commit -m "[AI] Generate tests"

                        git push -u origin ai/tests --force
                    '''
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