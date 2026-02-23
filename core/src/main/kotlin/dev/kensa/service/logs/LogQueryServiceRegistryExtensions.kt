package dev.kensa.service.logs

import java.nio.file.Path

fun LogQueryServiceRegistry.rawFile(id: String, path: Path, tailLines: Int = 200) {
    register(id) { sourceId ->
        RawLogFileQueryService(
            source = FileSource(sourceId, path),
            tailLines = tailLines
        )
    }
}

fun LogQueryServiceRegistry.indexedFile(id: String, path: Path, idPattern: Regex, delimiterLine: String) {
    register(id) { sourceId ->
        IndexedLogFileQueryService(
            source = FileSource(sourceId, path),
            idPattern,
            delimiterLine
        )
    }
}
