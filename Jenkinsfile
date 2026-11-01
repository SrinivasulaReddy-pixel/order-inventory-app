pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        DOCKER_REGISTRY = 'your-dockerhub-username' // or your ECR URI
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn -B clean verify'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['eureka-server', 'api-gateway', 'auth-service',
                                     'order-service', 'inventory-service', 'notification-service']
                    services.each { svc ->
                        sh "docker build -t ${DOCKER_REGISTRY}/${svc}:${IMAGE_TAG} ${svc}"
                    }
                }
            }
        }

        stage('Push Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-registry-creds',
                                                    usernameVariable: 'DOCKER_USER',
                                                    passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    script {
                        def services = ['eureka-server', 'api-gateway', 'auth-service',
                                         'order-service', 'inventory-service', 'notification-service']
                        services.each { svc ->
                            sh "docker push ${DOCKER_REGISTRY}/${svc}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                // Placeholder: swap for `aws ecs update-service ...` once you wire up AWS.
                echo "Deploy step - update docker-compose.yml image tags or trigger ECS service update here."
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
    }
}
