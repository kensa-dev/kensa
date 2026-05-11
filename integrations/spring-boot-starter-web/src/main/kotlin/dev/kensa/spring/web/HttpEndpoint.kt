package dev.kensa.spring.web

import dev.kensa.state.Party

data class HttpEndpoint(val name: String) : Party {
    override fun asString(): String = name

    companion object {
        val Client: HttpEndpoint = HttpEndpoint("Client")
        val Server: HttpEndpoint = HttpEndpoint("Server")
    }
}

data class HttpCapturedRequest(
    val method: String,
    val uri: String,
    val headers: Map<String, List<String>>,
    val body: String?,
)

data class HttpCapturedResponse(
    val status: Int,
    val headers: Map<String, List<String>>,
    val body: String?,
)
