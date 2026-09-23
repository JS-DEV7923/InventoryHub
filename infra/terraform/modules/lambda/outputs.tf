output "notification_lambda_name" {
  value = var.enable_lambda ? aws_lambda_function.notification_worker[0].function_name : null
}
