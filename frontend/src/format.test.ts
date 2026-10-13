import { describe, expect, test } from "bun:test";
import { formatAdresse, formatAndel, formatAreal } from "./format";

describe("formatAdresse", () => {
  test("includes bokstav when present", () => {
    expect(
      formatAdresse({
        id: 1,
        kommunenummer: "0301",
        adressenavn: "Kirkeveien",
        nummer: 64,
        bokstav: "A",
        postnummer: "0364",
        matrikkelenhetId: 3,
      }),
    ).toBe("Kirkeveien 64A, 0364");
  });

  test("omits bokstav when null", () => {
    expect(
      formatAdresse({
        id: 2,
        kommunenummer: "0301",
        adressenavn: "Storgata",
        nummer: 1,
        bokstav: null,
        postnummer: "0155",
        matrikkelenhetId: 1,
      }),
    ).toBe("Storgata 1, 0155");
  });
});

test("formatAndel", () => {
  expect(formatAndel({ id: 1, person: { id: 1, navn: "Kari", type: "FYSISK" }, andelTeller: 1, andelNevner: 2 })).toBe("1/2");
});

test("formatAreal", () => {
  expect(formatAreal(null)).toBe("–");
  expect(formatAreal(612.4)).toBe("612,4 m²");
});
