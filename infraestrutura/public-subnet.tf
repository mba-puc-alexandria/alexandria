resource "aws_subnet" "public_subnet_1a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.48.0/24"
  availability_zone = format("%sa", var.region)

  tags = {
    Name = format("%s-prublic_subnet_1a", var.project_name)
  }

}
