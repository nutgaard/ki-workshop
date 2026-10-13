import type { Adresse, Kommune, Matrikkelenhet, MatrikkelenhetSammendrag } from "./types";

async function getJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as { message?: string } | null;
    throw new Error(body?.message ?? `${response.status} ${response.statusText}`);
  }
  return response.json() as Promise<T>;
}

export function hentKommuner(): Promise<Kommune[]> {
  return getJson("/api/kommuner");
}

export interface MatrikkelSok {
  matrikkelnummer?: string;
  kommunenummer?: string;
  gardsnummer?: string;
  bruksnummer?: string;
}

export function sokMatrikkelenheter(sok: MatrikkelSok): Promise<MatrikkelenhetSammendrag[]> {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(sok)) {
    if (value) params.set(key, value);
  }
  return getJson(`/api/matrikkelenheter?${params}`);
}

export function hentMatrikkelenhet(id: number): Promise<Matrikkelenhet> {
  return getJson(`/api/matrikkelenheter/${id}`);
}

export function sokAdresser(q: string): Promise<Adresse[]> {
  return getJson(`/api/adresser?q=${encodeURIComponent(q)}`);
}
