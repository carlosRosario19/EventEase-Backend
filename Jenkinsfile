pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials') // Jenkins credentials ID
        DOCKER_IMAGE_NAME = 'carlosrosariocentennial/eventease-backend' // Replace with your Docker Hub username and image name
        DOCKER_IMAGE_TAG = "latest" // Change to "build-${BUILD_NUMBER}" if you want unique tags
    }
    
    stages {
        // Stage 1: Checkout code from GitHub
        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/carlosRosario19/EventEase-Backend.git'
            }
        }



        // Stage 2: Run tests, check test coverage and build
        stage('Test and Build') {
            steps {
                echo 'Testing and building Spring Boot application with Maven Wrapper...'
                // Set execute permissions for mvnw
                sh 'chmod +x mvnw'
                // Run the build
                sh './mvnw clean verify' // Fails build if thresholds (80%) not met

                recordCoverage(
                    tools: [[parser: 'JACOCO']],
                    id: 'jacoco',
                    name: 'JaCoCo Coverage',
                    sourceCodeRetention: 'EVERY_BUILD',
                    qualityGates: [
                        [threshold: 80.0, metric: 'LINE', baseline: 'PROJECT', unstable: true],
                        [threshold: 80.0, metric: 'METHOD', baseline: 'PROJECT', unstable: true],
                        [threshold: 60.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: true]
                    ]
                )
            }
        }
        // Stage 3: SonarQube Static Code Analysis
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    sh './mvnw sonar:sonar'
                }
            }
        }
        
        // Stage 4: Build Docker Image
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}", ".")
                }
            }
        }

        // Stage 5: Login to Docker Hub
        stage('Login to Docker Hub') {
            steps {
                withDockerRegistry([credentialsId: 'docker-hub-credentials', url: 'https://index.docker.io/v1/']) {
                    echo 'Logged in to Docker Hub successfully'
                }
            }
        }

        // Stage 6: Push Docker Image to Docker Hub
        stage('Push Docker Image') {
            steps {
                withDockerRegistry([credentialsId: 'docker-hub-credentials', url: 'https://index.docker.io/v1/']) {
                    sh "docker push ${env.DOCKER_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG}"
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
