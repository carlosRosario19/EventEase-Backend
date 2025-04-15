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
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                echo 'This is the Test stage - where we would run tests'
                // In a real pipeline, we might use:
                // sh './mvnw test'
            }
        }
        
        stage('Deploy') {
            steps {
                echo 'This is the Deploy stage - where we would deploy the application'
                // In a real pipeline, we might push Docker images here
            }
        }
    }
    
    post {
        always {
            echo 'This will always run at the end, regardless of success or failure'
        }
        success {
            echo 'This will only run if all stages completed successfully'
        }
        failure {
            echo 'This will only run if any stage failed'
        }
    }
}
