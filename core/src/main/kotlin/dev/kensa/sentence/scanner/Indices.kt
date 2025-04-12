package dev.kensa.sentence.scanner

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TokenType
import java.util.*

class Indices : Iterable<Index> {

    private val indices: SortedSet<Index> = TreeSet { i1: Index, i2: Index ->
        if (i1.start == i2.start) {
            i2.end.compareTo(i1.end)
        } else {
            i1.start.compareTo(i2.start)
        }
    }

    fun put(type: TokenType, start: Int, end: Int, emphasisDescriptor: EmphasisDescriptor? = null) {
        indices.add(Index(type, start, end, emphasisDescriptor))
        var lastIndex: Index? = null
        val iterator = indices.iterator()
        while (iterator.hasNext()) {
            val thisIndex = iterator.next()
            if (lastIndex != null) {
                if (lastIndex.cancels(thisIndex)) {
                    iterator.remove()
                } else {
                    lastIndex = thisIndex
                }
            } else {
                lastIndex = thisIndex
            }
        }
    }

    fun putWords(words: Set<Index>) {
        indices.addAll(words)
    }

    override fun iterator(): MutableIterator<Index> = indices.iterator()

    override fun toString(): String = "Indices{indices=$indices}"
}