pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'config/pipeline',
                url: 'https://github.com/carlosRosario19/EventEase-Backend.git'
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building Spring Boot application with Maven Wrapper...'
                // Set execute permissions for mvnw
                sh 'chmod +x mvnw'
                // Run the build
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests with Maven Wrapper...'
                sh './mvnw test'
            }
        }
        
        stage('Deploy') {
            environment {
                // Docker Hub credentials (store these in Jenkins credentials)
                DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')

                // Image name (replace with your Docker Hub username)
                DOCKER_IMAGE = 'carlosrosariocentennial/eventease-backend'
    
                // Version/tag (using timestamp + build number)
                IMAGE_TAG = "${new Date().format('yyyyMMdd-HHmmss')}-${env.BUILD_NUMBER}"
            }

            steps {
                script {
                    echo 'Building Docker image...'

                    // Build Docker image using the Dockerfile
                    docker.build("${DOCKER_IMAGE}:${IMAGE_TAG}")

                    echo 'Logging in to Docker Hub...'
                    // Authenticate with Docker Hub
                    sh "echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin"

                    echo 'Pushing Docker image...'
                    // Push the image
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${IMAGE_TAG}").push()

                        // Optionally push as 'latest' too
                        docker.image("${DOCKER_IMAGE}:${IMAGE_TAG}").push('latest')
                    }

                    echo 'Docker image pushed successfully!'
                }
            }
    }
    
    post {
        always {
            echo 'This will always run at the end, regardless of success or failure'
            sh 'docker logout'
        }
        success {
            echo 'This will only run if all stages completed successfully'
        }
        failure {
            echo 'This will only run if any stage failed'
        }
    }
}
