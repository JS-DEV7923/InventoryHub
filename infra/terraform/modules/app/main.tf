resource "aws_ssm_parameter" "lifecycle_topic_arn" {
  name  = "/${var.name_prefix}/messaging/lifecycle-topic-arn"
  type  = "String"
  value = var.lifecycle_topic_arn
  tags  = var.tags
}

resource "aws_ssm_parameter" "inventory_queue_url" {
  name  = "/${var.name_prefix}/messaging/inventory-queue-url"
  type  = "String"
  value = var.inventory_queue_url
  tags  = var.tags
}

resource "aws_ssm_parameter" "order_outcome_queue_url" {
  name  = "/${var.name_prefix}/messaging/order-outcome-queue-url"
  type  = "String"
  value = var.order_outcome_queue_url
  tags  = var.tags
}

resource "aws_ssm_parameter" "database_endpoint" {
  name  = "/${var.name_prefix}/database/endpoint"
  type  = "String"
  value = var.database_endpoint
  tags  = var.tags
}

resource "aws_ssm_parameter" "service_role_arn" {
  name  = "/${var.name_prefix}/app/service-role-arn"
  type  = "String"
  value = var.service_role_arn
  tags  = var.tags
}
