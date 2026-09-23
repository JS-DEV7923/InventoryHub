resource "aws_db_subnet_group" "this" {
  count      = length(var.subnet_ids) > 0 ? 1 : 0
  name       = "${var.name_prefix}-db-subnet-group"
  subnet_ids = var.subnet_ids
  tags       = var.tags
}

resource "aws_security_group" "rds" {
  count       = var.vpc_id == null ? 0 : 1
  name        = "${var.name_prefix}-rds-sg"
  description = "InventoryHub RDS security group"
  vpc_id      = var.vpc_id
  tags        = var.tags
}

resource "aws_db_instance" "mysql" {
  identifier             = "${var.name_prefix}-mysql"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t4g.micro"
  allocated_storage      = 20
  db_name                = "inventoryhub"
  username               = var.db_username
  password               = var.db_password
  skip_final_snapshot    = true
  publicly_accessible    = false
  db_subnet_group_name   = length(var.subnet_ids) > 0 ? aws_db_subnet_group.this[0].name : null
  vpc_security_group_ids = var.vpc_id == null ? [] : [aws_security_group.rds[0].id]
  tags                   = var.tags
}
