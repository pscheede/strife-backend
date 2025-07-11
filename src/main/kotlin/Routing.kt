package com.scheede

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello incredibly beautiful World! Let's gooo!")
        }
    }
}
