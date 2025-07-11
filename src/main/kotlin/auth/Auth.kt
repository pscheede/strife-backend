package com.scheede.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class BlockedPage(val host: String, val path: String)

val blockedPages = listOf(BlockedPage("beta.p-scheede.de", "/blog/photo-shoot-luka-alien-stage"))
val keyRepository = KeyRepository()

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

            suspend fun isAuthorized(): Boolean {
                if ("admin" in url.parameters) return true;

                return call.request.headers["MyAuthorization"]?.let {
                    val key = keyRepository.getMatchingKey(it)
                    key != null
                } ?: false
            }

            if (
                blockedPages.any { it.host == url.host && url.fullPath.startsWith(it.path) }
                && !isAuthorized()
            ) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            call.respond(HttpStatusCode.NoContent)
        }

        get("/auth/key") {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            val newKey = (1..24)
                .map { allowedChars.random() }
                .joinToString("")

            call.respond(newKey)
        }

        get("/login") {
            val targetUser = call.request.queryParameters["user"]

            if (targetUser == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val keyForUser = keyRepository.getKeyForUser(targetUser)

            if (keyForUser == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                call.response.cookies.append(Cookie("my-login-cookie", keyForUser.key))
                call.respond("cookie set")
            }
        }
    }
}
