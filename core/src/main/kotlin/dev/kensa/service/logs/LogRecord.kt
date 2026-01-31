package dev.kensa.service.logs

data class LogRecord(
  val sourceId: String,
  val identifier: String,
  val text: String
)