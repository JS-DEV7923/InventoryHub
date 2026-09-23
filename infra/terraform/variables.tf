variable "aws_region" {
  description = "AWS region for InventoryHub resources."
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name used for resource naming."
  type        = string
  default     = "inventoryhub"
}

variable "db_username" {
  description = "RDS MySQL username."
  type        = string
  default     = "inventoryhub"
}

variable "db_password" {
  description = "RDS MySQL password. Supply through a secure variable source."
  type        = string
  sensitive   = true
}

variable "vpc_id" {
  description = "VPC ID for RDS security group."
  type        = string
  default     = null
}

variable "subnet_ids" {
  description = "Private subnet IDs for RDS subnet group."
  type        = list(string)
  default     = []
}

variable "lambda_package_path" {
  description = "Path to notification Lambda deployment package."
  type        = string
  default     = "build/notification-worker.zip"
}

variable "enable_notification_lambda" {
  description = "Whether to create the notification Lambda worker. Enable after a deployable package exists."
  type        = bool
  default     = false
}
