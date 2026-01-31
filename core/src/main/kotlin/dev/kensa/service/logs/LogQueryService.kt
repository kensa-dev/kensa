package dev.kensa.service.logs

interface LogQueryService {
  fun query(identifier: String): List<LogRecord>
}

