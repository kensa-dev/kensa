package example

import dev.kensa.RenderedValue

@RenderedValue
fun topLevelRenderedValue(service: Service, value: String): String {
    service.call(arrayOf(value))
    return "top-$value"
}

@RenderedValue
fun Int.topLevelOnExtensionReceiver(service: Service): Int = (this * 2).also { service.call(arrayOf(it)) }
