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
                git branch: 'config/pipeline',
                url: 'https://github.com/carlosRosario19/EventEase-Backend.git'
            }
        }

        // Stage 2: Build the Maven project
        stage('Build') {
            steps {
                echo 'Building Spring Boot application with Maven Wrapper...'
                // Set execute permissions for mvnw
                sh 'chmod +x mvnw'
                // Run the build
                sh './mvnw clean package -DskipTests'
            }
        }

        // Stage 3: Run tests
        stage('Test') {
            steps {
                echo 'Running tests with Maven Wrapper...'
                sh './mvnw test'
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
                    bat "docker push %DOCKER_IMAGE_NAME%:%DOCKER_IMAGE_TAG%"
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
