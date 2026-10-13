# Glossary

Domain terms for Mini-matrikkel. These are simplified versions of the terms in Matrikkelen (M22). Use them as written in code, the API and the UI.

## Matrikkelenhet

**Matrikkelenhet**:
A unit registered in the cadastre with its own `Matrikkelnummer`. In this app it is one row in `matrikkelenhet`.

**Matrikkelnummer**:
The identifier of a `Matrikkelenhet`, made of `Kommunenummer`, `Gardsnummer`, `Bruksnummer`, `Festenummer` and `Seksjonsnummer`. It is written as `kommunenr-gnr/bnr[/fnr[/snr]]`, for example `0301-207/45` or `0301-220/12/0/1`. A trailing zero `fnr`/`snr` is omitted.

**Gardsnummer (gnr)**:
The first-level number within a `Kommune`.

**Bruksnummer (bnr)**:
The number of a unit within a `Gardsnummer`.

**Festenummer (fnr)**:
Non-zero when the unit is a `Festegrunn` (land held under a ground lease). `0` otherwise.

**Seksjonsnummer (snr)**:
Non-zero when the unit is a `Seksjon` of a sectioned property. `0` otherwise.

**Fradeling**:
Creating a new `Matrikkelenhet` by splitting land off an existing one. The new unit gets a new `Bruksnummer` under the same `Gardsnummer`.

**Sammenslåing**:
Merging two or more `Matrikkelenhet` records into one.

## Kommune

**Kommune**:
A municipality, identified by a four-digit `Kommunenummer` (for example `0301` Oslo).

## Eierforhold

**Person**:
A party that can own a `Matrikkelenhet`. `FYSISK` means a natural person and `JURIDISK` means an organisation.

**Eierforhold**:
The fact that a `Person` owns a share (`Andel`) of a `Matrikkelenhet`.

**Andel**:
An ownership share stored as a fraction `andel_teller/andel_nevner`, for example `1/2`. The shares for a `Matrikkelenhet` are expected to sum to `1/1`.

## Adresse and Bygning

**Adresse**:
An official address: `Adressenavn` + `Nummer` + optional `Bokstav`, plus `Postnummer`. Each address points to one `Matrikkelenhet`.

**Adressenavn**:
The street name part of an `Adresse`, for example `Kirkeveien`.

**Bygning**:
A registered building on a `Matrikkelenhet`, identified by a globally unique `Bygningsnummer`.
