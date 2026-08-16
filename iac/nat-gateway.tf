resource "aws_eip" "vpc_eip" {
  domain = "vpc"
  tags = {
    Name = format("%s-eip", var.project_name)
  }
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.vpc_eip.id
  subnet_id     = aws_subnet.public_subnet.id
  tags = {
    Name : format("%s-nat", var.project_name)
  }
}


resource "aws_route_table" "private_internet_access" {
  vpc_id = aws_vpc.main.id
  tags = {
    Name = format("%s-private", var.project_name)
  }
}


resource "aws_route" "private_access" {
  route_table_id         = aws_route_table.private_internet_access.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_nat_gateway.nat.id
}

#associa a subnet  a uma tabela de rotas
resource "aws_route_table_association" "private" {
  subnet_id      = aws_subnet.private_subnet.id
  route_table_id = aws_route_table.private_internet_access.id
}
