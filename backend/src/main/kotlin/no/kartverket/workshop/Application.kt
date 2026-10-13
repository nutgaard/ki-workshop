package no.kartverket.workshop

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8090
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:sqlite:mini-matrikkel.db"
    embeddedServer(Netty, port = port, host = "127.0.0.1") {
        module(Database(dbUrl))
    }.start(wait = true)
}

fun Application.module(database: Database) {
    database.migrate()
    val repository = MatrikkelRepository(database)

    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Not found"))
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Bad request"))
        }
    }

    matrikkelRoutes(repository)
}

class NotFoundException(message: String) : RuntimeException(message)
