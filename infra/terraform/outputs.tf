output "lifecycle_topic_arn" {
  value = module.messaging.lifecycle_topic_arn
}

output "inventory_queue_url" {
  value = module.messaging.inventory_queue_url
}

output "order_outcome_queue_url" {
  value = module.messaging.order_outcome_queue_url
}

output "notification_queue_url" {
  value = module.messaging.notification_queue_url
}

output "database_endpoint" {
  value = module.rds.database_endpoint
}
