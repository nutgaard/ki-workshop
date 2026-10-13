export interface Kommune {
  kommunenummer: string;
  navn: string;
}

export interface MatrikkelenhetSammendrag {
  id: number;
  matrikkelnummer: string;
  kommunenummer: string;
  gardsnummer: number;
  bruksnummer: number;
  festenummer: number;
  seksjonsnummer: number;
  areal: number | null;
}

export interface Person {
  id: number;
  navn: string;
  type: "FYSISK" | "JURIDISK";
}

export interface Eierforhold {
  id: number;
  person: Person;
  andelTeller: number;
  andelNevner: number;
}

export interface Adresse {
  id: number;
  kommunenummer: string;
  adressenavn: string;
  nummer: number;
  bokstav: string | null;
  postnummer: string;
  matrikkelenhetId: number;
}

export interface Bygning {
  id: number;
  bygningsnummer: number;
  bygningstype: string;
  byggeaar: number | null;
}

export interface Matrikkelenhet {
  id: number;
  matrikkelnummer: string;
  kommune: Kommune;
  gardsnummer: number;
  bruksnummer: number;
  festenummer: number;
  seksjonsnummer: number;
  areal: number | null;
  etablertDato: string;
  eierforhold: Eierforhold[];
  adresser: Adresse[];
  bygninger: Bygning[];
}
