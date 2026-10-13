package no.kartverket.workshop

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.matrikkelRoutes(repository: MatrikkelRepository) {
    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }

        route("/api") {
            get("/kommuner") { call.respond(repository.kommuner()) }

            get("/matrikkelenheter") {
                val params = call.request.queryParameters
                call.respond(
                    repository.sokMatrikkelenheter(
                        kommunenummer = params["kommunenummer"]?.takeIf { it.isNotBlank() },
                        gardsnummer = params["gardsnummer"]?.toIntOrNull(),
                        bruksnummer = params["bruksnummer"]?.toIntOrNull(),
                    ),
                )
            }

            get("/matrikkelenheter/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("Ugyldig id")
                call.respond(repository.matrikkelenhet(id))
            }

            post("/matrikkelenheter/{id}/eierforhold") {
                val id = call.parameters["id"]?.toLongOrNull() ?: throw IllegalArgumentException("Ugyldig id")
                val nytt = call.receive<NyttEierforhold>()
                call.respond(HttpStatusCode.Created, repository.leggTilEierforhold(id, nytt))
            }

            get("/adresser") {
                val q = call.request.queryParameters["q"].orEmpty()
                call.respond(if (q.isBlank()) emptyList() else repository.sokAdresser(q))
            }
        }
    }
}
