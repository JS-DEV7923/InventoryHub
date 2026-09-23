variable "name_prefix" {
  type = string
}

variable "lifecycle_topic_arn" {
  type = string
}

variable "inventory_queue_url" {
  type = string
}

variable "order_outcome_queue_url" {
  type = string
}

variable "database_endpoint" {
  type = string
}

variable "service_role_arn" {
  type = string
}

variable "tags" {
  type    = map(string)
  default = {}
}
