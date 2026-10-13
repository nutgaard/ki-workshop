import { useEffect, useState } from "react";
import { hentKommuner, hentMatrikkelenhet, sokAdresser, sokMatrikkelenheter } from "./api";
import { formatAdresse, formatAndel, formatAreal } from "./format";
import type { Adresse, Kommune, Matrikkelenhet, MatrikkelenhetSammendrag } from "./types";

type Fane = "matrikkel" | "adresse";

export function App() {
  const [fane, setFane] = useState<Fane>("matrikkel");
  const [valgtId, setValgtId] = useState<number | null>(null);

  return (
    <div className="app">
      <header>
        <h1>Mini-matrikkel</h1>
        <nav>
          <button className={fane === "matrikkel" ? "aktiv" : ""} onClick={() => setFane("matrikkel")}>
            Matrikkelenheter
          </button>
          <button className={fane === "adresse" ? "aktiv" : ""} onClick={() => setFane("adresse")}>
            Adresser
          </button>
        </nav>
      </header>
      <main>
        <section className="sok">
          {fane === "matrikkel" ? <MatrikkelSok onVelg={setValgtId} /> : <AdresseSok onVelg={setValgtId} />}
        </section>
        <section className="detalj">
          {valgtId === null ? <p className="tom">Velg en matrikkelenhet</p> : <MatrikkelenhetDetalj id={valgtId} />}
        </section>
      </main>
    </div>
  );
}

function MatrikkelSok({ onVelg }: { onVelg: (id: number) => void }) {
  const [kommuner, setKommuner] = useState<Kommune[]>([]);
  const [matrikkelnummer, setMatrikkelnummer] = useState("");
  const [kommunenummer, setKommunenummer] = useState("");
  const [gardsnummer, setGardsnummer] = useState("");
  const [bruksnummer, setBruksnummer] = useState("");
  const [treff, setTreff] = useState<MatrikkelenhetSammendrag[] | null>(null);
  const [feil, setFeil] = useState<string | null>(null);

  useEffect(() => {
    hentKommuner().then(setKommuner).catch((e: Error) => setFeil(e.message));
  }, []);

  function sok(event: React.FormEvent) {
    event.preventDefault();
    setFeil(null);
    const kriterier = matrikkelnummer.trim() ? { matrikkelnummer } : { kommunenummer, gardsnummer, bruksnummer };
    sokMatrikkelenheter(kriterier)
      .then(setTreff)
      .catch((e: Error) => setFeil(e.message));
  }

  return (
    <>
      <form onSubmit={sok}>
        <label className="bred">
          Matrikkelnummer
          <input
            value={matrikkelnummer}
            placeholder="f.eks. 0301-207/45"
            onChange={(e) => setMatrikkelnummer(e.target.value)}
          />
        </label>
        <button type="submit">Søk</button>
      </form>
      <p className="eller">eller</p>
      <form onSubmit={sok}>
        <label>
          Kommune
          <select value={kommunenummer} onChange={(e) => setKommunenummer(e.target.value)}>
            <option value="">Alle</option>
            {kommuner.map((k) => (
              <option key={k.kommunenummer} value={k.kommunenummer}>
                {k.kommunenummer} {k.navn}
              </option>
            ))}
          </select>
        </label>
        <label>
          Gnr
          <input inputMode="numeric" value={gardsnummer} onChange={(e) => setGardsnummer(e.target.value)} />
        </label>
        <label>
          Bnr
          <input inputMode="numeric" value={bruksnummer} onChange={(e) => setBruksnummer(e.target.value)} />
        </label>
        <button type="submit">Søk</button>
      </form>
      {feil && <p className="feil">{feil}</p>}
      {treff && (
        <ul className="treff">
          {treff.length === 0 && <li className="tom">Ingen treff</li>}
          {treff.map((t) => (
            <li key={t.id}>
              <button onClick={() => onVelg(t.id)}>
                <strong>{t.matrikkelnummer}</strong> <span>{formatAreal(t.areal)}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}

function AdresseSok({ onVelg }: { onVelg: (id: number) => void }) {
  const [q, setQ] = useState("");
  const [treff, setTreff] = useState<Adresse[] | null>(null);
  const [feil, setFeil] = useState<string | null>(null);

  function sok(event: React.FormEvent) {
    event.preventDefault();
    setFeil(null);
    sokAdresser(q)
      .then(setTreff)
      .catch((e: Error) => setFeil(e.message));
  }

  return (
    <>
      <form onSubmit={sok}>
        <label className="bred">
          Adresse
          <input value={q} placeholder="f.eks. Kirkeveien" onChange={(e) => setQ(e.target.value)} />
        </label>
        <button type="submit">Søk</button>
      </form>
      {feil && <p className="feil">{feil}</p>}
      {treff && (
        <ul className="treff">
          {treff.length === 0 && <li className="tom">Ingen treff</li>}
          {treff.map((a) => (
            <li key={a.id}>
              <button onClick={() => onVelg(a.matrikkelenhetId)}>{formatAdresse(a)}</button>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}

function MatrikkelenhetDetalj({ id }: { id: number }) {
  const [enhet, setEnhet] = useState<Matrikkelenhet | null>(null);
  const [feil, setFeil] = useState<string | null>(null);

  useEffect(() => {
    setEnhet(null);
    setFeil(null);
    hentMatrikkelenhet(id).then(setEnhet).catch((e: Error) => setFeil(e.message));
  }, [id]);

  if (feil) return <p className="feil">{feil}</p>;
  if (!enhet) return <p className="tom">Laster…</p>;

  return (
    <article>
      <h2>{enhet.matrikkelnummer}</h2>
      <dl>
        <dt>Kommune</dt>
        <dd>
          {enhet.kommune.kommunenummer} {enhet.kommune.navn}
        </dd>
        <dt>Areal</dt>
        <dd>{formatAreal(enhet.areal)}</dd>
        <dt>Etablert</dt>
        <dd>{enhet.etablertDato}</dd>
      </dl>

      <h3>Eierforhold</h3>
      {enhet.sumAndeler !== "1/1" && (
        <p className="advarsel">Andelene summerer til {enhet.sumAndeler}, ikke 1/1.</p>
      )}
      <table>
        <thead>
          <tr>
            <th>Eier</th>
            <th>Type</th>
            <th>Andel</th>
          </tr>
        </thead>
        <tbody>
          {enhet.eierforhold.map((e) => (
            <tr key={e.id}>
              <td>{e.person.navn}</td>
              <td>{e.person.type === "FYSISK" ? "Person" : "Virksomhet"}</td>
              <td>{formatAndel(e)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>Adresser</h3>
      {enhet.adresser.length === 0 ? (
        <p className="tom">Ingen adresser</p>
      ) : (
        <ul>
          {enhet.adresser.map((a) => (
            <li key={a.id}>{formatAdresse(a)}</li>
          ))}
        </ul>
      )}

      <h3>Bygninger</h3>
      {enhet.bygninger.length === 0 ? (
        <p className="tom">Ingen bygninger</p>
      ) : (
        <ul>
          {enhet.bygninger.map((b) => (
            <li key={b.id}>
              {b.bygningstype} ({b.bygningsnummer}){b.byggeaar ? `, bygget ${b.byggeaar}` : ""}
            </li>
          ))}
        </ul>
      )}
    </article>
  );
}
