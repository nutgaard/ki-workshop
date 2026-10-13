import type { Adresse, Eierforhold } from "./types";

/** "Kirkeveien 64A, 0364" */
export function formatAdresse(adresse: Adresse): string {
  return `${adresse.adressenavn} ${adresse.nummer}${adresse.bokstav ?? ""}, ${adresse.postnummer}`;
}

/** "1/2" */
export function formatAndel(eierforhold: Eierforhold): string {
  return `${eierforhold.andelTeller}/${eierforhold.andelNevner}`;
}

/** "612,4 m²", or "–" when areal is unknown. */
export function formatAreal(areal: number | null): string {
  if (areal === null) return "–";
  return `${areal.toLocaleString("nb-NO", { maximumFractionDigits: 1 })} m²`;
}
