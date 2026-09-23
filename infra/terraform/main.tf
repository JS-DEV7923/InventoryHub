locals {
  name_prefix = "${var.project_name}-${var.environment}"
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

module "messaging" {
  source      = "./modules/messaging"
  name_prefix = local.name_prefix
  tags        = local.common_tags
}

module "iam" {
  source                 = "./modules/iam"
  name_prefix            = local.name_prefix
  lifecycle_topic_arn    = module.messaging.lifecycle_topic_arn
  inventory_queue_arn    = module.messaging.inventory_queue_arn
  order_outcome_queue_arn = module.messaging.order_outcome_queue_arn
  notification_queue_arn = module.messaging.notification_queue_arn
  tags                   = local.common_tags
}

module "rds" {
  source      = "./modules/rds"
  name_prefix = local.name_prefix
  db_username = var.db_username
  db_password = var.db_password
  vpc_id      = var.vpc_id
  subnet_ids  = var.subnet_ids
  tags        = local.common_tags
}

module "lambda" {
  source                 = "./modules/lambda"
  name_prefix            = local.name_prefix
  enable_lambda          = var.enable_notification_lambda
  package_path           = var.lambda_package_path
  notification_queue_arn = module.messaging.notification_queue_arn
  lambda_role_arn        = module.iam.notification_lambda_role_arn
  tags                   = local.common_tags
}

module "app" {
  source                  = "./modules/app"
  name_prefix             = local.name_prefix
  lifecycle_topic_arn     = module.messaging.lifecycle_topic_arn
  inventory_queue_url     = module.messaging.inventory_queue_url
  order_outcome_queue_url = module.messaging.order_outcome_queue_url
  database_endpoint       = module.rds.database_endpoint
  service_role_arn        = module.iam.application_service_role_arn
  tags                    = local.common_tags
}
