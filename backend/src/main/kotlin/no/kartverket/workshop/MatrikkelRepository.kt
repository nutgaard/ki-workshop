package no.kartverket.workshop

import kotliquery.Row
import kotliquery.queryOf

class MatrikkelRepository(private val database: Database) {

    fun kommuner(): List<Kommune> = database.session { session ->
        session.run(
            queryOf("SELECT kommunenummer, navn FROM kommune ORDER BY kommunenummer")
                .map { Kommune(it.string("kommunenummer"), it.string("navn")) }
                .asList,
        )
    }

    fun sokMatrikkelenheter(
        kommunenummer: String?,
        gardsnummer: Int?,
        bruksnummer: Int?,
        festenummer: Int? = null,
        seksjonsnummer: Int? = null,
    ): List<MatrikkelenhetSammendrag> =
        database.session { session ->
            session.run(
                queryOf(
                    """
                    SELECT id, kommunenummer, gardsnummer, bruksnummer, festenummer, seksjonsnummer, areal
                    FROM matrikkelenhet
                    WHERE (:kommunenummer IS NULL OR kommunenummer = :kommunenummer)
                      AND (:gardsnummer IS NULL OR gardsnummer = :gardsnummer)
                      AND (:bruksnummer IS NULL OR bruksnummer = :bruksnummer)
                      AND (:festenummer IS NULL OR festenummer = :festenummer)
                      AND (:seksjonsnummer IS NULL OR seksjonsnummer = :seksjonsnummer)
                    ORDER BY kommunenummer, gardsnummer, bruksnummer, festenummer, seksjonsnummer
                    """.trimIndent(),
                    mapOf(
                        "kommunenummer" to kommunenummer,
                        "gardsnummer" to gardsnummer,
                        "bruksnummer" to bruksnummer,
                        "festenummer" to festenummer,
                        "seksjonsnummer" to seksjonsnummer,
                    ),
                ).map { it.toSammendrag() }.asList,
            )
        }

    fun matrikkelenhet(id: Long): Matrikkelenhet = database.session { session ->
        val enhet = session.run(
            queryOf(
                """
                SELECT m.*, k.navn AS kommunenavn
                FROM matrikkelenhet m JOIN kommune k ON k.kommunenummer = m.kommunenummer
                WHERE m.id = :id
                """.trimIndent(),
                mapOf("id" to id),
            ).map { row ->
                val sammendrag = row.toSammendrag()
                Matrikkelenhet(
                    id = sammendrag.id,
                    matrikkelnummer = sammendrag.matrikkelnummer,
                    kommune = Kommune(sammendrag.kommunenummer, row.string("kommunenavn")),
                    gardsnummer = sammendrag.gardsnummer,
                    bruksnummer = sammendrag.bruksnummer,
                    festenummer = sammendrag.festenummer,
                    seksjonsnummer = sammendrag.seksjonsnummer,
                    areal = sammendrag.areal,
                    etablertDato = row.string("etablert_dato"),
                    eierforhold = emptyList(),
                    sumAndeler = Andel.NULL.toString(),
                    adresser = emptyList(),
                    bygninger = emptyList(),
                )
            }.asSingle,
        ) ?: throw NotFoundException("Matrikkelenhet $id finnes ikke")

        val eierforhold = session.run(
            queryOf(
                """
                SELECT e.id, e.andel_teller, e.andel_nevner, p.id AS person_id, p.navn, p.type
                FROM eierforhold e JOIN person p ON p.id = e.person_id
                WHERE e.matrikkelenhet_id = :id
                ORDER BY e.id
                """.trimIndent(),
                mapOf("id" to id),
            ).map {
                Eierforhold(
                    id = it.long("id"),
                    person = Person(it.long("person_id"), it.string("navn"), it.string("type")),
                    andelTeller = it.int("andel_teller"),
                    andelNevner = it.int("andel_nevner"),
                )
            }.asList,
        )
        enhet.copy(
            eierforhold = eierforhold,
            sumAndeler = eierforhold.sumAndeler().toString(),
            adresser = session.run(
                queryOf(
                    "SELECT * FROM adresse WHERE matrikkelenhet_id = :id ORDER BY adressenavn, nummer, bokstav",
                    mapOf("id" to id),
                ).map { it.toAdresse() }.asList,
            ),
            bygninger = session.run(
                queryOf(
                    "SELECT * FROM bygning WHERE matrikkelenhet_id = :id ORDER BY bygningsnummer",
                    mapOf("id" to id),
                ).map {
                    Bygning(it.long("id"), it.long("bygningsnummer"), it.string("bygningstype"), it.intOrNull("byggeaar"))
                }.asList,
            ),
        )
    }

    /** Simple prefix search on adressenavn. No fuzzy matching. */
    fun sokAdresser(q: String): List<Adresse> = database.session { session ->
        session.run(
            queryOf(
                "SELECT * FROM adresse WHERE adressenavn LIKE :prefix ORDER BY adressenavn, nummer, bokstav LIMIT 50",
                mapOf("prefix" to "$q%"),
            ).map { it.toAdresse() }.asList,
        )
    }

    /**
     * Adds an eierforhold. Rejects it if the total for the matrikkelenhet would exceed 1/1.
     * A total below 1/1 is allowed, because owners are often registered one at a time.
     */
    fun leggTilEierforhold(matrikkelenhetId: Long, nytt: NyttEierforhold): Matrikkelenhet {
        val enhet = matrikkelenhet(matrikkelenhetId) // throws NotFoundException if missing
        require(nytt.andelTeller > 0) { "Andel må være større enn 0" }
        val nySum = enhet.eierforhold.sumAndeler() + Andel.av(nytt.andelTeller.toLong(), nytt.andelNevner.toLong())
        require(nySum <= Andel.HEL) {
            "Summen av andeler for ${enhet.matrikkelnummer} blir $nySum, som er mer enn 1/1"
        }
        database.session { session ->
            session.run(
                queryOf(
                    """
                    INSERT INTO eierforhold (matrikkelenhet_id, person_id, andel_teller, andel_nevner)
                    VALUES (:matrikkelenhetId, :personId, :andelTeller, :andelNevner)
                    """.trimIndent(),
                    mapOf(
                        "matrikkelenhetId" to matrikkelenhetId,
                        "personId" to nytt.personId,
                        "andelTeller" to nytt.andelTeller,
                        "andelNevner" to nytt.andelNevner,
                    ),
                ).asUpdate,
            )
        }
        return matrikkelenhet(matrikkelenhetId)
    }
}

private fun Row.toSammendrag(): MatrikkelenhetSammendrag {
    val kommunenummer = string("kommunenummer")
    val gnr = int("gardsnummer")
    val bnr = int("bruksnummer")
    val fnr = int("festenummer")
    val snr = int("seksjonsnummer")
    return MatrikkelenhetSammendrag(
        id = long("id"),
        matrikkelnummer = formatMatrikkelnummer(kommunenummer, gnr, bnr, fnr, snr),
        kommunenummer = kommunenummer,
        gardsnummer = gnr,
        bruksnummer = bnr,
        festenummer = fnr,
        seksjonsnummer = snr,
        areal = doubleOrNull("areal"),
    )
}

private fun Row.toAdresse() = Adresse(
    id = long("id"),
    kommunenummer = string("kommunenummer"),
    adressenavn = string("adressenavn"),
    nummer = int("nummer"),
    bokstav = stringOrNull("bokstav"),
    postnummer = string("postnummer"),
    matrikkelenhetId = long("matrikkelenhet_id"),
)
