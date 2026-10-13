CREATE TABLE kommune (
    kommunenummer TEXT PRIMARY KEY,
    navn          TEXT NOT NULL
);

CREATE TABLE matrikkelenhet (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    kommunenummer   TEXT    NOT NULL REFERENCES kommune (kommunenummer),
    gardsnummer     INTEGER NOT NULL,
    bruksnummer     INTEGER NOT NULL,
    festenummer     INTEGER NOT NULL DEFAULT 0,
    seksjonsnummer  INTEGER NOT NULL DEFAULT 0,
    areal           REAL,
    etablert_dato   TEXT    NOT NULL,
    UNIQUE (kommunenummer, gardsnummer, bruksnummer, festenummer, seksjonsnummer)
);

CREATE TABLE person (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    navn          TEXT NOT NULL,
    type          TEXT NOT NULL CHECK (type IN ('FYSISK', 'JURIDISK')),
    identifikator TEXT NOT NULL
);

CREATE TABLE eierforhold (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    matrikkelenhet_id  INTEGER NOT NULL REFERENCES matrikkelenhet (id),
    person_id          INTEGER NOT NULL REFERENCES person (id),
    andel_teller       INTEGER NOT NULL,
    andel_nevner       INTEGER NOT NULL
);

CREATE TABLE adresse (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    kommunenummer      TEXT    NOT NULL REFERENCES kommune (kommunenummer),
    adressenavn        TEXT    NOT NULL,
    nummer             INTEGER NOT NULL,
    bokstav            TEXT,
    postnummer         TEXT    NOT NULL,
    matrikkelenhet_id  INTEGER NOT NULL REFERENCES matrikkelenhet (id)
);

CREATE TABLE bygning (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    bygningsnummer     INTEGER NOT NULL UNIQUE,
    bygningstype       TEXT    NOT NULL,
    byggeaar           INTEGER,
    matrikkelenhet_id  INTEGER NOT NULL REFERENCES matrikkelenhet (id)
);
