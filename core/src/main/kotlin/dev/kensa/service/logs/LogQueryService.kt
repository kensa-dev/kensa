package dev.kensa.service.logs

interface LogQueryService {
  fun query(sourceId: String, identifier: String): List<LogRecord>
  fun queryAll(sourceId: String): List<LogRecord>
}

