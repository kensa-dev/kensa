package example

import dev.kensa.ExpandableSentence

@ExpandableSentence
fun topLevelExpandableSentence(service: Service, value: String) {
    service.call(arrayOf(service, value))
}

@ExpandableSentence
fun Int.topLevelExpandableOnExtensionReceiver(service: Service) {
    service.call(arrayOf(this, service))
}
