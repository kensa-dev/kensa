package dev.kensa.util

import kotlin.time.Duration

fun Duration.format(): String {

    fun StringBuilder.append(value: Int, singular: String, pluralSuffix: String = "s") {
        if (value > 0) {
            if(isNotEmpty()) {
                append(" ")
            }
            append(value).append(" ").append(singular).append(if (value == 1) "" else pluralSuffix)
        }
    }

    return toComponents { days, hours, minutes, seconds, nanoseconds ->
        buildString {
            append(days.toInt(), "Day")
            append(hours, "Hr")
            append(minutes, "Min")
            append(seconds, "Sec")
            append(nanoseconds / 1_000_000, "Ms", "")
        }
    }
}
