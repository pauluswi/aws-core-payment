# AWS EKS deployment notes

This folder contains a minimal Kubernetes manifest set for deploying the Spring Boot application to Amazon EKS.

## Prerequisites

- EKS cluster running
- AWS CLI configured
- kubectl configured to the target cluster
- ECR repository created for the application image

## Build and push

```bash
export AWS_ACCOUNT_ID=123456789012
export AWS_REGION=us-west-2
export ECR_REPO=aws-core-payment

docker build -t ${ECR_REPO}:latest .
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

docker tag ${ECR_REPO}:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:latest
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:latest
```

## Apply manifests

```bash
kubectl create namespace payment
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/ingress.yaml
```

## Adjustments for production

- Replace placeholder image repository with your real ECR URL.
- Use AWS Secrets Manager or External Secrets Operator instead of plain Kubernetes secrets for production.
- Wire real MSK, Aurora PostgreSQL, ALB, and IAM roles instead of mock configs.
- Use a managed ingress or AWS Load Balancer Controller for production traffic.

## Health check

```bash
kubectl -n payment get pods
kubectl -n payment port-forward svc/aws-core-payment 8080:80
curl http://localhost:8080/actuator/health
```
