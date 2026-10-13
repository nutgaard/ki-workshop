package no.kartverket.workshop

/** An ownership share as an exact fraction. Always stored reduced, with a positive nevner. */
data class Andel private constructor(val teller: Long, val nevner: Long) : Comparable<Andel> {

    operator fun plus(other: Andel) = av(teller * other.nevner + other.teller * nevner, nevner * other.nevner)

    override fun compareTo(other: Andel) = (teller * other.nevner).compareTo(other.teller * nevner)

    override fun toString() = "$teller/$nevner"

    companion object {
        val NULL = Andel(0, 1)
        val HEL = Andel(1, 1)

        fun av(teller: Long, nevner: Long): Andel {
            require(nevner > 0) { "Nevner må være større enn 0" }
            require(teller >= 0) { "Teller kan ikke være negativ" }
            val gcd = gcd(teller, nevner)
            return Andel(teller / gcd, nevner / gcd)
        }

        private tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
    }
}

fun List<Eierforhold>.sumAndeler(): Andel =
    fold(Andel.NULL) { sum, e -> sum + Andel.av(e.andelTeller.toLong(), e.andelNevner.toLong()) }
