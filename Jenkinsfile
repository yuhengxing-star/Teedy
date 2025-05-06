pipeline {
    agent any
    environment {
        // Docker 镜像名（只需要本地仓库）
        DOCKER_IMAGE = 'teedy-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }
    stages {
        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/docker']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/yuhengxing-star/Teedy.git']]
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Build image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    // 停掉并删掉已有容器（3个）
                    sh 'docker stop teedy-container-8082 || true'
                    sh 'docker rm teedy-container-8082 || true'
                    sh 'docker stop teedy-container-8083 || true'
                    sh 'docker rm teedy-container-8083 || true'
                    sh 'docker stop teedy-container-8084 || true'
                    sh 'docker rm teedy-container-8084 || true'

                    // 启动3个容器
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                        '--name teedy-container-8082 -d -p 8082:8080'
                    )
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                        '--name teedy-container-8083 -d -p 8083:8080'
                    )
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                        '--name teedy-container-8084 -d -p 8084:8080'
                    )

                    // 可选：列出所有容器
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}
