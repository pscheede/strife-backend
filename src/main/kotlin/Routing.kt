package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") { call.respondText("Hello incredibly beautiful World! Let's gooo!") }

        get("/auth") {
            val specifiedHeaders =
                    listOf("X-Original-URI", "X-Original-Remote-Addr", "X-Original-Host")

            println("auth request")
            specifiedHeaders.forEach { headerName ->
                val headerValue = call.request.headers[headerName] ?: "Not Provided"
                println("$headerName: $headerValue")
            }

            if (call.request.headers["X-Original-URI"]?.contains("photo-shoot-luka-alien-stage") == true
            ) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
