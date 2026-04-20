package dev.kensa.util

import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

internal class SubclassFirstComparator : Comparator<KClass<*>?> {
    override fun compare(c1: KClass<*>?, c2: KClass<*>?): Int {
        if (c1 == null) {
            return if (c2 == null) 0 else 1
        } else if (c2 == null) {
            return -1
        }
        if (c1 == c2) {
            return 0
        }
        val c1Sub = c2.isSuperclassOf(c1)
        val c2Sub = c1.isSuperclassOf(c2)
        if (c1Sub && !c2Sub) {
            return -1
        } else if (c2Sub && !c1Sub) {
            return 1
        }
        return c1.qualifiedName!!.compareTo(c2.qualifiedName!!)
    }
}
