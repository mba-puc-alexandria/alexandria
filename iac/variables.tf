variable "aws_region" {
  description = "Região AWS onde os recursos serão criados"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Nome do projeto para prefixar os recursos"
  type        = string
  default     = "linuxtips-sorteador"
}

variable "container_port" {
  description = "Porta do container da aplicação"
  type        = number
  default     = 5000
}

variable "task_cpu" {
  description = "CPU units para a task ECS (1024 = 1 vCPU)"
  type        = number
  default     = 256
}

variable "task_memory" {
  description = "Memória para a task ECS em MB"
  type        = number
  default     = 512
}

variable "service_desired_count" {
  description = "Número desejado de instâncias do serviço ECS"
  type        = number
  default     = 1
}

variable "service_min_count" {
  description = "Número mínimo de instâncias do serviço ECS"
  type        = number
  default     = 1
}

variable "service_max_count" {
  description = "Número máximo de instâncias do serviço ECS"
  type        = number
  default     = 2
}

variable "certificate_arn" {
  description = "ARN do certificado SSL para o ALB"
  type        = string
  default     = "arn:aws:acm:us-east-1:760337697893:certificate/d7fc9463-792e-4f15-9d0d-0dfab0789c02"
}

variable "common_tags" {
  description = "Tags comuns para todos os recursos"
  type        = map(string)
  default = {
    Project     = "linuxtips-sorteador"
    Environment = "production"
    ManagedBy   = "terraform"
    Owner       = "fabio"
  }
}

locals {
  vpc_id = data.aws_ssm_parameter.vpc_id.value

  public_subnet_ids = [
    data.aws_ssm_parameter.public_subnet_1a.value,
    data.aws_ssm_parameter.public_subnet_1b.value,
    data.aws_ssm_parameter.public_subnet_1c.value
  ]

  private_subnet_ids = [
    data.aws_ssm_parameter.private_subnet_1a.value,
    data.aws_ssm_parameter.private_subnet_1b.value,
    data.aws_ssm_parameter.private_subnet_1c.value
  ]
}
