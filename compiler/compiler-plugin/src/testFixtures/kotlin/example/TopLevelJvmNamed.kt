@file:JvmName("CustomTopLevelFacade")

package example

import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue

@RenderedValue
fun topLevelJvmNamedRenderedValue(service: Service, value: String): String {
    service.call(arrayOf(value))
    return "jvmnamed-$value"
}

@ExpandableSentence
fun topLevelJvmNamedExpandableSentence(service: Service, value: String) {
    service.call(arrayOf(service, value))
}
