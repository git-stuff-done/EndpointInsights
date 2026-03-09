pipeline {
    agent {
        label 'k8s-maven-node'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }
        
        stage('Backend Build') {
            steps {
                echo 'Building the backend project...'
                sh 'cd endpoint-insights-api && mvn clean compile'
            }
        }
        
        stage('Backend Unit Tests') {
            steps {
                echo 'Running backend unit tests...'
                sh 'cd endpoint-insights-api && mvn test'
            }
        }
        
        stage('Frontend Dependencies') {
            steps {
                echo 'Installing frontend dependencies...'
                sh 'cd endpoint-insights-ui && npm ci'
            }
        }
        
        stage('Frontend Build') {
            steps {
                echo 'Building the frontend project...'
                sh 'cd endpoint-insights-ui && npm run build'
            }
        }
        
        stage('Frontend Unit Tests') {
            steps {
                echo 'Running frontend unit tests...'
                sh 'cd endpoint-insights-ui && npm run test:ci'
            }
        }
    }
    
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
