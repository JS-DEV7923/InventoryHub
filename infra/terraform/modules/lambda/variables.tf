variable "name_prefix" {
  type = string
}

variable "enable_lambda" {
  type    = bool
  default = false
}

variable "package_path" {
  type = string
}

variable "notification_queue_arn" {
  type = string
}

variable "lambda_role_arn" {
  type = string
}

variable "tags" {
  type    = map(string)
  default = {}
}
