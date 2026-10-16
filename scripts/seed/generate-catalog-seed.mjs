#!/usr/bin/env node
// Genera src/main/resources/db/seed/R__catalog_seed.sql a partir de los datos que hoy muestra el sitio
// (carpeta hermana ../Frontend): catálogo de diseño (modules/catalog/data.ts), fichas con página propia
// (modules/catalog/registry.tsx y modules/quote/products.ts), catálogo de los PDF de precios (lib/catalog*.ts),
// biblioteca de recursos (modules/resources/library.ts) y las fichas PDF reales de public/docs (tamaño y SHA-256).
//
// Uso: node scripts/seed/generate-catalog-seed.mjs [ruta-al-frontend]
// Requiere Node 22.18+ (ejecuta TypeScript sin compilar). Los datos quedan con data_verified = false: son de
// diseño o de listas de precios y FUNDIGSAC debe validarlos antes de publicarlos como especificación oficial.
// Nunca se copian precios ni existencias.
import { createHash } from "node:crypto";
import { cpSync, mkdtempSync, readFileSync, rmSync, statSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";

const backend = path.resolve(import.meta.dirname, "..", "..");
const frontend = path.resolve(process.argv[2] ?? path.join(backend, "..", "Frontend"));
const src = path.join(frontend, "src");

// Copia temporal: Node exige extensión en los imports relativos que no son solo de tipos.
const work = mkdtempSync(path.join(tmpdir(), "fundigsac-seed-"));
for (const file of ["modules/catalog/data.ts", "modules/quote/products.ts", "modules/resources/library.ts", "lib/catalog.ts", "lib/catalog-pdf.ts"]) {
  cpSync(path.join(src, file), path.join(work, file));
}
const catalogTs = path.join(work, "lib/catalog.ts");
writeFileSync(catalogTs, readFileSync(catalogTs, "utf8").replace('from "./catalog-pdf"', 'from "./catalog-pdf.ts"'));
writeFileSync(path.join(work, "package.json"), '{ "type": "module" }');
const load = (file) => import(pathToFileURL(path.join(work, file)).href);
const { CATALOG_PRODUCTS, FAMILY_LABEL } = await load("modules/catalog/data.ts");
const { QUOTE_PRODUCTS } = await load("modules/quote/products.ts");
const { RESOURCE_ITEMS } = await load("modules/resources/library.ts");
const { products: PDF_PRODUCTS, categories: PDF_CATEGORIES } = await load("lib/catalog.ts");
rmSync(work, { recursive: true, force: true });

// ---------- utilidades ----------
const uuid = (kind, key) => {
  const h = createHash("sha1").update(`fundigsac:${kind}:${key}`).digest();
  h[6] = (h[6] & 0x0f) | 0x50; // versión 5 (determinista: misma clave → mismo id en cada ejecución)
  h[8] = (h[8] & 0x3f) | 0x80;
  const x = h.subarray(0, 16).toString("hex");
  return `${x.slice(0, 8)}-${x.slice(8, 12)}-${x.slice(12, 16)}-${x.slice(16, 20)}-${x.slice(20)}`;
};
const q = (v) => (v === null || v === undefined ? "NULL" : `'${String(v).replace(/'/g, "''")}'`);
const arr = (values, type = "text") => (values?.length ? `ARRAY[${values.map((v) => (type === "text" ? q(v) : Number(v))).join(", ")}]::${type}[]` : `'{}'::${type}[]`);
const json = (value) => `${q(JSON.stringify(value))}::jsonb`;
// Misma normalización que Slug.fold en Java: sin tildes, minúsculas, espacios simples.
const fold = (text) => text.normalize("NFD").replace(/\p{M}+/gu, "").toLowerCase().replace(/\s+/g, " ").trim();
const dnOf = (label) => { const m = /DN\s?(\d{2,4})/.exec(label); return m ? Number(m[1]) : null; };
const pnOf = (label) => { const m = /PN\s?(\d{1,2})/.exec(label); return m ? Number(m[1]) : null; };

const NOW = "TIMESTAMPTZ '2026-10-16 00:00:00+00'";
const out = [];
const emit = (sql) => out.push(sql);

// ---------- categorías ----------
const CATEGORIES = [
  { slug: "valvulas", name: "Válvulas", description: "Válvulas de compuerta, mariposa, retención, regulación y aire para redes de agua y procesos." },
  { slug: "tuberias", name: "Tuberías", description: "Tuberías de hierro dúctil para conducción de agua potable, alcantarillado e impulsión." },
  { slug: "marcos-y-tapas", name: "Marcos y tapas", description: "Marcos, tapas y rejillas de hierro dúctil para buzones, registros y sumideros." },
  ...PDF_CATEGORIES.filter((c) => c.slug !== "valvulas").map((c) => ({ slug: c.slug, name: c.name, description: null })),
];
CATEGORIES.forEach((c, i) => emit(
  `INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES (${q(uuid("category", c.slug))}, ${q(c.slug)}, ${q(c.name)}, ${q(c.description)}, ${i + 1}, true, ${NOW}, ${NOW})
ON CONFLICT (slug) DO NOTHING;`));
const categoryId = (slug) => `(SELECT id FROM product_categories WHERE slug = ${q(slug)})`;

// ---------- productos ----------
const products = new Map();
const add = (p) => {
  if (products.has(p.slug)) throw new Error(`slug duplicado: ${p.slug}`);
  products.set(p.slug, { variants: [], standards: [], homologations: [], pressure: [], specs: [], images: [], ...p });
};

// 1) Catálogo de diseño (tarjetas de /productos y búsqueda).
for (const p of CATALOG_PRODUCTS) {
  add({
    slug: p.id, sku: p.sku ?? null, category: p.category, family: p.family, name: p.name, summary: p.summary,
    material: p.material, joint: p.joint, dn: p.dn ?? null, pressure: p.pn ?? [], standards: p.standards,
    homologations: p.homologation, specs: p.specs.map(([label, value]) => ({ label, value })),
    images: [{ url: p.image, alt: p.imageAlt }], source: "Diseño del sitio (Stitch), pendiente de validación",
  });
}

// 2) Fichas con página propia (/productos/<categoría>/<slug>).
const FICHAS = [
  { category: "valvulas", slug: "valvula-compuerta-fundigsac", name: "Válvula de compuerta bridada FUNDIGSAC", description: "Válvula compuerta bridada de asiento resiliente, DN50 a DN600, PN10/PN16, diseño DIN 3352 F4, en hierro dúctil GGG50. Marca FUNDIGSAC.", family: "compuerta", joint: "bridada-f4", dn: [50, 600], pressure: [10, 16], material: "Hierro dúctil GGG-50" },
  { category: "valvulas", slug: "valvula-compuerta-fundigsac-garantia-extendida", name: "Válvula de compuerta bridada FUNDIGSAC (garantía extendida)", description: "Válvula de compuerta de cierre elástico con extremos bridados, DN40 a DN600, PN10/PN16, según EN 1074-2. Marca FUNDIGSAC con garantía extendida.", family: "compuerta", joint: "bridada", dn: [40, 600], pressure: [10, 16], material: "Hierro dúctil" },
];
for (const [slug, qp] of Object.entries(QUOTE_PRODUCTS)) {
  const family = { "valvula-compuerta": "compuerta", "valvula-mariposa": "mariposa", "tuberia-tyton": "tuberia", "marco-tapa-d400": "marco-tapa" }[slug];
  const category = qp.productHref.split("/")[2];
  add({
    slug, sku: qp.sku, category, family, name: qp.title, summary: qp.summary, material: qp.material,
    specs: qp.specs.map(([label, value]) => ({ label, value })), images: [{ url: qp.image.src, alt: qp.image.alt }],
    pressure: [...new Set(qp.specs.flatMap(([, v]) => [...v.matchAll(/PN\s?(\d{1,2})/g)].map((m) => Number(m[1]))))],
    source: "Diseño del sitio (ficha W06-W09), pendiente de validación",
  });
}
for (const f of FICHAS) {
  add({ slug: f.slug, sku: null, category: f.category, family: f.family, name: f.name, summary: f.description, material: f.material, joint: f.joint, dn: f.dn, pressure: f.pressure, source: "Ficha técnica PDF de FUNDIGSAC (public/docs)" });
}

// 3) Catálogo de los PDF de precios (solo nombres, códigos, medidas y datos técnicos impresos; sin precios).
for (const p of PDF_PRODUCTS) {
  const material = p.specs?.find(([k]) => /material/i.test(k))?.[1] ?? null;
  add({
    slug: p.slug, sku: null, category: p.category, family: p.category, name: p.name, summary: p.summary, material,
    specs: (p.specs ?? []).map(([label, value]) => ({ label, value })),
    images: p.image ? [{ url: p.image, alt: p.name, illustrative: Boolean(p.imageIllustrative) }] : [],
    variants: (p.variants ?? []).map((v) => ({ label: v.sdr ? `${v.label} · ${v.sdr}` : v.label, dn: dnOf(v.label), pn: pnOf(v.label) })),
    source: p.source,
  });
}

const familyLabel = (f) => FAMILY_LABEL[f] ?? f;
for (const p of products.values()) {
  const search = fold([p.name, p.sku ?? "", p.summary, p.material ?? "", familyLabel(p.family), p.category, ...p.standards, ...p.homologations, ...p.variants.map((v) => v.label)].join(" "));
  const id = uuid("product", p.slug);
  emit(`INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES (${q(id)}, ${categoryId(p.category)}, ${q(p.slug)}, ${q(p.sku)}, ${q(p.name)}, ${q(p.summary)}, ${q(p.family)},
  ${q(p.material)}, ${q(p.joint ?? null)}, ${p.dn ? p.dn[0] : "NULL"}, ${p.dn ? p.dn[1] : "NULL"}, ${arr(p.pressure, "smallint")},
  ${arr(p.standards)}, ${arr(p.homologations)}, ${json(p.specs)}, ${json(p.images)}, ${q(p.source)}, false, 'PUBLISHED',
  ${q(search)}, ${NOW}, ${NOW}, ${NOW})
ON CONFLICT (slug) DO NOTHING;`);
  p.variants.forEach((v, i) => emit(
    `INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES (${q(uuid("variant", `${p.slug}|${v.label}`))}, ${q(id)}, NULL, ${q(v.label)}, ${v.dn ?? "NULL"}, ${v.pn ?? "NULL"}, ${i + 1})
ON CONFLICT (product_id, label) DO NOTHING;`));
}

// ---------- sectores (página /industrias) ----------
const SECTORS = [
  { slug: "saneamiento", name: "Saneamiento", description: "Redes de agua potable y alcantarillado que requieren conducción, control y medición confiables.", products: ["tuberia-hierro-ductil-c40-k9-k7-thd", "valvula-compuerta-bridada-vbbh", "marco-y-tapa-pesada-2seg", "vcp-f4-01", "tub-ty-05", "mar-d4-07"] },
  { slug: "mineria", name: "Minería", description: "Conducción de agua y procesos en operaciones donde las líneas deben resistir uso exigente.", products: ["codo-hdpe-termofusion-90-sdr11", "acople-gran-rango-agr", "maquina-termofusion-serie-hdc", "vcp-knf-05"] },
  { slug: "agricultura", name: "Agricultura", description: "Sistemas de riego y distribución de agua en campo, con control de aire, presión y filtrado.", products: ["valvula-de-aire-roscada-vdar", "filtro-tipo-y-bridado-fyb", "tee-hdpe-termofusion-sdr11"] },
  { slug: "industria", name: "Industria", description: "Líneas de proceso y servicios de planta que necesitan bridas, juntas y válvulas con la clase correcta.", products: ["brida-iso-2351-pn16-biso", "junta-flexible-din-jfdin", "valvula-mariposa-wafer-timon-vmtwt"] },
];
SECTORS.forEach((s, i) => {
  emit(`INSERT INTO industry_sectors (id, slug, name, description, position)
VALUES (${q(uuid("sector", s.slug))}, ${q(s.slug)}, ${q(s.name)}, ${q(s.description)}, ${i + 1})
ON CONFLICT (slug) DO NOTHING;`);
  for (const slug of s.products.filter((x) => products.has(x))) {
    emit(`INSERT INTO product_sectors (product_id, sector_id) VALUES (${q(uuid("product", slug))}, ${q(uuid("sector", s.slug))}) ON CONFLICT DO NOTHING;`);
  }
});

// ---------- documentos técnicos ----------
const TYPE = { catalogos: "CATALOG", fichas: "DATASHEET", manuales: "MANUAL", planos: "DRAWING" };
const FAMILY_CATEGORY = { valvulas: "valvulas", tuberias: "tuberias", marcos: "marcos-y-tapas" };
const documents = [];
for (const r of RESOURCE_ITEMS) {
  documents.push({ slug: r.id, code: r.code, title: r.title, description: r.description, type: TYPE[r.category], families: r.families.map((f) => FAMILY_CATEGORY[f]), availability: "ON_REQUEST", products: [] });
}
const PDFS = [
  { file: "ficha-tecnica-valvula-compuerta-bridada.pdf", slug: "ficha-tecnica-valvula-compuerta-bridada", title: "Ficha técnica: válvula de compuerta bridada", products: ["valvula-compuerta", "valvula-compuerta-bridada"] },
  { file: "ficha-tecnica-valvula-compuerta-fundigsac.pdf", slug: "ficha-tecnica-valvula-compuerta-fundigsac", title: "Ficha técnica: válvula de compuerta bridada FUNDIGSAC", products: ["valvula-compuerta-fundigsac", "valvula-compuerta-fundigsac-garantia-extendida"] },
  { file: "ficha-tecnica-valvula-compuerta-fundigsac-ft-hd-002-2024.pdf", slug: "ficha-tecnica-valvula-compuerta-fundigsac-ft-hd-002-2024", code: "FT-HD-002-2024", title: "Ficha técnica FT-HD-002-2024: válvula de compuerta FUNDIGSAC", products: ["valvula-compuerta-fundigsac"] },
];
for (const pdf of PDFS) {
  const abs = path.join(frontend, "public", "docs", pdf.file);
  const bytes = readFileSync(abs);
  documents.push({ ...pdf, code: pdf.code ?? null, description: null, type: "DATASHEET", families: ["valvulas"], availability: "DOWNLOAD", url: `/docs/${pdf.file}`, size: statSync(abs).size, sha256: createHash("sha256").update(bytes).digest("hex") });
}
for (const d of documents) {
  const id = uuid("document", d.slug);
  emit(`INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES (${q(id)}, ${q(d.slug)}, ${q(d.code)}, ${q(d.title)}, ${q(d.description)}, ${q(d.type)}, ${arr(d.families)}, ${q(d.availability)},
  ${q(d.url ?? null)}, ${d.url ? "'application/pdf'" : "NULL"}, ${d.size ?? "NULL"}, ${q(d.sha256 ?? null)}, 'es', 'PUBLISHED', ${NOW}, ${NOW}, ${NOW})
ON CONFLICT (slug) DO NOTHING;`);
  for (const slug of d.products.filter((x) => products.has(x))) {
    emit(`INSERT INTO product_documents (product_id, document_id) VALUES (${q(uuid("product", slug))}, ${q(id)}) ON CONFLICT DO NOTHING;`);
  }
}

const header = `-- GENERADO por scripts/seed/generate-catalog-seed.mjs: no editar a mano.
-- Datos de demostración para desarrollo y pruebas (solo perfiles local y test). Provienen del sitio actual y quedan con
-- data_verified = false hasta que Ingeniería de FUNDIGSAC los valide. Sin precios ni existencias.
-- ${CATEGORIES.length} categorías, ${products.size} productos, ${[...products.values()].reduce((n, p) => n + p.variants.length, 0)} variantes, ${SECTORS.length} sectores, ${documents.length} documentos.
`;
const target = path.join(backend, "src/main/resources/db/seed/R__catalog_seed.sql");
writeFileSync(target, header + "\n" + out.join("\n\n") + "\n");
console.log(`OK ${path.relative(backend, target)}: ${products.size} productos, ${documents.length} documentos`);
