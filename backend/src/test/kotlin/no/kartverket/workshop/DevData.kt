package no.kartverket.workshop

import kotliquery.TransactionalSession
import kotliquery.queryOf

/**
 * Fictional dev/test data. Any resemblance to real cadastral units or people is coincidental.
 *
 * Run against the local database with `./gradlew seed`. Tests call [seedDevData] directly.
 * Some rows are deliberately flawed so workshop tickets have something to find:
 *  - matrikkelenhet 7 has eierforhold that sum to 3/4
 *  - "Gårdsvegen" / "gårdsvegen" have inconsistent casing
 */
fun main() {
    val database = Database(System.getenv("DB_URL") ?: "jdbc:sqlite:mini-matrikkel.db")
    database.migrate()
    if (seedDevData(database)) println("Seeded dev data") else println("Database already has data, skipping seed")
}

/** Inserts the dev data set if the database is empty. Returns false if it already had data. */
fun seedDevData(database: Database): Boolean = database.transaction { tx ->
    val antallKommuner = tx.run(queryOf("SELECT COUNT(*) AS antall FROM kommune").map { it.int("antall") }.asSingle) ?: 0
    if (antallKommuner > 0) return@transaction false

    tx.insertAll(
        "INSERT INTO kommune (kommunenummer, navn) VALUES (:kommunenummer, :navn)",
        listOf("0301" to "Oslo", "3301" to "Drammen", "4601" to "Bergen", "5001" to "Trondheim")
            .map { (nr, navn) -> mapOf("kommunenummer" to nr, "navn" to navn) },
    )

    tx.insertAll(
        """
        INSERT INTO matrikkelenhet (id, kommunenummer, gardsnummer, bruksnummer, festenummer, seksjonsnummer, areal, etablert_dato)
        VALUES (:id, :kommunenummer, :gnr, :bnr, :fnr, :snr, :areal, :etablert)
        """.trimIndent(),
        listOf(
            enhet(1, "0301", 207, 45, areal = 612.4, etablert = "1958-04-12"),
            enhet(2, "0301", 207, 46, areal = 598.0, etablert = "1958-04-12"),
            enhet(3, "0301", 208, 1, areal = 4120.7, etablert = "1921-09-01"),
            enhet(4, "0301", 208, 1, fnr = 3, areal = 310.0, etablert = "1987-06-20"),
            enhet(5, "0301", 220, 12, areal = 1503.3, etablert = "2004-11-30"),
            enhet(6, "0301", 220, 12, snr = 1, areal = null, etablert = "2005-02-14"),
            enhet(7, "0301", 220, 12, snr = 2, areal = null, etablert = "2005-02-14"),
            enhet(8, "3301", 113, 7, areal = 845.9, etablert = "1972-03-03"),
            enhet(9, "3301", 113, 8, areal = 902.1, etablert = "1972-03-03"),
            enhet(10, "4601", 166, 330, areal = 455.5, etablert = "1999-08-08"),
            enhet(11, "4601", 166, 331, areal = 470.2, etablert = "1999-08-08"),
            enhet(12, "5001", 401, 2, areal = 25033.0, etablert = "1890-01-01"),
        ),
    )

    tx.insertAll(
        "INSERT INTO person (id, navn, type, identifikator) VALUES (:id, :navn, :type, :identifikator)",
        listOf(
            person(1, "Kari Nordmann", "FYSISK", "fiktiv-fnr-0001"),
            person(2, "Ola Nordmann", "FYSISK", "fiktiv-fnr-0002"),
            person(3, "Per Hansen", "FYSISK", "fiktiv-fnr-0003"),
            person(4, "Ingrid Berg", "FYSISK", "fiktiv-fnr-0004"),
            person(5, "Fjordbris Eiendom AS", "JURIDISK", "999000001"),
            person(6, "Trøndelag Gårdsdrift SA", "JURIDISK", "999000002"),
            person(7, "Oslo kommune", "JURIDISK", "999000003"),
        ),
    )

    tx.insertAll(
        """
        INSERT INTO eierforhold (matrikkelenhet_id, person_id, andel_teller, andel_nevner)
        VALUES (:enhet, :person, :teller, :nevner)
        """.trimIndent(),
        listOf(
            eier(1, 1, 1, 2),
            eier(1, 2, 1, 2),
            eier(2, 3, 1, 1),
            eier(3, 7, 1, 1),
            eier(4, 4, 1, 1),
            eier(5, 5, 1, 1),
            eier(6, 1, 1, 1),
            eier(7, 3, 1, 2),
            eier(7, 4, 1, 4), // deliberately sums to 3/4
            eier(8, 2, 1, 1),
            eier(9, 2, 1, 3),
            eier(9, 4, 2, 3),
            eier(10, 5, 1, 1),
            eier(11, 5, 1, 1),
            eier(12, 6, 1, 1),
        ),
    )

    tx.insertAll(
        """
        INSERT INTO adresse (kommunenummer, adressenavn, nummer, bokstav, postnummer, matrikkelenhet_id)
        VALUES (:kommunenummer, :adressenavn, :nummer, :bokstav, :postnummer, :enhet)
        """.trimIndent(),
        listOf(
            adresse("0301", "Storgata", 1, null, "0155", 1),
            adresse("0301", "Storgata", 3, null, "0155", 2),
            adresse("0301", "Kirkeveien", 64, "A", "0364", 3),
            adresse("0301", "Kirkeveien", 64, "B", "0364", 3),
            adresse("0301", "Kirkeveien", 66, null, "0364", 4),
            adresse("0301", "Sannergata", 14, null, "0557", 5),
            adresse("3301", "Strandveien", 21, null, "3015", 8),
            adresse("3301", "Strandveien", 23, null, "3015", 9),
            adresse("4601", "Øvre Ullern terrasse", 2, null, "5009", 10),
            adresse("4601", "Kong Oscars gate", 7, "C", "5017", 11),
            adresse("5001", "Gårdsvegen", 100, null, "7080", 12),
            adresse("5001", "gårdsvegen", 102, null, "7080", 12), // deliberately inconsistent casing
        ),
    )

    tx.insertAll(
        """
        INSERT INTO bygning (bygningsnummer, bygningstype, byggeaar, matrikkelenhet_id)
        VALUES (:bygningsnummer, :bygningstype, :byggeaar, :enhet)
        """.trimIndent(),
        listOf(
            bygning(80012345, "Enebolig", 1960, 1),
            bygning(80012346, "Enebolig", 1961, 2),
            bygning(80012347, "Skole", 1925, 3),
            bygning(80012348, "Garasje", 1988, 4),
            bygning(80012349, "Blokk", 2006, 5),
            bygning(80012350, "Tomannsbolig", 1974, 8),
            bygning(80012351, "Enebolig", 1975, 9),
            bygning(80012352, "Rekkehus", 2001, 10),
            bygning(80012353, "Rekkehus", 2001, 11),
            bygning(80012354, "Våningshus", 1895, 12),
            bygning(80012355, "Låve", 1902, 12),
        ),
    )
    true
}

private fun TransactionalSession.insertAll(sql: String, rows: List<Map<String, Any?>>) {
    rows.forEach { run(queryOf(sql, it).asUpdate) }
}

private fun enhet(id: Int, kommune: String, gnr: Int, bnr: Int, fnr: Int = 0, snr: Int = 0, areal: Double?, etablert: String) =
    mapOf("id" to id, "kommunenummer" to kommune, "gnr" to gnr, "bnr" to bnr, "fnr" to fnr, "snr" to snr, "areal" to areal, "etablert" to etablert)

private fun person(id: Int, navn: String, type: String, identifikator: String) =
    mapOf("id" to id, "navn" to navn, "type" to type, "identifikator" to identifikator)

private fun eier(enhet: Int, person: Int, teller: Int, nevner: Int) =
    mapOf("enhet" to enhet, "person" to person, "teller" to teller, "nevner" to nevner)

private fun adresse(kommune: String, navn: String, nummer: Int, bokstav: String?, postnummer: String, enhet: Int) =
    mapOf("kommunenummer" to kommune, "adressenavn" to navn, "nummer" to nummer, "bokstav" to bokstav, "postnummer" to postnummer, "enhet" to enhet)

private fun bygning(bygningsnummer: Long, type: String, byggeaar: Int?, enhet: Int) =
    mapOf("bygningsnummer" to bygningsnummer, "bygningstype" to type, "byggeaar" to byggeaar, "enhet" to enhet)
