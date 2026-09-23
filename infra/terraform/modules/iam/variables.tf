variable "name_prefix" {
  type = string
}

variable "lifecycle_topic_arn" {
  type = string
}

variable "inventory_queue_arn" {
  type = string
}

variable "order_outcome_queue_arn" {
  type = string
}

variable "notification_queue_arn" {
  type = string
}

variable "tags" {
  type    = map(string)
  default = {}
}
