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

            steps {
                script {

                    def files = env.FILES_TO_PROCESS.split("\\n")

                    files.each { sourceFile ->

                        if (sourceFile.endsWith("Application.java")) {
                            echo "Skipping Spring Boot application class: ${sourceFile}"
                            return
                        }

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
                        You are a Java code generator.

                        Generate a JUnit 5 test class.

                        STRICT RULES:

                        1. Output ONLY valid Java source code.
                        2. Do NOT explain anything.
                        3. Do NOT use markdown.
                        4. Do NOT use code fences.
                        5. Do NOT write "Here is the code".
                        6. Response MUST start with package declaration.
                        7. Every import statement must end with a semicolon.
                        8. Every Java statement must compile.
                        9. Return only one Java class.

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
                                .trim()

                        generatedCode = generatedCode
                                .replace("```java", "")
                                .replace("```", "")
                                .trim()

                        def packageIndex = generatedCode.indexOf("package ")

                        if (packageIndex > 0) {
                            generatedCode = generatedCode.substring(packageIndex)
                        }

                        def parentDir =
                                testFile.substring(
                                        0,
                                        testFile.lastIndexOf('/')
                                )

                        sh "mkdir -p '${parentDir}'"

                        generatedCode = generatedCode
                            .replace('```java', '')
                            .replace('```', '')
                            .trim()

                        echo "===== GENERATED CODE ====="
                        echo generatedCode.take(1000)
                        echo "=========================="

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
            steps {
                echo "Skipping compilation while debugging generated code"
            }
        }

        stage('Push Code To Git') {

             when {

                   expression {
                        env.FILES_TO_PROCESS?.trim()
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
                    '''

                    withCredentials([
                        usernamePassword(
                            credentialsId: 'github-pat',
                            usernameVariable: 'GIT_USER',
                            passwordVariable: 'GIT_TOKEN'
                        )
                    ]) {

                        sh '''
                            git remote set-url origin \
                            https://${GIT_USER}:${GIT_TOKEN}@github.com/scriptingmaverick/jenkins-pipeline-practice.git

                            git push -u origin ai/tests --force
                        '''
                    }
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