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
                        echo "Skipping Spring Boot application class"
                        return
                    }

                    echo "===================================="
                    echo "Processing ${sourceFile}"
                    echo "===================================="

                    def sourceCode = readFile(sourceFile)

                    def testFile = sourceFile
                            .replace("src/main/java", "src/test/java")
                            .replace(".java", "Test.java")

                    def generatedCode = null

                    def prompt = """
        Generate a JUnit 5 test class.

        Rules:

        * Use only public methods.
        * Never access private fields.
        * Include all required imports.
        * Return ONLY Java code.
        * No markdown.
        * No explanations.
        * Code must compile.

        Source:

        ${sourceCode}
        """
                    boolean valid = false

                    for (int attempt = 1; attempt <= 3; attempt++) {

                        echo "AI Generation Attempt ${attempt}"

                        def payload = JsonOutput.toJson([
                                model : "qwen2.5:7b",
                                prompt: prompt,
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

                        generatedCode = new JsonSlurper()
                                .parseText(response)
                                .response
                                .toString()
                                .trim()

                        generatedCode = generatedCode
                                .replace("```java", "")
                                .replace("```", "")
                                .trim()

                        if (generatedCode.contains("private")) {

                            echo "Rejected: private field access"

                            prompt = """
        Previous output failed.

        Reason:
        You accessed a private field.

        Generate again using ONLY public methods.

        Source:

        ${sourceCode}
        """
        continue
        }

                        if (!generatedCode.contains("@Test")) {

                            echo "Rejected: no @Test annotation"

                            prompt = """
        Previous output failed.

        Reason:
        No @Test methods were generated.

        Generate again.

        Source:

        ${sourceCode}
        """
        continue
        }

                        if (!generatedCode.trim().endsWith("}")) {

                            echo "Rejected: truncated Java file"

                            prompt = """
        Previous output failed.

        Reason:
        Generated Java was incomplete.

        Generate again.

        Source:

        ${sourceCode}
        """
        continue
        }
                        valid = true
                        break
                    }

                    if (!valid) {
                        error("Unable to generate valid test after 3 attempts")
                    }

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

            steps {
                sh './mvnw test-compile'
            }
        }

        stage('Run Tests') {

            when {
                expression {
                    env.FILES_TO_PROCESS?.trim()
                }
            }

            steps {
                sh './mvnw test'
            }
        }

        stage('Push Code To Git') {

             when {

             allOf{

                expression {
                currentBuild.currentResult == 'SUCCESS'
                }

                   expression {
                        env.FILES_TO_PROCESS?.trim()
                    }
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
                            echo "USER=$GIT_USER"
                            echo "TOKEN_LENGTH=${#GIT_TOKEN}"
                        '''

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