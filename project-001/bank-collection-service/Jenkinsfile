pipeline {
    agent any
    parameters {
        string(name: 'REPO_NAME', defaultValue: 'bank-collection-service', description: 'Name of the repository')
        string(name: 'DOCKER_IMAGE', defaultValue: 'xxxxxx/bank-collection-service:latest', description: 'Docker image name')
    }
    environment {
        DOCKER_CREDENTIALS_ID = credentials('docker-credentials-id')
        K8S_CREDENTIALS_ID = 'app-k8-token'
        K8S_SERVER_URL = 'https://your-k8s-server-url'
        K8S_NAMESPACE = 'webapps'
        GO_VERSION = '1.19' 
    }
    tools {
        go "${GO_VERSION}"
    }
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
        stage('Checkout Repository') {
            steps {
                git url: 'https://your-repo-url.git', branch: 'main'
            }
        }
        stage('Set Up Go Environment') {
            steps {
                script {
                    def goRoot = tool name: "${GO_VERSION}", type: 'go'
                    env.GOROOT = goRoot
                    env.PATH = "${goRoot}/bin:${env.PATH}"
                }
            }
        }
        stage('Build') {
            steps {
                sh 'go build -o app ./cmd/main.go'
            }
        }
        stage('Test') {
            steps {
                sh 'go test ./...'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}")
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
                        docker.image("${DOCKER_IMAGE}").push()
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: K8S_CREDENTIALS_ID, serverUrl: K8S_SERVER_URL]) {
                    sh "kubectl apply -f deployment.yml -n ${K8S_NAMESPACE}"
                    sh "kubectl set image deployment/${REPO_NAME} ${REPO_NAME}=${DOCKER_IMAGE} -n ${K8S_NAMESPACE}"
                }
            }
        }
        stage('Verify Deployment') {
            steps {
                withKubeConfig([credentialsId: K8S_CREDENTIALS_ID, serverUrl: K8S_SERVER_URL]) {
                    sh "kubectl rollout status deployment/${REPO_NAME} -n ${K8S_NAMESPACE}"
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
