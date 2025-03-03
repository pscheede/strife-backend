package com.scheede

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") { call.respondText("Hello incredibly beautiful World! Let's gooo!") }
    }
}
