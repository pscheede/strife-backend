package com.scheede

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.forms.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import kotlinx.html.*
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

@Serializable
data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token_expires_in") val refreshTokenExpiresIn: Int? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val scope: String,
    @SerialName("token_type") val tokenType: String,
)

@Serializable
data class AddressPayload(val address: String)

@Serializable
data class RoutesRequest(
    val origin: AddressPayload,
    val destination: AddressPayload
)

val dotenv = dotenv() {
    filename = ".env.local"
}

var token: String? = null;

val scopes = arrayOf("https://www.googleapis.com/auth/calendar.readonly", "https://www.googleapis.com/auth/cloud-platform")

val bearerTokenStorage = mutableListOf<BearerTokens>()
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }

    install(Auth) {
        bearer {
            loadTokens {
                bearerTokenStorage.last()
            }
            sendWithoutRequest { request ->
                request.url.host == "www.googleapis.com" || request.url.host == "routes.googleapis.com"
            }
            refreshTokens {
                val refreshTokenInfo: TokenInfo = client.submitForm(
                    url = "https://accounts.google.com/o/oauth2/token",
                    formParameters = parameters {
                        append("grant_type", "refresh_token")
                        append("client_id", dotenv["GOOGLE_CLOUD_CLIENT_ID"])
                        append("refresh_token", oldTokens?.refreshToken ?: "")
                    }
                ) { markAsRefreshTokenRequest() }.body()
                bearerTokenStorage.add(BearerTokens(refreshTokenInfo.accessToken, oldTokens?.refreshToken!!))
                bearerTokenStorage.last()
            }
        }
    }
}

fun Application.configureRouting() {
    routing {
        get("/") { call.respondHtml(HttpStatusCode.OK) {
            body {
                style = "display: flex; flex-direction: column"
                h1 { +"heloo this is fantastic!" }
                a {
                    href = "/login"
                    +"Login"
                }

                a {
                    href = "/calendars"
                    +"Calendars"
                }

                a {
                    href = "/events"
                    +"Events"
                }
            }
        } }

        get("/login") {
            if (token == null) {
                val authorizationUrlQuery = parameters {
                    append("client_id", dotenv["GOOGLE_CLOUD_CLIENT_ID"])
                    append("scope", scopes.joinToString(" "))
                    append("response_type", "code")
                    append("redirect_uri", "http://localhost:8080/callback")
                    append("access_type", "offline")
                    append("state", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").let { LocalDateTime.now().format(it) })
                    append("prompt", "consent")
                }.formUrlEncode()

                call.respondHtml(HttpStatusCode.OK) {
                    body {
                        a {
                            href = "https://accounts.google.com/o/oauth2/auth?$authorizationUrlQuery"
                            // target="_blank"
                            // rel="noopener noreferrer"
                            +"click me"
                        }
                    }
                }
            } else {
                call.respondText("noice");
            }
        }

        get("/callback") {
            val authorizationCode = call.request.queryParameters["code"] ?: return@get;

            val tokenInfo: TokenInfo = client.submitForm(
                url = "https://accounts.google.com/o/oauth2/token",
                formParameters = parameters {
                    append("grant_type", "authorization_code")
                    append("code", authorizationCode)
                    append("client_id", dotenv["GOOGLE_CLOUD_CLIENT_ID"])
                    append("client_secret", dotenv["GOOGLE_CLOUD_CLIENT_SECRET"])
                    append("redirect_uri", "http://localhost:8080/callback")
                }
            ).body()

            if (tokenInfo.refreshToken == null) {
                println("häääää")
            }

            bearerTokenStorage.add(BearerTokens(tokenInfo.accessToken, tokenInfo.refreshToken!!))

            call.respondRedirect("/")
        }

        get("/calendars") {
            val response = client.get("https://www.googleapis.com/calendar/v3/users/me/calendarList")

            call.respondText(response.body())
        }

        get("/events") {
            val response = client.get("https://www.googleapis.com/calendar/v3/calendars/failipp99@gmail.com/events") {
                val now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                println(now.format(formatter))
                println(now.plus(1, ChronoUnit.WEEKS).format(formatter))
                parameter("timeMin", now.format(formatter))
                parameter("timeMax", now.plus(1, ChronoUnit.WEEKS).format(formatter))
                parameter("singleEvents", true)
                parameter("orderBy", "startTime")
            }

            call.respondText(response.body())
        }

        get("/route") {
            val response = client.post("https://routes.googleapis.com/directions/v2:computeRoutes") {
                headers {
                    append("X-Goog-FieldMask", "*")
                }
                
                contentType(ContentType.Application.Json)
                setBody(
                    RoutesRequest(
                        AddressPayload("Keselstraße 25, 87435, Kempten"),
                        AddressPayload("Altes Wasserwerk 10, Bellenberg")
                    )
                )
            }

            call.respondText(response.body())
        }
    }
}

