pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                echo 'This is the Checkout stage - where we would get code from Git'
                // In a real pipeline, we would use:
                // git branch: 'main', url: 'https://github.com/your-repo/your-project.git'
            }
        }
        
        stage('Build') {
            steps {
                echo 'This is the Build stage - where we would compile the code'
                // In a real pipeline, we might use:
                // sh './mvnw clean package' for Maven projects
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
