resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"

  #gestao nativa de dns
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = var.project_name
  }
}