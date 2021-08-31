package dev.kensa.output

import dev.kensa.context.TestContainer

interface ResultWriter {
    fun write(containers: Set<TestContainer>)
}