output "application_service_role_arn" {
  value = aws_iam_role.application_service.arn
}

output "notification_lambda_role_arn" {
  value = aws_iam_role.notification_lambda.arn
}
