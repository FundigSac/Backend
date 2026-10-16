-- Carrito de cotización del cliente con cuenta (lista de productos a cotizar; sin precios).

CREATE TABLE quote_carts (
    owner_id    uuid        PRIMARY KEY,
    updated_at  timestamptz NOT NULL,
    version     bigint      NOT NULL DEFAULT 0
);

CREATE TABLE quote_cart_items (
    id             uuid         PRIMARY KEY,
    owner_id       uuid         NOT NULL REFERENCES quote_carts (owner_id) ON DELETE CASCADE,
    product_id     uuid         NOT NULL,
    product_slug   varchar(120) NOT NULL,
    product_name   varchar(200) NOT NULL,
    variant_label  varchar(120),
    quantity       integer      NOT NULL CHECK (quantity BETWEEN 1 AND 999),
    note           varchar(500),
    position       integer      NOT NULL,
    added_at       timestamptz  NOT NULL
);
CREATE UNIQUE INDEX quote_cart_items_line_uq ON quote_cart_items (owner_id, product_slug, coalesce(variant_label, ''));
