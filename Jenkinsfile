pipeline {
    agent any
    environment {
        // Docker Hub 凭证 ID (Jenkins 里配置的)
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials')

        // Docker Hub 仓库名 (你的用户名/仓库名)
        DOCKER_IMAGE = 'xingyh2003/teedy'

        // 镜像 tag (使用 Jenkins build number)
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout & Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/docker']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/yuhengxing-star/Teedy.git']]
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'DOCKER_HUB_CREDENTIALS') {
                        // 推镜像 (带 tag)
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()

                        // 可选: 也打 latest 标签一起推
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Run Containers') {
            steps {
                script {
                    // 停止并删除旧容器 (3个端口)
                    for (port in [8082, 8083, 8084]) {
                        sh "docker stop teedy-container-${port} || true"
                        sh "docker rm teedy-container-${port} || true"
                    }

                    // 启动3个容器
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run('--name teedy-container-8082 -d -p 8082:8080')
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run('--name teedy-container-8083 -d -p 8083:8080')
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run('--name teedy-container-8084 -d -p 8084:8080')

                    // 可选: 列出容器
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}
