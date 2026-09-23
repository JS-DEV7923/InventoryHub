output "lifecycle_topic_arn" {
  value = aws_sns_topic.lifecycle.arn
}

output "inventory_queue_arn" {
  value = aws_sqs_queue.inventory.arn
}

output "inventory_queue_url" {
  value = aws_sqs_queue.inventory.url
}

output "order_outcome_queue_arn" {
  value = aws_sqs_queue.order_outcome.arn
}

output "order_outcome_queue_url" {
  value = aws_sqs_queue.order_outcome.url
}

output "notification_queue_arn" {
  value = aws_sqs_queue.notification.arn
}

output "notification_queue_url" {
  value = aws_sqs_queue.notification.url
}
