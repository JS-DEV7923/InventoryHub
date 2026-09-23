data "aws_iam_policy_document" "application_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "application_service" {
  name               = "${var.name_prefix}-application-service-role"
  assume_role_policy = data.aws_iam_policy_document.application_assume_role.json
  tags               = var.tags
}

resource "aws_iam_role" "notification_lambda" {
  name               = "${var.name_prefix}-notification-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = var.tags
}

data "aws_iam_policy_document" "application_policy" {
  statement {
    actions   = ["sns:Publish"]
    resources = [var.lifecycle_topic_arn]
  }

  statement {
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes"
    ]
    resources = [
      var.inventory_queue_arn,
      var.order_outcome_queue_arn
    ]
  }
}

data "aws_iam_policy_document" "lambda_policy" {
  statement {
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes"
    ]
    resources = [var.notification_queue_arn]
  }

  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "application_policy" {
  name   = "${var.name_prefix}-application-policy"
  policy = data.aws_iam_policy_document.application_policy.json
  tags   = var.tags
}

resource "aws_iam_policy" "lambda_policy" {
  name   = "${var.name_prefix}-notification-lambda-policy"
  policy = data.aws_iam_policy_document.lambda_policy.json
  tags   = var.tags
}

resource "aws_iam_role_policy_attachment" "application_policy" {
  role       = aws_iam_role.application_service.name
  policy_arn = aws_iam_policy.application_policy.arn
}

resource "aws_iam_role_policy_attachment" "lambda_policy" {
  role       = aws_iam_role.notification_lambda.name
  policy_arn = aws_iam_policy.lambda_policy.arn
}
