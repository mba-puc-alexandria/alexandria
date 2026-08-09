resource "aws_subnet" "private_subnet" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.16.0/20"
  availability_zone = format("%sa", var.region)

  tags = {
    Name = format("%s-private_subnet", var.project_name)
  }

}
