-- Catálogo técnico: categorías (familias), productos con atributos tipados para filtrar (DN, PN, unión, normas,
-- homologaciones), variantes, sectores y documentos técnicos versionados con checksum. Un producto solo es público en
-- estado PUBLISHED; nunca se borra (se archiva) para no romper cotizaciones ni historiales.

CREATE TABLE product_categories (
    id           uuid         PRIMARY KEY,
    slug         varchar(120) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    name         varchar(120) NOT NULL,
    description  varchar(500),
    position     integer      NOT NULL DEFAULT 0,
    visible      boolean      NOT NULL DEFAULT true,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL
);

CREATE TABLE industry_sectors (
    id           uuid         PRIMARY KEY,
    slug         varchar(120) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    name         varchar(120) NOT NULL,
    description  varchar(500),
    position     integer      NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id                uuid         PRIMARY KEY,
    category_id       uuid         NOT NULL REFERENCES product_categories (id),
    slug              varchar(120) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    sku               varchar(40)  UNIQUE CHECK (sku ~ '^[A-Z0-9][A-Z0-9.-]{1,39}$'),
    name              varchar(200) NOT NULL,
    summary           varchar(600) NOT NULL,
    description       text         CHECK (char_length(description) <= 8000),
    family            varchar(60)  NOT NULL,
    material          varchar(200),
    joint_type        varchar(20)  CHECK (joint_type IN ('bridada', 'bridada-f4', 'bridada-f5', 'ranurada', 'roscada',
                                                         'tyton', 'acerrojada', 'mecanica', 'termofusion', 'ninguna')),
    dn_min_mm         integer      CHECK (dn_min_mm > 0),
    dn_max_mm         integer,
    pressure_ratings  smallint[]   NOT NULL DEFAULT '{}',
    standards         text[]       NOT NULL DEFAULT '{}',
    homologations     text[]       NOT NULL DEFAULT '{}' CHECK (homologations <@ ARRAY['sedapal', 'otass', 'wras']),
    specs             jsonb        NOT NULL DEFAULT '[]' CHECK (jsonb_typeof(specs) = 'array'),
    images            jsonb        NOT NULL DEFAULT '[]' CHECK (jsonb_typeof(images) = 'array'),
    source_reference  varchar(300),
    data_verified     boolean      NOT NULL DEFAULT false,
    status            varchar(12)  NOT NULL CHECK (status IN ('DRAFT', 'IN_REVIEW', 'PUBLISHED', 'ARCHIVED')),
    review_note       varchar(500),
    search_text       text         NOT NULL DEFAULT '',
    search_vector     tsvector     GENERATED ALWAYS AS (to_tsvector('spanish'::regconfig, search_text)) STORED,
    created_by        uuid,
    submitted_by      uuid,
    submitted_at      timestamptz,
    published_by      uuid,
    published_at      timestamptz,
    created_at        timestamptz  NOT NULL,
    updated_at        timestamptz  NOT NULL,
    version           bigint       NOT NULL DEFAULT 0,
    CHECK ((dn_min_mm IS NULL) = (dn_max_mm IS NULL)),
    CHECK (dn_max_mm IS NULL OR dn_max_mm >= dn_min_mm),
    CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL)
);
CREATE INDEX products_category_status_idx ON products (category_id, status);
CREATE INDEX products_status_published_idx ON products (status, published_at DESC);
CREATE INDEX products_search_idx ON products USING gin (search_vector);
CREATE INDEX products_homologations_idx ON products USING gin (homologations);
CREATE INDEX products_pressure_idx ON products USING gin (pressure_ratings);
SELECT restrict_app_role('products', 'DELETE, TRUNCATE');

CREATE TABLE product_variants (
    id          uuid         PRIMARY KEY,
    product_id  uuid         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    sku         varchar(40)  UNIQUE CHECK (sku ~ '^[A-Z0-9][A-Z0-9.-]{1,39}$'),
    label       varchar(120) NOT NULL,
    dn_mm       integer      CHECK (dn_mm > 0),
    pn          smallint     CHECK (pn > 0),
    position    integer      NOT NULL,
    UNIQUE (product_id, label)
);
CREATE INDEX product_variants_dn_idx ON product_variants (dn_mm) WHERE dn_mm IS NOT NULL;

CREATE TABLE product_sectors (
    product_id  uuid NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    sector_id   uuid NOT NULL REFERENCES industry_sectors (id),
    PRIMARY KEY (product_id, sector_id)
);
CREATE INDEX product_sectors_sector_idx ON product_sectors (sector_id);

CREATE TABLE technical_documents (
    id            uuid         PRIMARY KEY,
    slug          varchar(120) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    code          varchar(60),
    title         varchar(200) NOT NULL,
    description   varchar(1000),
    doc_type      varchar(16)  NOT NULL CHECK (doc_type IN ('CATALOG', 'DATASHEET', 'MANUAL', 'DRAWING', 'CERTIFICATE', 'OTHER')),
    families      text[]       NOT NULL DEFAULT '{}',
    availability  varchar(12)  NOT NULL CHECK (availability IN ('DOWNLOAD', 'ON_REQUEST')),
    file_url      varchar(500) CHECK (file_url ~ '^(/[A-Za-z0-9._~/-]+|https://[A-Za-z0-9.-]+/[A-Za-z0-9._~/%-]*)$'),
    media_type    varchar(80),
    size_bytes    bigint       CHECK (size_bytes > 0),
    sha256        char(64)     CHECK (sha256 ~ '^[0-9a-f]{64}$'),
    revision      varchar(40),
    language      char(2)      NOT NULL DEFAULT 'es',
    status        varchar(12)  NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'WITHDRAWN')),
    published_at  timestamptz,
    created_at    timestamptz  NOT NULL,
    updated_at    timestamptz  NOT NULL,
    version       bigint       NOT NULL DEFAULT 0,
    -- Un documento descargable siempre tiene archivo, tipo, tamaño y checksum verificables.
    CHECK (availability = 'ON_REQUEST'
           OR (file_url IS NOT NULL AND media_type IS NOT NULL AND size_bytes IS NOT NULL AND sha256 IS NOT NULL))
);
CREATE INDEX technical_documents_status_idx ON technical_documents (status, doc_type);
SELECT restrict_app_role('technical_documents', 'DELETE, TRUNCATE');

CREATE TABLE product_documents (
    product_id   uuid NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    document_id  uuid NOT NULL REFERENCES technical_documents (id),
    PRIMARY KEY (product_id, document_id)
);
CREATE INDEX product_documents_document_idx ON product_documents (document_id);
