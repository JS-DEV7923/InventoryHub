resource "aws_lambda_function" "notification_worker" {
  count         = var.enable_lambda ? 1 : 0
  function_name = "${var.name_prefix}-notification-worker"
  role          = var.lambda_role_arn
  handler       = "com.inventoryhub.notifications.NotificationLambdaHandler::handleRequest"
  runtime       = "java21"
  filename      = var.package_path
  timeout       = 30
  memory_size   = 512
  tags          = var.tags

  lifecycle {
    ignore_changes = [filename, source_code_hash]
  }
}

resource "aws_lambda_event_source_mapping" "notification_queue" {
  count            = var.enable_lambda ? 1 : 0
  event_source_arn = var.notification_queue_arn
  function_name    = aws_lambda_function.notification_worker[0].arn
  batch_size       = 10
  enabled          = true
}
