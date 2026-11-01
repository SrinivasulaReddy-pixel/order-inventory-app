# AWS deployment mapping (reference, not yet provisioned)

| Local (docker-compose) | AWS equivalent |
|---|---|
| SQL Server container | Amazon RDS for SQL Server (authdb, orderdb) |
| MongoDB container | Amazon DocumentDB (Mongo-compatible) or MongoDB Atlas |
| Kafka + Zookeeper | Amazon MSK, or self-hosted Kafka on EC2 for a cheaper demo |
| Local Docker build | Amazon ECR (image registry) |
| `docker-compose up` | Amazon ECS on Fargate (one service per task definition) |
| Eureka container | Keep as an ECS task; Cloud Map/ECS Service Discovery is the AWS-native alternative, worth mentioning as a tradeoff |
| `npm run dev` build | S3 + CloudFront (static hosting) for the production React build |
| `.env` / compose env vars | AWS Secrets Manager or SSM Parameter Store for DB creds + jwt.secret |
| Jenkins (local) | Jenkins on EC2 (or container) — builds, pushes to ECR, triggers `aws ecs update-service` |

See `order-service-task-def.json` for a sample ECS Fargate task definition — the pattern is identical for the other services (swap image, port, env vars).
