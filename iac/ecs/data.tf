# Data sources para parâmetros SSM
data "aws_ssm_parameter" "vpc_id" {
  name = "/mba-puc-alexandria/vpc/id"
}

data "aws_ssm_parameter" "public_subnet_1a" {
  name = "/mba-puc-alexandria/vpc/public-subnet-1a"
}

data "aws_ssm_parameter" "public_subnet_1b" {
  name = "/mba-puc-alexandria/vpc/public-subnet-1b"
}

data "aws_ssm_parameter" "public_subnet_1c" {
  name = "/mba-puc-alexandria/vpc/public-subnet-1c"
}

data "aws_ssm_parameter" "private_subnet_1a" {
  name = "/mba-puc-alexandria/vpc/private-subnet-1a"
}

data "aws_ssm_parameter" "private_subnet_1b" {
  name = "/mba-puc-alexandria/vpc/private-subnet-1b"
}

data "aws_ssm_parameter" "private_subnet_1c" {
  name = "/mba-puc-alexandria/vpc/private-subnet-1c"
}
