# Data sources para parâmetros SSM
data "aws_ssm_parameter" "vpc_id" {
  name = "/linuxtips/vpc/id"
}

data "aws_ssm_parameter" "public_subnet_1a" {
  name = "/linuxtips/vpc/public-subnet-1a"
}

data "aws_ssm_parameter" "public_subnet_1b" {
  name = "/linuxtips/vpc/public-subnet-1b"
}

data "aws_ssm_parameter" "public_subnet_1c" {
  name = "/linuxtips/vpc/public-subnet-1c"
}

data "aws_ssm_parameter" "private_subnet_1a" {
  name = "/linuxtips/vpc/private-subnet-1a"
}

data "aws_ssm_parameter" "private_subnet_1b" {
  name = "/linuxtips/vpc/private-subnet-1b"
}

data "aws_ssm_parameter" "private_subnet_1c" {
  name = "/linuxtips/vpc/private-subnet-1c"
}
