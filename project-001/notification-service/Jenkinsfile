pipeline {
    agent any
     parameters {
        string(name: 'REPO_NAME', defaultValue: 'notification-service', description: 'Name of the repository')
        string(name: 'DOCKER_IMAGE', defaultValue: 'xxxxxx/notification-service:latest', description: 'Docker image name')
    }
    environment {
        DOCKER_CREDENTIALS_ID = credentials('docker-credentials-id')
        K8S_CREDENTIALS_ID = 'app-k8-token'
        K8S_SERVER_URL = 'https://your-k8s-server-url'
        K8S_NAMESPACE = 'webapps'
    }

    stages {
        stage('Cleaning Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout Repository') {
            steps {
                git url: 'https://your-repo-url.git', branch: 'main'
            }
        }
        stage('Build') {
            environment {
                BUILD_ENV = 'production'
            }
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.BUILD_NUMBER}")
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
                        docker.image("${env.DOCKER_IMAGE}:${env.BUILD_NUMBER}").push()
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    caCertificate: '',
                    clusterName: 'EKS-1',
                    contextName: '',
                    credentialsId: 'app-k8-token',
                    namespace: 'webapps',
                    serverUrl: 'xxxxxxxxx'
                ]]) {
                    sh "kubectl apply -f deployment-service.yml"
                    sh "kubectl set image deployment/your-deployment-name your-container-name=${env.DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                    sleep 60
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    caCertificate: '',
                    clusterName: 'EKS-1',
                    contextName: '',
                    credentialsId: 'app-k8-token',
                    namespace: 'webapps',
                    serverUrl: 'xxxxxxxxx'
                ]]) {
                    sh "kubectl get svc -n webapps"
                    sleep 30
                }
            }
        }

        stage('Deploy ELK to Kubernetes') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    caCertificate: '',
                    clusterName: 'EKS-1',
                    contextName: '',
                    credentialsId: 'elk-k8-token',
                    namespace: 'elk',
                    serverUrl: 'xxxxxxxxx'
                ]]) {
                    sh "kubectl apply -f ./elk"
                    sleep 30
                }
            }
        }

        stage('Verify ELK Deployment') {
            steps {
                withKubeCredentials(kubectlCredentials: [[
                    caCertificate: '',
                    clusterName: 'EKS-1',
                    contextName: '',
                    credentialsId: 'elk-k8-token',
                    namespace: 'elk',
                    serverUrl: 'xxxxxxxxx'
                ]]) {
                    sh "kubectl get svc -n elk"
                }
            }
        }
    }
    post {
        success {
            echo 'Deployment succeeded!'
            // Add success notifications here
        }
        failure {
            echo 'Deployment failed!'
            // Add failure notifications here
        }
        always {
            cleanWs()
        }
    }
}
