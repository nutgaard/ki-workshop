package no.kartverket.workshop

import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource

class Database(url: String) {
    private val dataSource = SQLiteDataSource(SQLiteConfig().apply { enforceForeignKeys(true) }).apply {
        this.url = url
    }

    /** Applies schema migrations. Flyway owns the schema only, never data. */
    fun migrate() {
        Flyway.configure().dataSource(dataSource).load().migrate()
    }

    fun <T> session(block: (Session) -> T): T = sessionOf(dataSource).use(block)

    fun <T> transaction(block: (TransactionalSession) -> T): T = sessionOf(dataSource).use { it.transaction(block) }
}
