package dev.kensa

import kotlin.time.Duration

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

    @JvmStatic
    fun with(duration: Duration) = duration

    fun within(duration: Duration) = duration

    fun over(duration: Duration) = duration

    @JvmStatic
    fun with(duration: java.time.Duration) = duration

    @JvmStatic
    fun <T> has(vararg items: T): Array<out T> = items

    @JvmStatic
    fun <T> have(vararg items: T): Array<out T> = items

    @JvmStatic
    fun <T> having(vararg items: T): Array<out T> = items

    @JvmStatic
    fun <T> including(vararg items: T): Array<out T> = items

    @JvmStatic
    fun <T> containing(vararg items: T): Array<out T> = items

    @JvmStatic
    fun <T> with(vararg items: T): Array<out T> = items
}
