aws ssm get-parameter --name "/alexandria/db-host" --query "Parameter.Value" --output text

aws ssm get-parameter --name "/alexandria/db-port" --query "Parameter.Value" --output text

aws ssm get-parameter --name "/alexandria/db-name" --query "Parameter.Value" --output text

aws ssm get-parameter --name "/alexandria/db-user" --query "Parameter.Value" --output text

aws ssm get-parameter --name "/alexandria/db-password" --with-decryption --query "Parameter.Value" --output text

aws ssm get-parameter --name "/alexandria/jwt-secret" --with-decryption --query "Parameter.Value" --output text
