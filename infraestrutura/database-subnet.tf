resource "aws_subnet" "database_subnet" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.50.0/24"
  availability_zone = format("%sa", var.region)

  tags = {
    Name = format("%s-database_subnet", var.project_name)
  }

}
