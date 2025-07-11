package com.scheede.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class BlockedPage(val host: String, val path: String)

val blockedPages = listOf(
    BlockedPage("beta.p-scheede.de", "/blog/photo-shoot-luka-alien-stage"),
    BlockedPage("beta.p-scheede.de", "/blog/stance-on-generative-ai")
)

fun Application.authRouting() {
    routing {
        get("/auth") {
            val badRequest: suspend (String) -> Unit = { it: String ->
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing header: $it"
                )
            }

            val host = call.request.headers["X-Original-Host"] ?: run {
                badRequest("X-Original-Host")
                return@get
            }
            val uri = call.request.headers["X-Original-URI"] ?: run {
                badRequest("X-Original-URI")
                return@get
            }
            /* val callerIp = call.request.headers["X-Original-Remote-Addr"] ?: run {
                badRequest("X-Original-Remote-Addr")
                return@get
            } */

            // required for easily extracting/separating query from path
            val url = Url("https://$host$uri")

            if (
                blockedPages.any { it.host == url.host && url.fullPath.startsWith(it.path) }
                && "admin" !in url.parameters
            ) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
