@file:JvmName("CustomTopLevelFacade")

package example

import dev.kensa.RenderedValue

@RenderedValue
fun topLevelJvmNamedRenderedValue(service: Service, value: String): String {
    service.call(arrayOf(value))
    return "jvmnamed-$value"
}
