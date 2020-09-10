package dev.kensa

object RefinedSugar {
    @JvmStatic
    fun <T> has(t: T): T = t

    @JvmStatic
    fun <T> have(t: T): T = t

    @JvmStatic
    fun <T> a(t: T): T = t

    @JvmStatic
    fun <T> an(t: T): T = t

    @JvmStatic
    fun <T> andA(t: T): T = t

    @JvmStatic
    fun <T> andAn(t: T): T = t

    @JvmStatic
    fun <T> andThe(t: T): T = t

    @JvmStatic
    fun <T> at(t: T): T = t

    @JvmStatic
    fun <T> does(t: T): T = t

    @JvmStatic
    fun <T> `in`(t: T): T = t

    @JvmStatic
    fun <T> `is`(t: T): T = t

    @JvmStatic
    fun <T> of(t: T): T = t

    @JvmStatic
    fun <T> that(t: T): T = t

    @JvmStatic
    fun <T> the(t: T): T = t

    @JvmStatic
    fun <T> to(t: T): T = t

    @JvmStatic
    fun <T> with(t: T): T = t
}