package no.kartverket.workshop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatrikkelApiTest {

    private fun withApp(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) {
        val dbFile = File.createTempFile("mini-matrikkel-test", ".db").apply { deleteOnExit() }
        val database = Database("jdbc:sqlite:${dbFile.absolutePath}").apply {
            migrate()
            seedDevData(this)
        }
        testApplication {
            application { module(database) }
            val client = createClient { install(ContentNegotiation) { json() } }
            block(client)
        }
    }

    @Test
    fun `lists kommuner from seed data`() = withApp { client ->
        val kommuner = client.get("/api/kommuner").body<List<Kommune>>()
        assertEquals(listOf("0301", "3301", "4601", "5001"), kommuner.map { it.kommunenummer })
    }

    @Test
    fun `searches matrikkelenheter by gardsnummer and bruksnummer`() = withApp { client ->
        val treff = client.get("/api/matrikkelenheter?kommunenummer=0301&gardsnummer=207&bruksnummer=45")
            .body<List<MatrikkelenhetSammendrag>>()
        assertEquals(listOf("0301-207/45"), treff.map { it.matrikkelnummer })
    }

    @Test
    fun `returns matrikkelenhet with eierforhold, adresser and bygninger`() = withApp { client ->
        val enhet = client.get("/api/matrikkelenheter/3").body<Matrikkelenhet>()
        assertEquals("Oslo", enhet.kommune.navn)
        assertEquals(listOf("Oslo kommune"), enhet.eierforhold.map { it.person.navn })
        assertEquals(listOf("64A", "64B"), enhet.adresser.map { "${it.nummer}${it.bokstav.orEmpty()}" })
        assertEquals(listOf("Skole"), enhet.bygninger.map { it.bygningstype })
    }

    @Test
    fun `returns 404 for unknown matrikkelenhet`() = withApp { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/api/matrikkelenheter/9999").status)
    }

    @Test
    fun `adds eierforhold to matrikkelenhet`() = withApp { client ->
        val response = client.post("/api/matrikkelenheter/7/eierforhold") {
            contentType(ContentType.Application.Json)
            setBody(NyttEierforhold(personId = 2, andelTeller = 1, andelNevner = 4))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(3, response.body<Matrikkelenhet>().eierforhold.size)
    }

    @Test
    fun `searches adresser by prefix`() = withApp { client ->
        val adresser = client.get("/api/adresser?q=Kirke").body<List<Adresse>>()
        assertTrue(adresser.isNotEmpty())
        assertTrue(adresser.all { it.adressenavn == "Kirkeveien" })
    }

    @Test
    fun `formats matrikkelnummer`() {
        assertEquals("0301-207/45", formatMatrikkelnummer("0301", 207, 45, 0, 0))
        assertEquals("0301-208/1/3", formatMatrikkelnummer("0301", 208, 1, 3, 0))
        assertEquals("0301-220/12/0/1", formatMatrikkelnummer("0301", 220, 12, 0, 1))
    }
}
