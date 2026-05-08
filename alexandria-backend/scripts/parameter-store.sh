aws ssm put-parameter --name "/alexandria/db-host" --value "alexandria-db.c9qs2asg26uk.us-east-2.rds.amazonaws.com" --type "String" --overwrite

aws ssm put-parameter --name "/alexandria/db-port" --value "3306" --type "String" --overwrite

aws ssm put-parameter --name "/alexandria/db-name" --value "alexandriadb" --type "String" --overwrite

aws ssm put-parameter --name "/alexandria/db-user" --value "admin" --type "String" --overwrite

aws ssm put-parameter --name "/alexandria/db-password" --value "wBBLmV5dZkaYqB5" --type "SecureString" --overwrite

aws ssm put-parameter --name "/alexandria/jwt-secret" --value "sua-chave-super-segura-de-256-bits" --type "SecureString" --overwrite
