#!/bin/bash
set -e

EC2_DNS="ec2-18-225-37-127.us-east-2.compute.amazonaws.com"
SSH_KEY="ec2-login.pem"
REMOTE_DIR="/home/ec2-user/app"

echo "Criando diretório na EC2..."
ssh -i ~/.ssh/$SSH_KEY ec2-user@$EC2_DNS "mkdir -p $REMOTE_DIR"

echo "Copiando backend..."
rsync -avz --exclude 'node_modules' --exclude '.git' --exclude 'target' \
  -e "ssh -i ~/.ssh/$SSH_KEY" \
  ../alexandria-backend/ \
  ec2-user@$EC2_DNS:$REMOTE_DIR/backend/

echo "Subindo container..."
ssh -i ~/.ssh/$SSH_KEY ec2-user@$EC2_DNS << 'SSH'
  cd /home/ec2-user/app/backend
  docker build -t alexandria-backend .
  docker rm -f alexandria-backend 2>/dev/null || true
  docker run -d \
    --name alexandria-backend \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=rds \
    -e DB_HOST=alexandria-db.c9qs2asg26uk.us-east-2.rds.amazonaws.com \
    -e DB_PORT=3306 \
    -e DB_NAME=alexandriadb \
    -e DB_USER=admin \
    -e DB_PASSWORD=wBBLmV5dZkaYqB5 \
    -e JWT_SECRET=sua-chave-super-segura-de-256-bits \
    alexandria-backend
SSH

echo "Backend rodando em: http://$EC2_DNS:8080"
echo "Health: http://$EC2_DNS:8080/actuator/health"
