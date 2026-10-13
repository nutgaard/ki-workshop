package no.kartverket.workshop

import kotlinx.serialization.Serializable

@Serializable
data class Kommune(val kommunenummer: String, val navn: String)

@Serializable
data class MatrikkelenhetSammendrag(
    val id: Long,
    val matrikkelnummer: String,
    val kommunenummer: String,
    val gardsnummer: Int,
    val bruksnummer: Int,
    val festenummer: Int,
    val seksjonsnummer: Int,
    val areal: Double?,
)

@Serializable
data class Matrikkelenhet(
    val id: Long,
    val matrikkelnummer: String,
    val kommune: Kommune,
    val gardsnummer: Int,
    val bruksnummer: Int,
    val festenummer: Int,
    val seksjonsnummer: Int,
    val areal: Double?,
    val etablertDato: String,
    val eierforhold: List<Eierforhold>,
    val adresser: List<Adresse>,
    val bygninger: List<Bygning>,
)

@Serializable
data class Person(val id: Long, val navn: String, val type: String)

@Serializable
data class Eierforhold(val id: Long, val person: Person, val andelTeller: Int, val andelNevner: Int)

@Serializable
data class Adresse(
    val id: Long,
    val kommunenummer: String,
    val adressenavn: String,
    val nummer: Int,
    val bokstav: String?,
    val postnummer: String,
    val matrikkelenhetId: Long,
)

@Serializable
data class Bygning(val id: Long, val bygningsnummer: Long, val bygningstype: String, val byggeaar: Int?)

@Serializable
data class NyttEierforhold(val personId: Long, val andelTeller: Int, val andelNevner: Int)

@Serializable
data class ErrorResponse(val message: String)

/** Formats a matrikkelnummer as `kommunenr-gnr/bnr[/fnr[/snr]]`, e.g. `0301-207/45` or `0301-220/12/0/1`. */
fun formatMatrikkelnummer(kommunenummer: String, gnr: Int, bnr: Int, fnr: Int, snr: Int): String {
    val parts = mutableListOf(gnr, bnr)
    if (fnr != 0 || snr != 0) parts += fnr
    if (snr != 0) parts += snr
    return "$kommunenummer-${parts.joinToString("/")}"
}
