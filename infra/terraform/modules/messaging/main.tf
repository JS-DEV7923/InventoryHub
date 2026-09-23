resource "aws_sns_topic" "lifecycle" {
  name = "${var.name_prefix}-lifecycle-events"
  tags = var.tags
}

resource "aws_sqs_queue" "inventory_dlq" {
  name = "${var.name_prefix}-inventory-reservation-dlq"
  tags = var.tags
}

resource "aws_sqs_queue" "order_outcome_dlq" {
  name = "${var.name_prefix}-order-outcome-dlq"
  tags = var.tags
}

resource "aws_sqs_queue" "notification_dlq" {
  name = "${var.name_prefix}-notification-dlq"
  tags = var.tags
}

resource "aws_sqs_queue" "inventory" {
  name                       = "${var.name_prefix}-inventory-reservation-queue"
  visibility_timeout_seconds = 60
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.inventory_dlq.arn
    maxReceiveCount     = 5
  })
  tags = var.tags
}

resource "aws_sqs_queue" "order_outcome" {
  name                       = "${var.name_prefix}-order-outcome-queue"
  visibility_timeout_seconds = 60
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.order_outcome_dlq.arn
    maxReceiveCount     = 5
  })
  tags = var.tags
}

resource "aws_sqs_queue" "notification" {
  name                       = "${var.name_prefix}-notification-queue"
  visibility_timeout_seconds = 60
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notification_dlq.arn
    maxReceiveCount     = 5
  })
  tags = var.tags
}

resource "aws_sns_topic_subscription" "inventory" {
  topic_arn = aws_sns_topic.lifecycle.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.inventory.arn
}

resource "aws_sns_topic_subscription" "order_outcome" {
  topic_arn = aws_sns_topic.lifecycle.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.order_outcome.arn
}

resource "aws_sns_topic_subscription" "notification" {
  topic_arn = aws_sns_topic.lifecycle.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.notification.arn
}

data "aws_iam_policy_document" "inventory_queue_policy" {
  statement {
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.inventory.arn]

    principals {
      type        = "Service"
      identifiers = ["sns.amazonaws.com"]
    }

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_sns_topic.lifecycle.arn]
    }
  }
}

data "aws_iam_policy_document" "order_outcome_queue_policy" {
  statement {
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.order_outcome.arn]

    principals {
      type        = "Service"
      identifiers = ["sns.amazonaws.com"]
    }

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_sns_topic.lifecycle.arn]
    }
  }
}

data "aws_iam_policy_document" "notification_queue_policy" {
  statement {
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.notification.arn]

    principals {
      type        = "Service"
      identifiers = ["sns.amazonaws.com"]
    }

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_sns_topic.lifecycle.arn]
    }
  }
}

resource "aws_sqs_queue_policy" "inventory" {
  queue_url = aws_sqs_queue.inventory.url
  policy    = data.aws_iam_policy_document.inventory_queue_policy.json
}

resource "aws_sqs_queue_policy" "order_outcome" {
  queue_url = aws_sqs_queue.order_outcome.url
  policy    = data.aws_iam_policy_document.order_outcome_queue_policy.json
}

resource "aws_sqs_queue_policy" "notification" {
  queue_url = aws_sqs_queue.notification.url
  policy    = data.aws_iam_policy_document.notification_queue_policy.json
}
