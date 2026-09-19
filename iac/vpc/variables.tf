## GENERAL CONFIGS ###

variable "project_name" {
  description = "Nome do projeto para prefixar os recursos"
  type        = string
  default     = "mba-puc-alexandria"
}


variable "aws_region" {
  description = "Região AWS onde os recursos serão criados"
  type        = string
  default     = "us-east-1"
}


variable "common_tags" {
  description = "Tags comuns para todos os recursos"
  type        = map(string)
  default = {
    Project     = "mba-puc-alexandria"
    Environment = "production"
    ManagedBy   = "terraform"
    Owner       = "tamiris"
  }
}
