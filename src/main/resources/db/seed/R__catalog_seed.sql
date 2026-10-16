-- GENERADO por scripts/seed/generate-catalog-seed.mjs: no editar a mano.
-- Datos de demostración para desarrollo y pruebas (solo perfiles local y test). Provienen del sitio actual y quedan con
-- data_verified = false hasta que Ingeniería de FUNDIGSAC los valide. Sin precios ni existencias.
-- 8 categorías, 94 productos, 658 variantes, 4 sectores, 9 documentos.

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('9df15716-4105-5060-9de0-c3fa374d5a3c', 'valvulas', 'Válvulas', 'Válvulas de compuerta, mariposa, retención, regulación y aire para redes de agua y procesos.', 1, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('ab74cfd8-f65d-59b9-9d4e-f6340536f258', 'tuberias', 'Tuberías', 'Tuberías de hierro dúctil para conducción de agua potable, alcantarillado e impulsión.', 2, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('fb7582c7-b9b3-5b18-babf-26ab7dbebd57', 'marcos-y-tapas', 'Marcos y tapas', 'Marcos, tapas y rejillas de hierro dúctil para buzones, registros y sumideros.', 3, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('9806ff07-cca7-57b2-b926-d24b7e411290', 'hdpe', 'Accesorios HDPE', NULL, 4, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('e1478665-24e3-5f8a-b060-ddefe2d7084e', 'equipos', 'Equipos de termofusión', NULL, 5, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('c7fc9b38-1b3f-5d78-86bd-611ffd07d71e', 'acoples', 'Acoples y uniones', NULL, 6, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('6fa362d4-97ea-566d-92b9-9775104d2ee4', 'hierro-ductil', 'Hierro dúctil', NULL, 7, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_categories (id, slug, name, description, position, visible, created_at, updated_at)
VALUES ('d654b6fb-18d9-5092-8cf6-48a9ad80a758', 'medidores', 'Medidores', NULL, 8, true, TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('a997e1b5-45ec-5bf7-82e0-b090afad0143', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vcp-f4-01', 'VCP-F4-01', 'Válvula Compuerta Bridada Vástago No Ascendente', 'Cuerpo corto en hierro dúctil GGG-50, vástago no ascendente en acero inoxidable AISI 420 y cuña vulcanizada en EPDM sanitario.', 'compuerta',
  'GGG-50 (EN-GJS-500)', 'bridada-f4', 50, 300, ARRAY[16, 25]::smallint[],
  ARRAY['EN 1171', 'EN 1074', 'NTP ISO 7259', 'DIN 3202 F4']::text[], ARRAY['sedapal', 'otass']::text[], '[{"label":"Rango de Diámetros","value":"DN 50 – DN 300"},{"label":"Presión Nominal","value":"PN 16 / PN 25"},{"label":"Material Cuerpo","value":"GGG-50 (EN-GJS-500)"},{"label":"Recubrimiento","value":"Epoxi 250 µm RAL 5005"}]'::jsonb, '[{"url":"/media/valvula-hd/v-01.webp","alt":"Válvula de compuerta bridada de hierro dúctil con recubrimiento epóxico azul"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula compuerta bridada vastago no ascendente vcp-f4-01 cuerpo corto en hierro ductil ggg-50, vastago no ascendente en acero inoxidable aisi 420 y cuna vulcanizada en epdm sanitario. ggg-50 (en-gjs-500) valvulas de compuerta valvulas en 1171 en 1074 ntp iso 7259 din 3202 f4 sedapal otass', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('cba03c59-f7aa-582c-987e-5f36ee0bea4c', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vcp-f5-02', 'VCP-F5-02', 'Válvula Compuerta Brida Serie F5 Larga PN 16 / PN 25', 'Longitud entre caras ampliada para reposición en matrices primarias. Doble retén O-Ring y cuña con guía autolubricada.', 'compuerta',
  'Hierro dúctil GGG-50', 'bridada-f5', 50, 600, ARRAY[16, 25]::smallint[],
  ARRAY['DIN 3202 F5', 'EN 1171']::text[], ARRAY['sedapal']::text[], '[{"label":"Rango de Diámetros","value":"DN 50 – DN 600"},{"label":"Presión Nominal","value":"PN 16 / PN 25"},{"label":"Longitud entre caras","value":"Serie F5 larga"},{"label":"Sellado de vástago","value":"Doble retén O-Ring"}]'::jsonb, '[{"url":"/media/valvula-hd/v-02.webp","alt":"Válvula de compuerta bridada de serie larga F5"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula compuerta brida serie f5 larga pn 16 / pn 25 vcp-f5-02 longitud entre caras ampliada para reposicion en matrices primarias. doble reten o-ring y cuna con guia autolubricada. hierro ductil ggg-50 valvulas de compuerta valvulas din 3202 f5 en 1171 sedapal', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('d3d3fba3-7b99-59c4-9d33-fc0aa010fefe', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vcp-br-03', 'VCP-BR-03', 'Válvula con Asiento Elástico y Bonete Roscado', 'Diseño estanco sin ranura inferior de retención de sedimentos. Paso integral 100% libre de pérdidas de carga hidráulica.', 'compuerta',
  'Hierro dúctil', 'bridada-f4', 80, 200, ARRAY[16]::smallint[],
  ARRAY['EN 1074-2']::text[], ARRAY['sedapal', 'otass']::text[], '[{"label":"Rango de Diámetros","value":"DN 80 – DN 200"},{"label":"Presión Nominal","value":"PN 16"},{"label":"Diseño","value":"Estanco sin ranura inferior"},{"label":"Bonete","value":"Roscado"}]'::jsonb, '[{"url":"/media/valvula-hd/v-03.webp","alt":"Válvula de compuerta de asiento elástico con bonete roscado"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula con asiento elastico y bonete roscado vcp-br-03 diseno estanco sin ranura inferior de retencion de sedimentos. paso integral 100% libre de perdidas de carga hidraulica. hierro ductil valvulas de compuerta valvulas en 1074-2 sedapal otass', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('cd03bb62-5f8c-57d9-bc4c-fe66d63e97fb', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vcp-dom-04', 'VCP-DOM-04', 'Válvula Compuerta Roscada para Acometida', 'Especializada para cajas de medidor en vereda y arranques domiciliares. Cuerpo compacto de alta resistencia al aplastamiento vehicular.', 'compuerta',
  'Hierro dúctil', 'roscada', 25, 50, ARRAY[16]::smallint[],
  ARRAY['ISO 7/1 BSP']::text[], ARRAY['sedapal', 'otass']::text[], '[{"label":"Rango de Diámetros","value":"DN 25 – DN 50"},{"label":"Presión Nominal","value":"PN 16"},{"label":"Conexión","value":"Roscada ISO 7/1 BSP"},{"label":"Aplicación","value":"Acometida domiciliaria"}]'::jsonb, '[{"url":"/media/valvula-hd/v-04.webp","alt":"Válvula de compuerta roscada para acometida domiciliaria"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula compuerta roscada para acometida vcp-dom-04 especializada para cajas de medidor en vereda y arranques domiciliares. cuerpo compacto de alta resistencia al aplastamiento vehicular. hierro ductil valvulas de compuerta valvulas iso 7/1 bsp sedapal otass', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('5c0e4a07-364f-590f-9ea1-7d7e281943de', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vcp-knf-05', 'VCP-KNF-05', 'Válvula Compuerta Cuchilla Guillotina', 'Pala de corte en acero inoxidable AISI 304/316 para fluidos con sólidos en suspensión, pulpas mineras, relaves y aguas residuales.', 'compuerta',
  'Hierro dúctil / pala AISI 304-316', 'ranurada', 100, 500, ARRAY[10, 16]::smallint[],
  ARRAY['MSS SP-81']::text[], '{}'::text[], '[{"label":"Rango de Diámetros","value":"DN 100 – DN 500"},{"label":"Presión Nominal","value":"PN 10 / PN 16"},{"label":"Pala de corte","value":"Acero inox AISI 304/316"},{"label":"Aplicación","value":"Pulpas, relaves y lodos"}]'::jsonb, '[{"url":"/media/valvula-hd/v-05.webp","alt":"Válvula de compuerta tipo cuchilla guillotina para minería y lodos"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula compuerta cuchilla guillotina vcp-knf-05 pala de corte en acero inoxidable aisi 304/316 para fluidos con solidos en suspension, pulpas mineras, relaves y aguas residuales. hierro ductil / pala aisi 304-316 valvulas de compuerta valvulas mss sp-81', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ee93f956-aba9-52ae-9898-dcf4643469ca', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vcp-tel-06', 'VCP-TEL-06', 'Válvula con Extensión Telescópica y Cuadrante', 'Para enterramiento directo sin cámara de válvulas. Vástago telescópico regulable (1.20m a 2.50m) y dado de accionamiento superficial.', 'compuerta',
  'Hierro dúctil', 'bridada-f4', 80, 400, ARRAY[16]::smallint[],
  ARRAY['NTP 350.085']::text[], ARRAY['sedapal']::text[], '[{"label":"Rango de Diámetros","value":"DN 80 – DN 400"},{"label":"Presión Nominal","value":"PN 16"},{"label":"Extensión","value":"Telescópica 1.20 a 2.50 m"},{"label":"Instalación","value":"Enterramiento directo"}]'::jsonb, '[{"url":"/media/valvula-hd/v-01.webp","alt":"Válvula de compuerta con extensión telescópica para enterramiento directo"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula con extension telescopica y cuadrante vcp-tel-06 para enterramiento directo sin camara de valvulas. vastago telescopico regulable (1.20m a 2.50m) y dado de accionamiento superficial. hierro ductil valvulas de compuerta valvulas ntp 350.085 sedapal', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('705c51de-08ad-51b3-9d16-9cd811f6ea15', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vma-wf-02', 'VMA-WF-02', 'Válvula Mariposa Excéntrica con Reductor Manual', 'Válvula mariposa excéntrica bridada de hierro dúctil con caja reductora IP68 y asiento EPDM vulcanizado.', 'mariposa',
  'Hierro dúctil', 'bridada', 80, 600, ARRAY[16, 25]::smallint[],
  ARRAY['ISO 5752', 'EN 593']::text[], '{}'::text[], '[{"label":"Rango de Diámetros","value":"DN 80 – DN 600"},{"label":"Presión Nominal","value":"PN 16 (Opción PN 25)"},{"label":"Accionamiento","value":"Caja reductora IP68"},{"label":"Asiento de Cierre","value":"EPDM vulcanizado / Inox"}]'::jsonb, '[{"url":"/images/stitch/8f2df4ffc2.jpg","alt":"Válvula mariposa excéntrica con reductor manual y recubrimiento azul"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula mariposa excentrica con reductor manual vma-wf-02 valvula mariposa excentrica bridada de hierro ductil con caja reductora ip68 y asiento epdm vulcanizado. hierro ductil valvulas mariposa valvulas iso 5752 en 593', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('3cca8b38-7ec8-5e10-aec5-815cd19a64d9', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vma-wafer', NULL, 'Válvula Mariposa Tipo Wafer', 'Diseño de montaje entre bridas con reductor de tornillo sinfín de alto torque. Disco en fundición dúctil cromada y asiento vulcanizado en EPDM apto para agua potable.', 'mariposa',
  'Hierro dúctil, disco cromado', 'bridada', 50, 1200, ARRAY[10, 16]::smallint[],
  ARRAY['EN 593', 'ISO 5752']::text[], '{}'::text[], '[{"label":"Rango de Diámetros","value":"DN 50 – DN 1200"},{"label":"Presión Nominal","value":"PN 10 / PN 16"},{"label":"Instalación","value":"Wafer EN 1092-2 / ANSI"},{"label":"Disco / Eje","value":"Dúctil cromado / SS 416"}]'::jsonb, '[{"url":"/images/stitch/c2800fd46b.jpg","alt":"Válvula mariposa tipo wafer con reductor de tornillo sinfín"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula mariposa tipo wafer diseno de montaje entre bridas con reductor de tornillo sinfin de alto torque. disco en fundicion ductil cromada y asiento vulcanizado en epdm apto para agua potable. hierro ductil, disco cromado valvulas mariposa valvulas en 593 iso 5752', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('34556905-73b6-5921-ae99-d0a8a88d6cd9', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vch-sw-03', 'VCH-SW-03', 'Válvula Check Swing Antirretorno Bridada', 'Disco basculante totalmente engomado con cierre silencioso. Tapa de registro superior que permite inspección y mantenimiento sin desacoplar de la tubería matriz.', 'retencion',
  'Fundición dúctil + EPDM', 'bridada', 50, 400, ARRAY[16, 25]::smallint[],
  ARRAY['EN 16767', 'EN 1074-3', 'DIN 3202']::text[], '{}'::text[], '[{"label":"Rango de Diámetros","value":"DN 50 – DN 400"},{"label":"Presión de Trabajo","value":"PN 16 / PN 25"},{"label":"Disco / Obturador","value":"Fundición Dúctil + EPDM"},{"label":"Tipo Contrapeso","value":"Brazo amortiguador opt."}]'::jsonb, '[{"url":"/images/stitch/c28c3c28eb-limpia.jpg","alt":"Válvula check swing bridada con brazo y contrapeso"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula check swing antirretorno bridada vch-sw-03 disco basculante totalmente engomado con cierre silencioso. tapa de registro superior que permite inspeccion y mantenimiento sin desacoplar de la tuberia matriz. fundicion ductil + epdm valvulas de retencion valvulas en 16767 en 1074-3 din 3202', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('afa54b98-c40d-5caa-87ee-5fbabdd0e511', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vch-dual', NULL, 'Válvula Check Flex Doble Clapeta', 'Retención de respuesta ultrarrápida con resorte central en acero inoxidable AISI 316. Mínima pérdida de carga hidrodinámica y prevención de golpe de ariete.', 'retencion',
  'Hierro dúctil GGG-50, resorte AISI 316', 'bridada', 50, 800, ARRAY[16, 25]::smallint[],
  ARRAY['API 594', 'EN 1074-3']::text[], '{}'::text[], '[{"label":"Rango de Diámetros","value":"DN 50 – DN 800"},{"label":"Presión de Trabajo","value":"PN 16 / PN 25"},{"label":"Instalación","value":"Wafer EN 1092-2 / ANSI B16.1"},{"label":"Resorte / Eje","value":"Inox AISI 316 / Bronce"}]'::jsonb, '[{"url":"/images/stitch/4ffdfc51b2-limpia.jpg","alt":"Válvula check wafer de doble clapeta"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula check flex doble clapeta retencion de respuesta ultrarrapida con resorte central en acero inoxidable aisi 316. minima perdida de carga hidrodinamica y prevencion de golpe de ariete. hierro ductil ggg-50, resorte aisi 316 valvulas de retencion valvulas api 594 en 1074-3', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('0453d82e-9760-5a06-937d-2186793951e7', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'vrp-pl-04', 'VRP-PL-04', 'Válvula Reductora de Presión Piloto Hidráulico', 'Válvula de diafragma guiada hidrostáticamente que reduce una presión aguas arriba fluctuante a una presión aguas abajo constante preestablecida.', 'regulacion',
  'Hierro dúctil GGG-50, piloto bronce / inox 316', 'bridada', 50, 300, ARRAY[25]::smallint[],
  ARRAY['AWWA C530', 'EN 1074-5']::text[], '{}'::text[], '[{"label":"Rango de Diámetros","value":"DN 50 – DN 300"},{"label":"Presión Nominal","value":"PN 25 (Hasta 25 bar)"},{"label":"Circuito Piloto","value":"Bronce / Acero Inox 316"},{"label":"Ajuste de Salida","value":"1.0 a 16.0 bar regulable"}]'::jsonb, '[{"url":"/images/stitch/f15c5a8711.jpg","alt":"Válvula reductora de presión con circuito piloto y manómetros"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'valvula reductora de presion piloto hidraulico vrp-pl-04 valvula de diafragma guiada hidrostaticamente que reduce una presion aguas arriba fluctuante a una presion aguas abajo constante preestablecida. hierro ductil ggg-50, piloto bronce / inox 316 regulacion hidraulica y alivio valvulas awwa c530 en 1074-5', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('2a6bebd4-b4c2-5363-a167-a8d9490e9b78', (SELECT id FROM product_categories WHERE slug = 'tuberias'), 'tub-ty-05', 'TUB-TY-05', 'Tubería Hierro Dúctil Campana Tyton', 'Tubería de hierro dúctil con junta flexible Tyton para conducción principal de redes de agua potable.', 'tuberia',
  'Hierro dúctil, revestimiento interior de mortero de cemento', 'tyton', 80, 1000, ARRAY[40]::smallint[],
  ARRAY['ISO 2531', 'EN 545']::text[], ARRAY['wras']::text[], '[{"label":"Rango Nominal","value":"DN 80 – DN 1000"},{"label":"Clase de Presión","value":"Clase C40 / K9"},{"label":"Revestimiento Int.","value":"Mortero cemento Portland"},{"label":"Longitud Útil","value":"5.80 / 6.00 metros"}]'::jsonb, '[{"url":"/images/stitch/c49bbc0276.jpg","alt":"Tuberías de hierro dúctil con junta Tyton apiladas en patio"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'tuberia hierro ductil campana tyton tub-ty-05 tuberia de hierro ductil con junta flexible tyton para conduccion principal de redes de agua potable. hierro ductil, revestimiento interior de mortero de cemento tuberias de hierro ductil tuberias iso 2531 en 545 wras', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ab7c7e3d-885e-577a-a4f4-b5d9ef36a0e6', (SELECT id FROM product_categories WHERE slug = 'tuberias'), 'tub-ac-06', 'TUB-AC-06', 'Tubería Acerrojada Autoportante Vi', 'Incorpora insertos de acero inoxidable templado dentro de la junta de elastómero. Elimina la necesidad de dados de concreto para empujes hidráulicos.', 'tuberia',
  'Hierro dúctil, junta con insertos de acero inoxidable', 'acerrojada', 100, 600, ARRAY[16, 25]::smallint[],
  ARRAY['EN 545', 'ISO 10804']::text[], '{}'::text[], '[{"label":"Rango Nominal","value":"DN 100 – DN 600"},{"label":"Presión Admisible","value":"PN 16 / PN 25 sin dados"},{"label":"Deflexión Angular","value":"Hasta 3° por enchufe"},{"label":"Aplicación","value":"Terrenos inestables / Sismo"}]'::jsonb, '[{"url":"/images/stitch/8a067ff5f6-limpia.jpg","alt":"Junta acerrojada de tubería de hierro dúctil con segmentos de bloqueo"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'tuberia acerrojada autoportante vi tub-ac-06 incorpora insertos de acero inoxidable templado dentro de la junta de elastomero. elimina la necesidad de dados de concreto para empujes hidraulicos. hierro ductil, junta con insertos de acero inoxidable tuberias de hierro ductil tuberias en 545 iso 10804', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('b62f58d3-882b-5a4c-a391-ec77db8ce844', (SELECT id FROM product_categories WHERE slug = 'tuberias'), 'tub-k9', NULL, 'Tubería Matriz Clase K9 de Gran Diámetro', 'Dimensionada con factor de espesor K9 para soportar altas cargas vivas de tránsito pesado, terraplenes profundos y sobrepresiones por golpe de ariete en estaciones de bombeo primario.', 'tuberia',
  'Hierro dúctil, clase K9', 'tyton', 700, 1200, '{}'::smallint[],
  ARRAY['ISO 2531', 'EN 545']::text[], '{}'::text[], '[{"label":"Rango Nominal","value":"DN 700 – DN 1200"},{"label":"Clase de espesor","value":"K9"},{"label":"Módulo de elasticidad","value":"170,000 MPa"},{"label":"Aplicación","value":"Líneas de impulsión y túneles"}]'::jsonb, '[{"url":"/images/stitch/92121f0ce3.jpg","alt":"Tuberías de hierro dúctil de gran diámetro apiladas en patio de obra"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'tuberia matriz clase k9 de gran diametro dimensionada con factor de espesor k9 para soportar altas cargas vivas de transito pesado, terraplenes profundos y sobrepresiones por golpe de ariete en estaciones de bombeo primario. hierro ductil, clase k9 tuberias de hierro ductil tuberias iso 2531 en 545', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c0f3f486-1210-5cfb-91ce-dc81524eda00', (SELECT id FROM product_categories WHERE slug = 'tuberias'), 'tub-san', NULL, 'Tubería para Alcantarillado e Impulsión', 'Especificada bajo norma europea EN 598 con revestimiento interior de cemento aluminoso (CAC) para colectores por gravedad y presión de aguas servidas.', 'tuberia',
  'Hierro dúctil, revestimiento interior de cemento aluminoso', 'tyton', 100, 1000, '{}'::smallint[],
  ARRAY['EN 598']::text[], '{}'::text[], '[{"label":"Rango Nominal","value":"DN 100 – DN 1000"},{"label":"Norma","value":"EN 598 (saneamiento)"},{"label":"Revestimiento Int.","value":"Cemento aluminoso (CAC)"},{"label":"Identificación exterior","value":"Epoxi marrón rojizo"}]'::jsonb, '[{"url":"/images/stitch/87e8f763c6.jpg","alt":"Tubería de hierro dúctil para saneamiento con revestimiento de cemento aluminoso"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'tuberia para alcantarillado e impulsion especificada bajo norma europea en 598 con revestimiento interior de cemento aluminoso (cac) para colectores por gravedad y presion de aguas servidas. hierro ductil, revestimiento interior de cemento aluminoso tuberias de hierro ductil tuberias en 598', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('51b16ed9-0d23-55f1-8830-a9b828e6dabf', (SELECT id FROM product_categories WHERE slug = 'marcos-y-tapas'), 'mar-d4-07', 'MAR-D4-07', 'Marco y Tapa Articulada D400 Ø 600 mm', 'Sistema estándar para buzones de alcantarillado: junta perimetral insonorizante y cerrojo elástico de seguridad.', 'marco-tapa',
  'Hierro dúctil GGG-50 / GS 500-7', 'ninguna', NULL, NULL, '{}'::smallint[],
  ARRAY['EN 124 D400', 'NTP 350.085', 'NTP-EN 124']::text[], '{}'::text[], '[{"label":"Capacidad de Carga","value":"400 kN (40 Toneladas)"},{"label":"Apertura Segura","value":"Bisagra 110° con bloqueo"},{"label":"Paso Libre","value":"Ø 600 mm circular"},{"label":"Amortiguación","value":"Junta EPDM perimetral"}]'::jsonb, '[{"url":"/images/stitch/d7313151e5-limpia.jpg","alt":"Marco y tapa circular articulada de hierro dúctil clase D400"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'marco y tapa articulada d400 ø 600 mm mar-d4-07 sistema estandar para buzones de alcantarillado: junta perimetral insonorizante y cerrojo elastico de seguridad. hierro ductil ggg-50 / gs 500-7 marcos y tapas de calzada marcos-y-tapas en 124 d400 ntp 350.085 ntp-en 124', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('54d21194-c293-582b-adbe-ce139e945675', (SELECT id FROM product_categories WHERE slug = 'marcos-y-tapas'), 'mar-sq-800', NULL, 'Conjunto Marco y Tapa Cuadrada 800×800 mm D400', 'Dimensionada para cámaras de inspección y cajas de válvulas enterradas. Brida perimetral ancha para anclaje sobre losa de concreto armado.', 'marco-tapa',
  'Hierro dúctil, pintura asfáltica bituminosa', 'ninguna', NULL, NULL, '{}'::smallint[],
  ARRAY['EN 124 D400', 'NTP-EN 124']::text[], '{}'::text[], '[{"label":"Paso Libre","value":"800 × 800 mm"},{"label":"Carga de Ensayo","value":"400 kN (NTP-EN 124)"},{"label":"Acabado","value":"Pintura asfáltica bituminosa"},{"label":"Aplicación","value":"Cámaras y cajas de válvulas"}]'::jsonb, '[{"url":"/images/stitch/4a8f370f88.jpg","alt":"Marco y tapa cuadrada de hierro dúctil de 800 por 800 mm clase D400"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'conjunto marco y tapa cuadrada 800×800 mm d400 dimensionada para camaras de inspeccion y cajas de valvulas enterradas. brida perimetral ancha para anclaje sobre losa de concreto armado. hierro ductil, pintura asfaltica bituminosa marcos y tapas de calzada marcos-y-tapas en 124 d400 ntp-en 124', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('227362ff-2688-5f04-b266-daef02774563', (SELECT id FROM product_categories WHERE slug = 'marcos-y-tapas'), 'mar-c250-estanca', NULL, 'Tapa de Registro Hermética Estanco C250', 'Pernos de acero inoxidable y junta tórica de neopreno continua. Previene la emanación de gases sulfhídricos (H₂S), olores cloacales y el ingreso de aguas superficiales.', 'marco-tapa',
  'Hierro dúctil, pernos AISI 316', 'ninguna', NULL, NULL, '{}'::smallint[],
  ARRAY['EN 124 C250', 'NTP-EN 124']::text[], '{}'::text[], '[{"label":"Hermeticidad","value":"Líquidos y gases (1.0 bar)"},{"label":"Fijación","value":"4 a 6 pernos AISI 316"},{"label":"Clase de carga","value":"C 250 (250 kN)"},{"label":"Aplicación","value":"PTAR y galerías subterráneas"}]'::jsonb, '[{"url":"/images/stitch/dd14ede5c2.jpg","alt":"Tapa de registro hermética de hierro dúctil clase C250 con pernos"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'tapa de registro hermetica estanco c250 pernos de acero inoxidable y junta torica de neopreno continua. previene la emanacion de gases sulfhidricos (h₂s), olores cloacales y el ingreso de aguas superficiales. hierro ductil, pernos aisi 316 marcos y tapas de calzada marcos-y-tapas en 124 c250 ntp-en 124', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c523ee26-044d-5b07-b20a-8ef28e9317f3', (SELECT id FROM product_categories WHERE slug = 'marcos-y-tapas'), 'rej-ab-08', 'REJ-AB-08', 'Rejilla de Sumidero Calzada Abatible', 'Perfil hidráulico con ranuras orientadas a favor de la pendiente de cuneta y bisagra antivandálica para mantenimiento y descolmatación.', 'rejilla',
  'Hierro dúctil, pintura bituminosa negra', 'ninguna', NULL, NULL, '{}'::smallint[],
  ARRAY['EN 124 D400', 'SEDAPAL AP-02']::text[], ARRAY['sedapal']::text[], '[{"label":"Formato Geométrico","value":"500 x 400 mm exterior"},{"label":"Área de Absorción","value":"880 cm² superficie libre"},{"label":"Sistema de Cierre","value":"Abatimiento antirrobo"},{"label":"Normativa","value":"Tráfico rodado pesado"}]'::jsonb, '[{"url":"/images/stitch/039f77948c-recorte.jpg","alt":"Rejilla de sumidero abatible de hierro dúctil para calzada"}]'::jsonb, 'Diseño del sitio (Stitch), pendiente de validación', false, 'PUBLISHED',
  'rejilla de sumidero calzada abatible rej-ab-08 perfil hidraulico con ranuras orientadas a favor de la pendiente de cuneta y bisagra antivandalica para mantenimiento y descolmatacion. hierro ductil, pintura bituminosa negra rejillas de sumidero marcos-y-tapas en 124 d400 sedapal ap-02 sedapal', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('b18ae21a-8aeb-5cde-9e63-ec7d16925d8b', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-compuerta', 'VCP-F4-150', 'Válvula de Compuerta Bridada con Asiento Elástico F4 PN 16', 'Cierre estanco bidireccional mediante cuña vulcanizada y paso recto total sin retención de sedimentos.', 'compuerta',
  'Hierro Dúctil GGG-50 / EN-GJS-500-7', NULL, NULL, NULL, ARRAY[16]::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Diámetro Nominal (DN)","value":"DN 150 (6\")"},{"label":"Presión de Trabajo (PN)","value":"PN 16 bar"},{"label":"Longitud entre caras","value":"DIN 3202 F4 (210 mm)"},{"label":"Perforación de Bridas","value":"ISO 7005-2 / EN 1092-2"},{"label":"Recubrimiento Sanitario","value":"Epoxi FBE Azul ≥ 250 µm"},{"label":"Elastómero de Cuña","value":"EPDM grado potable (WRAS)"}]'::jsonb, '[{"url":"/media/valvula-hd/v-01.webp","alt":"Válvula de compuerta de hierro dúctil DN 150 PN 16 con recubrimiento epóxico azul y volante"}]'::jsonb, 'Diseño del sitio (ficha W06-W09), pendiente de validación', false, 'PUBLISHED',
  'valvula de compuerta bridada con asiento elastico f4 pn 16 vcp-f4-150 cierre estanco bidireccional mediante cuna vulcanizada y paso recto total sin retencion de sedimentos. hierro ductil ggg-50 / en-gjs-500-7 valvulas de compuerta valvulas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('adbd21a4-9ac8-5df8-892c-51275a01a2aa', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa', 'VMA-EX-DN200', 'Válvula Mariposa Excéntrica con Caja Reductora Manual', 'Doble excentricidad de bajo torque hidrodinámico, disco perfilado de mínima pérdida de carga y anillo de asiento sustituible.', 'mariposa',
  'Hierro Dúctil · Disco AISI 316', NULL, NULL, NULL, ARRAY[10, 16, 25]::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Diámetro Nominal (DN)","value":"DN 200 (8\")"},{"label":"Presión de Trabajo (PN)","value":"PN 10 / PN 16 / PN 25"},{"label":"Longitud entre caras","value":"ISO 5752 Serie 20"},{"label":"Perforación de Bridas","value":"EN 1092-2"},{"label":"Disco","value":"Acero inoxidable AISI 316"},{"label":"Accionamiento","value":"Caja reductora IP68 (brida ISO 5211)"}]'::jsonb, '[{"url":"/images/stitch/85ae6c87ca.jpg","alt":"Válvula mariposa doble excéntrica de hierro dúctil con caja reductora manual y recubrimiento epóxico azul"}]'::jsonb, 'Diseño del sitio (ficha W06-W09), pendiente de validación', false, 'PUBLISHED',
  'valvula mariposa excentrica con caja reductora manual vma-ex-dn200 doble excentricidad de bajo torque hidrodinamico, disco perfilado de minima perdida de carga y anillo de asiento sustituible. hierro ductil · disco aisi 316 valvulas mariposa valvulas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('a3197bca-8028-5576-98c1-d97423ceb531', (SELECT id FROM product_categories WHERE slug = 'tuberias'), 'tuberia-tyton', 'TUB-TYT-C40', 'Tubería de Hierro Dúctil Junta Flexible Tyton Clase C40', 'Tubos centrifugados de 6.00 m de longitud útil con junta de empuje flexible que absorbe deflexiones y asentamientos del terreno.', 'tuberia',
  'Hierro Dúctil Centrifugado · Clase C40', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Diámetro Nominal (DN)","value":"DN 200 mm"},{"label":"Clase de presión","value":"C40 / K9"},{"label":"Longitud útil","value":"6.00 m (ISO 2531)"},{"label":"Tipo de junta","value":"Flexible Tyton (elastómero EPDM)"},{"label":"Revestimiento exterior","value":"Cinc 200 g/m² + capa bituminosa"},{"label":"Prueba hidrostática","value":"100% de los tubos en fábrica"}]'::jsonb, '[{"url":"/images/stitch/b018bc2b92-limpia.jpg","alt":"Corte técnico de un tubo de hierro dúctil con campana de junta Tyton y revestimiento interior de mortero"}]'::jsonb, 'Diseño del sitio (ficha W06-W09), pendiente de validación', false, 'PUBLISHED',
  'tuberia de hierro ductil junta flexible tyton clase c40 tub-tyt-c40 tubos centrifugados de 6.00 m de longitud util con junta de empuje flexible que absorbe deflexiones y asentamientos del terreno. hierro ductil centrifugado · clase c40 tuberias de hierro ductil tuberias', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('8a625fc0-0912-53cf-b24b-7aa163e2d78f', (SELECT id FROM product_categories WHERE slug = 'marcos-y-tapas'), 'marco-tapa-d400', 'MAR-D4-600', 'Marco y Tapa Articulada Circular Ø 600 mm Clase D400', 'Dispositivo de cubrimiento y cierre para tráfico pesado con bisagra, bloqueo de seguridad y junta antirruido.', 'marco-tapa',
  'Hierro Dúctil ASTM A536 / GGG-50', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Diámetro de paso","value":"Ø 600 mm (DN 600)"},{"label":"Clase de carga","value":"D400 (400 kN)"},{"label":"Norma","value":"EN 124"},{"label":"Material","value":"Hierro dúctil ASTM A536 / GGG-50"},{"label":"Articulación","value":"Bisagra con bloqueo a 90°"},{"label":"Junta","value":"Perfil de elastómero antirruido"}]'::jsonb, '[{"url":"/images/stitch/245160faac.jpg","alt":"Marco y tapa articulada circular de hierro dúctil clase D400 para calzada"}]'::jsonb, 'Diseño del sitio (ficha W06-W09), pendiente de validación', false, 'PUBLISHED',
  'marco y tapa articulada circular ø 600 mm clase d400 mar-d4-600 dispositivo de cubrimiento y cierre para trafico pesado con bisagra, bloqueo de seguridad y junta antirruido. hierro ductil astm a536 / ggg-50 marcos y tapas de calzada marcos-y-tapas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ccd28b46-175a-5677-acb6-f4d1ac5085f7', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-compuerta-fundigsac', NULL, 'Válvula de compuerta bridada FUNDIGSAC', 'Válvula compuerta bridada de asiento resiliente, DN50 a DN600, PN10/PN16, diseño DIN 3352 F4, en hierro dúctil GGG50. Marca FUNDIGSAC.', 'compuerta',
  'Hierro dúctil GGG-50', 'bridada-f4', 50, 600, ARRAY[10, 16]::smallint[],
  '{}'::text[], '{}'::text[], '[]'::jsonb, '[]'::jsonb, 'Ficha técnica PDF de FUNDIGSAC (public/docs)', false, 'PUBLISHED',
  'valvula de compuerta bridada fundigsac valvula compuerta bridada de asiento resiliente, dn50 a dn600, pn10/pn16, diseno din 3352 f4, en hierro ductil ggg50. marca fundigsac. hierro ductil ggg-50 valvulas de compuerta valvulas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('84a9de30-bc4e-54a4-9f37-17a1ea546156', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-compuerta-fundigsac-garantia-extendida', NULL, 'Válvula de compuerta bridada FUNDIGSAC (garantía extendida)', 'Válvula de compuerta de cierre elástico con extremos bridados, DN40 a DN600, PN10/PN16, según EN 1074-2. Marca FUNDIGSAC con garantía extendida.', 'compuerta',
  'Hierro dúctil', 'bridada', 40, 600, ARRAY[10, 16]::smallint[],
  '{}'::text[], '{}'::text[], '[]'::jsonb, '[]'::jsonb, 'Ficha técnica PDF de FUNDIGSAC (public/docs)', false, 'PUBLISHED',
  'valvula de compuerta bridada fundigsac (garantia extendida) valvula de compuerta de cierre elastico con extremos bridados, dn40 a dn600, pn10/pn16, segun en 1074-2. marca fundigsac con garantia extendida. hierro ductil valvulas de compuerta valvulas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('797668df-4a29-5287-8e59-048e2ff5c3f9', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-check-flex', NULL, 'Válvula check flex', 'Válvula de retención para redes de conducción.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VCFLEX"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/check-flex.webp","alt":"Válvula check flex","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula check flex valvula de retencion para redes de conduccion. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('24f8413f-aade-53bb-8d1c-3c86a2923de8', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8f6d42e0-e686-5f6b-8e8a-956802fd5aba', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0bbf5f37-0ea8-59fa-9a1c-7df562e1c95f', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4c705666-9361-5b5d-a67b-c38c52c1980b', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e4b886c1-fa2a-5a7d-a130-107bb2842323', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8dce94b3-8f9c-5c0e-a6ae-370cde282bfe', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('923ab559-774c-5400-b173-68fb3929c947', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e17af536-ebda-5501-9c8e-54e85ed5bf31', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4a0fa69e-d89b-5e4b-81b2-9dbb19dd50eb', '797668df-4a29-5287-8e59-048e2ff5c3f9', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('2d20fe65-7799-508e-a999-06fc4cf74b26', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-check-swing', NULL, 'Válvula check swing', 'Válvula de retención tipo swing.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VCSW"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/check-swing.webp","alt":"Válvula check swing","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula check swing valvula de retencion tipo swing. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4487c452-ffef-5e08-bb1a-1461abe103e3', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('718c799f-0fa6-5f96-9239-da4ffe89f058', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ed2d60c3-2289-5a0f-a515-6cfaef58c144', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2a77bc23-3c92-51e6-8bb0-2fe664367d7c', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('52f1dc70-667b-50b2-acd3-959d892e0d2d', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('da42684b-2c3b-54c5-b9fa-97a00bde7479', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7c7c386e-23ce-5d03-bb28-2f8a2d0aaa19', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1be5f185-20a8-584c-9727-b279eb6ba116', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('fa1a1457-d642-5362-963d-10e118da6f46', '2d20fe65-7799-508e-a999-06fc4cf74b26', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('e75f44c0-deb2-5c4a-94bb-8105c017797b', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-compuerta-acerrojada', NULL, 'Válvula compuerta acerrojada', 'Válvula de compuerta con unión acerrojada.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VACER"},{"label":"Clase","value":"ISO PN16"},{"label":"Tubería","value":"HDPE, 63 a 315 mm"}]'::jsonb, '[{"url":"/media/catalog/compuerta-acerrojada.webp","alt":"Válvula compuerta acerrojada","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula compuerta acerrojada valvula de compuerta con union acerrojada. valvulas valvulas dn50 · 63 mm dn65 · 75 mm dn80 · 90 mm dn100 · 110 mm dn125 · 140 mm dn150 · 160 mm dn200 · 200 mm dn250 · 250 mm dn300 · 315 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5c7ddb79-97a2-587c-bd83-44f4ad90a67c', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN50 · 63 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b9ee19bc-ebbc-5b64-8621-4889afa0167a', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN65 · 75 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b0e05778-462e-560e-8ae1-15ec5425c92b', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN80 · 90 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('51c6b43d-3989-5386-a474-f43f61f7ea75', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN100 · 110 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('40dabb27-5dc3-526c-9afb-d48b684275c4', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN125 · 140 mm', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3b567a67-012e-5408-b828-fa0293524c07', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN150 · 160 mm', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dff42a0d-6253-543f-85f0-f578217d9d3a', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN200 · 200 mm', 200, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('acc74429-ae24-5e8b-8330-9f2515538e4b', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN250 · 250 mm', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a9bfa517-7bc9-5f69-83b6-591c952cbdc4', 'e75f44c0-deb2-5c4a-94bb-8105c017797b', NULL, 'DN300 · 315 mm', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('b7fdb30d-2784-5412-bde9-cea3b1d6adcb', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-compuerta-bridada', NULL, 'Válvula compuerta bridada', 'Válvula de compuerta con extremos bridados.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[]'::jsonb, '[{"url":"/media/valvula-compuerta-bridada.jpg","alt":"Válvula compuerta bridada","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula compuerta bridada valvula de compuerta con extremos bridados. valvulas valvulas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-de-alivio-bridada', NULL, 'Válvula de alivio bridada', 'Válvula de alivio con extremos bridados.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VDA"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/valvula-alivio.webp","alt":"Válvula de alivio bridada","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula de alivio bridada valvula de alivio con extremos bridados. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5dc64301-9f51-5baa-90d1-79242a156def', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a3aec749-8aa4-5ceb-b010-f1741d10fe1c', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('aa55de62-bf77-5cd3-815b-99e8af36e5c1', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('07471b84-16f6-5988-a5c8-1dd6950b7e42', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8e7da123-593e-57c2-8316-f066b12364f7', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('990e9004-d604-5e3d-8c09-1fd389407bf3', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('83668713-b73b-55e2-bcc8-b95c6004616d', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2864d6df-8c0b-5c01-a211-63d87733fa6f', 'c3aad7ee-c2e8-54cb-b884-1b8dd8aa0994', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('5a970454-3b62-5812-ac44-01a418f0ca81', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-embone-tipo-luflex', NULL, 'Válvula embone tipo Luflex', 'Válvula de embone tipo Luflex.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VEMBO"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/compuerta-luflex.webp","alt":"Válvula embone tipo Luflex","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula embone tipo luflex valvula de embone tipo luflex. valvulas valvulas dn50 · 63 mm dn65 · 75 mm dn80 · 90 mm dn100 · 110 mm dn150 · 160 mm dn200 · 200 mm dn250 · 250 mm dn300 · 315 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a90617e7-d86d-5def-a263-9c08c07b91ff', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN50 · 63 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4c7bed8b-5e3a-5de3-a98d-89058a636edd', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN65 · 75 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('16893f70-afd8-5362-a137-3f500e8c58ac', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN80 · 90 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b025d8ac-be76-5d56-bda0-86405e3a16a8', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN100 · 110 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3282a1b5-5524-5068-b6ed-d24e71fdb82b', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN150 · 160 mm', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4b69d0c1-ea18-55d0-999a-b04c1d37f1cb', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN200 · 200 mm', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('eb581806-d5dd-5c73-abf5-3115d3d15bda', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN250 · 250 mm', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cb3ef15f-17b8-5180-98ff-f3f7efdc83ea', '5a970454-3b62-5812-ac44-01a418f0ca81', NULL, 'DN300 · 315 mm', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('3cef9787-dae8-5e31-9806-03abe2d0eef2', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-flotadora-bridada', NULL, 'Válvula flotadora bridada', 'Válvula flotadora con extremos bridados.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VFLOT"},{"label":"Rango nominal","value":"DN50 a DN400"}]'::jsonb, '[{"url":"/media/catalog/valvula-flotadora.webp","alt":"Válvula flotadora bridada","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula flotadora bridada valvula flotadora con extremos bridados. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b73d58dc-1daf-5861-a165-93b49835194c', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2c73d70e-ed2f-5443-a3a0-1f0326891871', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('459a1a29-17db-5eeb-acd2-ea9e29176f56', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7ce13008-7663-5b6a-8ea3-bb0676cf4ea2', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1267895a-5db3-5c30-ac49-4c0c7517b903', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b7dcae81-4ca3-53be-a6e9-d39dda4e8f73', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('72bb3603-72fa-5968-9e6e-99d86a138a61', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f59036ec-f369-5a6b-9c1a-f81ead57e223', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ad0e0d9f-0518-5460-9e80-52e5ab20f4b8', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN350 · 14"', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('88c2da90-7868-5706-9e27-03321e2192a0', '3cef9787-dae8-5e31-9806-03abe2d0eef2', NULL, 'DN400 · 16"', 400, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('5a8e86a7-f392-5ac3-9a5e-3447ee6e5ad1', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-guillotina', NULL, 'Válvula guillotina', 'Válvula tipo guillotina.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[]'::jsonb, '[{"url":"/media/valvula-guillotina.jpg","alt":"Válvula guillotina","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula guillotina valvula tipo guillotina. valvulas valvulas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('d369a458-3e6f-5f6b-833c-78bbe93cc1a9', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa-excentrica', NULL, 'Válvula mariposa excéntrica', 'Válvula mariposa de diseño excéntrico.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[]'::jsonb, '[{"url":"/media/valvula-mariposa.jpg","alt":"Válvula mariposa excéntrica","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula mariposa excentrica valvula mariposa de diseno excentrico. valvulas valvulas', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('b8471a96-c219-5eb7-9307-da7daf154a69', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-reductora-de-presion', NULL, 'Válvula reductora de presión', 'Válvula para reducción de presión.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VRDP"},{"label":"Rango nominal","value":"DN50 a DN350"}]'::jsonb, '[{"url":"/media/catalog/reductora-presion.webp","alt":"Válvula reductora de presión","illustrative":false}]'::jsonb, 'Catálogo público legacy de FUNDIGSAC', false, 'PUBLISHED',
  'valvula reductora de presion valvula para reduccion de presion. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a576a5e2-3ec6-5d33-9410-c4094c00954c', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ea301073-7620-5a77-a0eb-e941ace1feee', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d280ffda-d340-5215-9279-1d644722728b', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9c38588d-c5b4-5857-bf47-5b09b9530045', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3029b742-ece8-578c-b6d5-b9aabfae3d36', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1f7eb45e-3b61-503d-9a93-c47373dc8f7d', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('393c1a41-38eb-5870-9372-7bb134c6a447', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('810b2d09-1ac4-5d03-b05f-97b95eee733b', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3298ddde-4584-55e8-8429-4a210eefd243', 'b8471a96-c219-5eb7-9307-da7daf154a69', NULL, 'DN350 · 14"', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('b471ff7f-bf3f-5316-b15c-0acd7ff21509', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'codo-hdpe-termofusion-90-sdr11', NULL, 'Codo HDPE termofusión 90° SDR11', 'Accesorio HDPE para unión por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Ángulo","value":"90°"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11 hasta 315 mm; SDR17 desde 355 mm"}]'::jsonb, '[{"url":"/media/catalog/hdpe-codo-90.webp","alt":"Codo HDPE termofusión 90° SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 1', false, 'PUBLISHED',
  'codo hdpe termofusion 90° sdr11 accesorio hdpe para union por termofusion. hdpe hdpe hdpe 63 mm 75 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm 355 mm · sdr17 400 mm · sdr17 450 mm · sdr17 500 mm · sdr17 630 mm · sdr17', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b2de1471-ab35-576e-a5b6-68e723e0962f', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('46a45b16-1072-5994-9623-29a804755764', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '75 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1f28feb4-55f1-5d0e-9811-91b6ba15af72', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '90 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6d763f29-157a-5003-8ed7-660955405e6a', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '110 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('adf17b14-5cdc-5779-91e7-5756c17c151d', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '160 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c2623bda-f569-5539-8f15-35df0954dfcd', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '200 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b6eb1fb8-b66c-5184-a4f9-456bf6346c56', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '250 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('22dcd202-0f6c-5493-9619-4313a9f1a907', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '315 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d579d6bc-72a9-582d-bbee-12ac664a32c6', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '355 mm · SDR17', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('08201c49-a337-51d9-be8e-76f9b2540329', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '400 mm · SDR17', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c1901b4c-496f-56c1-b160-27e335c9b0f2', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '450 mm · SDR17', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('79f00330-ff22-51f8-a9fa-00b50ae51a84', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '500 mm · SDR17', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cb9a7e42-21cd-5325-9563-649e39ddb784', 'b471ff7f-bf3f-5316-b15c-0acd7ff21509', NULL, '630 mm · SDR17', NULL, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('a2edfde3-ceba-5f0c-aec0-d9fa02548f17', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'codo-hdpe-termofusion-45-sdr11', NULL, 'Codo HDPE termofusión 45° SDR11', 'Accesorio HDPE para unión por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Ángulo","value":"45°"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11 hasta 315 mm; SDR17 desde 355 mm"}]'::jsonb, '[{"url":"/media/catalog/hdpe-codo-45.webp","alt":"Codo HDPE termofusión 45° SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 1', false, 'PUBLISHED',
  'codo hdpe termofusion 45° sdr11 accesorio hdpe para union por termofusion. hdpe hdpe hdpe 63 mm 75 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm 355 mm · sdr17 400 mm · sdr17 450 mm · sdr17 500 mm · sdr17 630 mm · sdr17', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1f1b4b53-6c06-591c-9944-2b0fbadc652f', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7c699c85-1073-525c-bbe0-a2ad98999648', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '75 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b86030d6-feee-59d2-9434-f987cdd35a1f', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '90 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('45485f62-01bd-5d96-8372-835f9d445361', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '110 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('11dedd04-bfa1-53d2-a0b6-38afe5a3c371', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '160 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1e5cd967-192d-51b7-8029-19e00f50867f', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '200 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2259e878-29d8-5a73-a156-837eeed4994e', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '250 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cfea9a7b-9a4a-5295-8914-ba0fd57ca572', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '315 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d5ecfd21-d60a-5f89-b34c-843c4fc0054a', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '355 mm · SDR17', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c943e563-13ca-5b68-84f2-6ab94a41b15c', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '400 mm · SDR17', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7fb30635-bc70-557b-8478-b184c63db719', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '450 mm · SDR17', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4350918f-4d08-58fc-81d2-8db25003a134', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '500 mm · SDR17', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7eba0fc8-6571-52b0-9c4a-7624adc8e311', 'a2edfde3-ceba-5f0c-aec0-d9fa02548f17', NULL, '630 mm · SDR17', NULL, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('7334f4f8-391d-5513-98a5-d435afe5cefb', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'acople-gran-rango-agr', NULL, 'Acople gran rango AGR', 'Acople de gran rango: cada medida cubre un intervalo de diámetro exterior de tubería.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"AGR"},{"label":"Rango nominal","value":"DN50 a DN600"},{"label":"Dato publicado","value":"Intervalo de diámetro exterior por DN"}]'::jsonb, '[{"url":"/media/catalog/acople-gran-rango.webp","alt":"Acople gran rango AGR","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 1', false, 'PUBLISHED',
  'acople gran rango agr acople de gran rango: cada medida cubre un intervalo de diametro exterior de tuberia. acoples acoples dn50 · 57–74 mm dn65 · 68–86 mm dn80 · 88–103 mm dn100 · 105–125 mm dn125 · 132–146 mm dn150 · 155–175 mm dn200 · 192–210 mm dn200 · 198–225 mm dn250 · 242–262 mm dn250 · 250–274 mm dn300 · 315–332 mm dn350 · 351–378 mm dn400 · 390–410 mm dn400 · 398–430 mm dn450 · 450–480 mm dn500 · 500–533 mm dn600 · 608–636 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b2affac7-4231-5f87-a15b-4cd07f260240', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN50 · 57–74 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('69aa5f46-1132-5d97-97ed-ef269ecfa8c2', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN65 · 68–86 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6b065f32-d784-5039-867e-83567befe1f0', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN80 · 88–103 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('796141e2-fd9e-563f-89ba-be69f317b396', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN100 · 105–125 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1d3a8831-8056-54ce-a2a1-8207bc3fec35', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN125 · 132–146 mm', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2fd1e480-3b5d-5d76-9f5c-11019f1641aa', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN150 · 155–175 mm', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d3078b03-58c9-59d4-bf8b-aa43b14b3a66', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN200 · 192–210 mm', 200, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d664b260-360c-5ffd-a1db-d85e3534e692', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN200 · 198–225 mm', 200, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('85d29ef0-1b20-5bff-a451-06b19648979e', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN250 · 242–262 mm', 250, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('10555092-d20a-5f3a-8041-556e34d0b9be', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN250 · 250–274 mm', 250, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0c86e182-b237-5b82-83a2-4f418660b1b4', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN300 · 315–332 mm', 300, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('93b81ed2-7e97-5062-b3a4-ec9c0598f173', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN350 · 351–378 mm', 350, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c45fb038-aebc-5c36-908b-f2627974cbe6', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN400 · 390–410 mm', 400, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2f2c7ed4-d7fa-5023-9aaf-d90f85dfdad8', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN400 · 398–430 mm', 400, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('35e6abee-c8f3-53b0-b246-bc606ea7f1d6', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN450 · 450–480 mm', 450, NULL, 15)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ab5528ca-d4f0-5182-a55e-478725dc4036', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN500 · 500–533 mm', 500, NULL, 16)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c67c72c1-6be2-56e9-b29c-78b03ad7e258', '7334f4f8-391d-5513-98a5-d435afe5cefb', NULL, 'DN600 · 608–636 mm', 600, NULL, 17)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('b4949a69-5143-5fb4-af6a-2bc7e06fec30', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-termofusion-hdl-160-2m', NULL, 'Máquina de termofusión HDL, 2 mordazas, timón', 'Máquina manual de soldar PE con dos mordazas y timón.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje","value":"220 V"},{"label":"Placa calefactora máxima","value":"270 °C"},{"label":"Mordazas","value":"2"}]'::jsonb, '[{"url":"/media/catalog/maq-hdl-2m.webp","alt":"Máquina de termofusión HDL, 2 mordazas, timón","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 1 (15/11/2025)', false, 'PUBLISHED',
  'maquina de termofusion hdl, 2 mordazas, timon maquina manual de soldar pe con dos mordazas y timon. equipos equipos hdl 160-2m · 40–160 mm · 42,5 kg hdl 200-2m · 63–200 mm · 45 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cdc22c24-c214-55a8-b582-b7200c30202b', 'b4949a69-5143-5fb4-af6a-2bc7e06fec30', NULL, 'HDL 160-2M · 40–160 mm · 42,5 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('43b236cc-b4e3-5d92-8de3-6e300bed45ee', 'b4949a69-5143-5fb4-af6a-2bc7e06fec30', NULL, 'HDL 200-2M · 63–200 mm · 45 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'adaptador-brida-gran-rango-abgr', NULL, 'Adaptador brida gran rango ABGR', 'Adaptador brida de gran rango: cada medida cubre un intervalo de diámetro exterior de tubería.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"ABGR"},{"label":"Rango nominal","value":"DN50 a DN600"},{"label":"Dato publicado","value":"Intervalo de diámetro exterior por DN"}]'::jsonb, '[{"url":"/media/catalog/adaptador-brida-gran-rango.webp","alt":"Adaptador brida gran rango ABGR","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 1', false, 'PUBLISHED',
  'adaptador brida gran rango abgr adaptador brida de gran rango: cada medida cubre un intervalo de diametro exterior de tuberia. acoples acoples dn50 · 57–74 mm dn65 · 68–86 mm dn80 · 88–103 mm dn100 · 105–125 mm dn125 · 132–146 mm dn150 · 155–175 mm dn200 · 192–210 mm dn200 · 198–225 mm dn250 · 242–262 mm dn250 · 250–274 mm dn300 · 315–332 mm dn350 · 351–378 mm dn400 · 390–410 mm dn400 · 398–430 mm dn450 · 450–480 mm dn500 · 500–533 mm dn600 · 608–636 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a0e35518-37f9-55a7-846c-55ba7ca8d2f2', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN50 · 57–74 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7cb10c71-5167-594f-b449-b08db124b4ac', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN65 · 68–86 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('db247611-4832-5188-9d31-204c01810867', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN80 · 88–103 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a2f34e08-2f68-59c8-b870-ecd27c028bf9', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN100 · 105–125 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a4e78e20-3b07-5e2d-b527-a03cac750301', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN125 · 132–146 mm', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3999c652-57aa-566d-ad61-741cc3272317', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN150 · 155–175 mm', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('36f6923b-73ca-58f1-8a68-2e997ab84887', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN200 · 192–210 mm', 200, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('49fec606-7ee7-5091-8bb3-8f2cf70fd023', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN200 · 198–225 mm', 200, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('20cdc2da-4a98-53b8-adde-e9d234a6e714', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN250 · 242–262 mm', 250, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('06c687d4-ae5a-57b4-9eff-5ecceb2bb357', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN250 · 250–274 mm', 250, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bd70997a-0637-5704-9509-316097d59c57', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN300 · 315–332 mm', 300, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5ba03fc5-ac37-5460-b6f4-28638ebc6a4d', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN350 · 351–378 mm', 350, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('77e4a381-4cbc-59dd-89f4-366404572f6b', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN400 · 390–410 mm', 400, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('35bf9882-7806-5241-a840-d5b73783f7cc', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN400 · 398–430 mm', 400, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8c6c9d8e-46af-57f4-b4ed-06af986bda0d', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN450 · 450–480 mm', 450, NULL, 15)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('57e31291-c65d-5cef-b278-4bb42ff7947d', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN500 · 500–533 mm', 500, NULL, 16)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c49856ab-45e2-50c3-9d27-797f602b2573', '36d365b1-2ad6-5ffd-9b63-9ff5b42d36f7', NULL, 'DN600 · 608–636 mm', 600, NULL, 17)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('e1a0ae68-ee18-59b9-9609-2b35bf040058', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'adaptador-brida-tubo-hdpe-abpth', NULL, 'Adaptador brida para tubo HDPE ABPTH', 'Adaptador con brida para conectar tubería HDPE a una red bridada.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"ABPTH"},{"label":"Tubería","value":"HDPE, 63 a 315 mm"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/adaptador-brida-hdpe.webp","alt":"Adaptador brida para tubo HDPE ABPTH","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 1', false, 'PUBLISHED',
  'adaptador brida para tubo hdpe abpth adaptador con brida para conectar tuberia hdpe a una red bridada. acoples acoples dn50 · 63 mm dn65 · 75 mm dn80 · 90 mm dn100 · 110 mm dn150 · 160 mm dn200 · 200 mm dn250 · 250 mm dn300 · 315 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a46def06-77be-5ffb-8dbd-16daaa13a4ae', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN50 · 63 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('858f9785-5de2-5597-9dbd-4a56203a5cd5', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN65 · 75 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cfb160ac-f5c9-576f-bf6c-2074a7b74cb8', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN80 · 90 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c8dbc8e4-74a9-547c-bb34-b9d3c3cde8a1', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN100 · 110 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8a4b36ab-c0df-558b-8eae-9422170b9b50', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN150 · 160 mm', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4cfca84e-4b24-54f6-9441-4b518acee008', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN200 · 200 mm', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ee9ad52f-4ab2-5b7d-a123-14087bbd9768', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN250 · 250 mm', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8e4f0ec0-0f13-5c6d-b581-294d6e2c7d5b', 'e1a0ae68-ee18-59b9-9609-2b35bf040058', NULL, 'DN300 · 315 mm', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('99bb72b0-9d02-5753-8781-57744ee5cbd3', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'acople-acerrojado-tubo-hdpe-aapth', NULL, 'Acople acerrojado para tubo HDPE AAPTH', 'Acople acerrojado para unir tubería HDPE.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"AAPTH"},{"label":"Tubería","value":"HDPE, 63 a 315 mm"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/acople-acerrojado-hdpe.webp","alt":"Acople acerrojado para tubo HDPE AAPTH","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 1', false, 'PUBLISHED',
  'acople acerrojado para tubo hdpe aapth acople acerrojado para unir tuberia hdpe. acoples acoples dn50 · 63 mm dn65 · 75 mm dn80 · 90 mm dn100 · 110 mm dn150 · 160 mm dn200 · 200 mm dn250 · 250 mm dn300 · 315 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f8864990-1bfc-5854-9dd9-fe36d2a1ead9', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN50 · 63 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6184756e-6815-5466-8fd3-21d1aec4ed5e', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN65 · 75 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ccb655c6-1cb7-5442-b534-06fde049777c', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN80 · 90 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d6d8a24a-54e6-51b0-a41d-6c489ac67a21', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN100 · 110 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d44af2e9-0dae-586e-9f82-14bdcbba7b43', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN150 · 160 mm', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c5d17655-7f2e-5c66-a913-95ff290b30e0', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN200 · 200 mm', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('202a004e-d596-54a9-94bf-d05d1822ed24', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN250 · 250 mm', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('db257d30-bebf-591e-bfdf-d416293b1f0a', '99bb72b0-9d02-5753-8781-57744ee5cbd3', NULL, 'DN300 · 315 mm', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('d5e1d393-4c66-5199-9cc7-80cd7396edd6', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'adaptador-rapido-polietileno-arpth', NULL, 'Adaptador rápido para tubo de polietileno ARPTH', 'Adaptador rápido con brida para tubería de polietileno.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"ARPTH"},{"label":"Tubería","value":"Polietileno, 63 a 400 mm"},{"label":"Rango nominal","value":"DN50 a DN400"}]'::jsonb, '[{"url":"/media/catalog/adaptador-rapido-pe.webp","alt":"Adaptador rápido para tubo de polietileno ARPTH","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 2', false, 'PUBLISHED',
  'adaptador rapido para tubo de polietileno arpth adaptador rapido con brida para tuberia de polietileno. acoples acoples dn50 · 63 mm dn65 · 75 mm dn80 · 90 mm dn100 · 110 mm dn150 · 160 mm dn200 · 200 mm dn250 · 250 mm dn300 · 315 mm dn350 · 355 mm dn400 · 400 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f9fb68e8-7ac2-5eba-a315-18dbe63f0c3f', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN50 · 63 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('511864e1-4650-5999-acef-2a6b2b9edba8', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN65 · 75 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('26a83291-24d1-559e-be99-12d13c7eb174', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN80 · 90 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f4aacab8-bd14-5131-9891-d3dfefb4d4cb', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN100 · 110 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3ddbd6d9-be89-5692-bdaa-826a842ce009', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN150 · 160 mm', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('418a33a1-68dd-562e-a2d3-bbc2aba30fcc', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN200 · 200 mm', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('64e5c2d9-85de-52ae-adba-3cbab2948621', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN250 · 250 mm', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('48cc7f33-74ae-575d-98b6-bb51c4166b9a', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN300 · 315 mm', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f9e73c33-4602-55a7-b229-cf4097b5bc13', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN350 · 355 mm', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8db17832-4bc1-521c-bf30-b2eb962637c5', 'd5e1d393-4c66-5199-9cc7-80cd7396edd6', NULL, 'DN400 · 400 mm', 400, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('52cdaeb1-b9e4-56ed-97fc-07c1810da995', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'acople-995-tuberia-hdpe-a995pth', NULL, 'Acople 995 para tubería HDPE A995PTH', 'Acople tipo 995 para tubería HDPE.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"A995PTH"},{"label":"Tubería","value":"HDPE, 63 a 315 mm"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/acople-995.webp","alt":"Acople 995 para tubería HDPE A995PTH","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 2', false, 'PUBLISHED',
  'acople 995 para tuberia hdpe a995pth acople tipo 995 para tuberia hdpe. acoples acoples dn50 · 63 mm dn65 · 75 mm dn80 · 90 mm dn100 · 110 mm dn150 · 160 mm dn200 · 200 mm dn250 · 250 mm dn300 · 315 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8c02e64b-5976-545d-8890-f93fb97dec34', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN50 · 63 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('10b013de-0116-53b4-9388-949d337fee4c', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN65 · 75 mm', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d450d0ff-6354-50fe-b616-b5159c32c833', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN80 · 90 mm', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bc72177f-e035-55e0-b9ff-d35c49b272eb', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN100 · 110 mm', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('04fa64f5-3a1e-51fe-9dfb-70eb426ad64f', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN150 · 160 mm', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dbe49a9a-7c58-5fdd-8b52-7d89d051b140', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN200 · 200 mm', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f53bced3-2d04-5dd4-8ac9-41f1b79add52', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN250 · 250 mm', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b7df49ac-e740-5f21-a309-8247ff078d37', '52cdaeb1-b9e4-56ed-97fc-07c1810da995', NULL, 'DN300 · 315 mm', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('dc64d4c2-0dde-5c52-84ea-e15905943e1f', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'union-autoportante-desmontaje-uauto', NULL, 'Unión autoportante tipo desmontaje UAUTO', 'Unión autoportante de desmontaje para montaje y mantenimiento de redes bridadas.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"UAUTO"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN600"}]'::jsonb, '[{"url":"/media/catalog/union-autoportante.webp","alt":"Unión autoportante tipo desmontaje UAUTO","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 2', false, 'PUBLISHED',
  'union autoportante tipo desmontaje uauto union autoportante de desmontaje para montaje y mantenimiento de redes bridadas. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a862ae23-2a33-519d-b499-13daa0dda586', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5bc21c7a-eb69-5535-a54c-130c43fc0932', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('840775e5-96e9-5d57-b692-a7dc78fa4b69', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('beac3e68-aeae-5318-bdab-8f9a39f3c7cb', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e0e160e7-72a4-58fe-bc2d-66c44c9541d7', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('35f953d1-dc07-5a83-b74d-18b9b0262cc4', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4b1ca72c-61f5-5d0f-a4b6-05ca885527b9', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1280b95c-4841-575d-8a84-4f09a69aec91', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('335c5171-58e0-502c-abea-5074f9661460', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d0ae95c2-c573-5812-8765-83aebf070202', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN350 · 14"', 350, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d233d380-8b41-5bbb-bf71-4b4dde87577d', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN400 · 16"', 400, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4f3af204-95f8-5508-89ff-b59c2e297c60', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN450 · 18"', 450, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f82b4049-cbb9-5b55-94d5-4fcdf1c14d4d', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN500 · 20"', 500, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c4644148-d470-57e0-b86f-7690a381cc65', 'dc64d4c2-0dde-5c52-84ea-e15905943e1f', NULL, 'DN600 · 24"', 600, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('e50f7581-84e3-57cb-bed5-2db4da2b81e8', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'union-desmontaje-autoportante-autop', NULL, 'Unión de desmontaje autoportante AUTOP', 'Unión de desmontaje autoportante de gran diámetro.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"AUTOP"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN600"}]'::jsonb, '[{"url":"/media/catalog/union-desmontaje-autop.webp","alt":"Unión de desmontaje autoportante AUTOP","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 12', false, 'PUBLISHED',
  'union de desmontaje autoportante autop union de desmontaje autoportante de gran diametro. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('db4eeb39-5b00-58d4-9a1d-a1dfcb3473f0', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('370103f7-bddf-5de6-8913-28b4fee178b6', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e66f54ab-184e-5df3-8b2b-a4e91cf51f0e', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c0b139fc-3e32-5970-86f9-51c839db6851', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8639b00c-15dc-5554-a5ab-30369522a664', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6ef29fbe-54a5-594d-9d37-268c85ce7d13', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cd934b5e-35d7-5552-b569-cfcc5d985d42', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('601c6421-02d8-5a5f-b9ee-48f16f4e80a5', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('279ca5f8-4ebd-556c-bb1a-58119b8b6372', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c2569b86-f829-5694-9bda-45f790087950', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN350 · 14"', 350, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5bba2600-00df-5e9c-baec-8c83986976c2', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN400 · 16"', 400, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e1f2f087-0b82-5d1f-8367-7b8983602c07', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN450 · 18"', 450, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('99f443e9-bbb9-597c-ba58-76cd071dde08', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN500 · 20"', 500, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('65a31485-2feb-54fe-929f-6d60abb71196', 'e50f7581-84e3-57cb-bed5-2db4da2b81e8', NULL, 'DN600 · 24"', 600, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c34937de-57fb-5085-b431-2602811675aa', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'junta-flexible-din-jfdin', NULL, 'Junta flexible DIN JFDIN', 'Junta flexible de caucho con bridas para absorber movimiento y vibración en tuberías.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"JFDIN"},{"label":"Norma de brida","value":"DIN / ISO"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/junta-flexible-din.webp","alt":"Junta flexible DIN JFDIN","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 2', false, 'PUBLISHED',
  'junta flexible din jfdin junta flexible de caucho con bridas para absorber movimiento y vibracion en tuberias. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('be2bee43-89da-5855-9e56-1897e48e79af', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('472ccad6-407e-578a-8e43-39de44cdb6f4', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('258874e3-8d30-57c0-844a-5db8078bf582', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d5793b81-f999-542c-b96b-a04b9c802136', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ebda265b-20e5-5158-97cc-ee58c429085b', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3f3b900b-3a69-5512-89d6-9a31418493b4', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('00245f27-35b8-595c-838a-762f194d7c8d', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('755a6d98-744e-5b10-8d11-1c6061828eaf', 'c34937de-57fb-5085-b431-2602811675aa', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('951844a0-5af5-54a3-ae82-275aae91016b', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'junta-flexible-ansi-jfansi', NULL, 'Junta flexible ANSI JFANSI', 'Junta flexible de caucho con bridas ANSI.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"JFANSI"},{"label":"Norma de brida","value":"ANSI"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/junta-flexible-ansi.webp","alt":"Junta flexible ANSI JFANSI","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 2', false, 'PUBLISHED',
  'junta flexible ansi jfansi junta flexible de caucho con bridas ansi. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cca4ce88-9744-5f90-973c-ba0cf241dfee', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0e3c954c-9a49-5318-9740-59532d5a53bc', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d5ed4a8b-a438-55d5-81f3-e9cce625ea40', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dd448981-e6a4-5f28-9e90-6d21038dcdf8', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c271d272-2db1-52db-a08f-b06cd2d3959e', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4ec27bbc-9080-5ea1-b137-c8a5a0e058f1', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c69dac0d-547e-5260-bb6e-63c89579685c', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('685638f9-2696-5939-a3bd-be50b65a79a6', '951844a0-5af5-54a3-ae82-275aae91016b', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('f4ce821c-d304-59dd-b50b-6869a6eae17c', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'brida-iso-2351-pn16-biso', NULL, 'Brida ISO 2351 PN16 BISO', 'Brida plana ISO 2351 PN16 para conexiones bridadas.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"BISO"},{"label":"Norma","value":"ISO 2351 PN16"},{"label":"Rango nominal","value":"DN50 a DN700"}]'::jsonb, '[{"url":"/media/catalog/brida-iso.webp","alt":"Brida ISO 2351 PN16 BISO","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, páginas 2 y 3', false, 'PUBLISHED',
  'brida iso 2351 pn16 biso brida plana iso 2351 pn16 para conexiones bridadas. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn125 · 5" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24" dn700 · 28"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('038df5e5-e8a6-50d2-a5e8-e51cc8756d36', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('73b24869-3faf-5604-b760-715ddcc35e62', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d4d11200-d1de-5564-be6b-396c263b03f9', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0b8463b4-24e2-5044-9cd1-89aea6157974', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('750772bd-ac3f-5171-a919-a595f33e6ebe', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN125 · 5"', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4c3915af-e4cf-5677-82cd-f73d6c5f784f', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN150 · 6"', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a324a9b6-7785-51f6-9a67-ab819890f353', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN200 · 8"', 200, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('87d30da2-17d7-52af-81ce-a9be3e3793b6', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN200 PN10 · 8"', 200, 10, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8efb43e1-ac9c-5cac-8cbb-48cdd9d24b86', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN250 · 10"', 250, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5d89b7ba-8bd1-5cb7-9ad2-5bd433f9397e', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN300 · 12"', 300, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c2e311d6-336c-56ae-ad7d-5d966654397b', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN350 · 14"', 350, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c941b9d5-dfbf-5767-bcc9-cd3567abb4cf', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN400 · 16"', 400, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d2a3e063-1efe-54bc-b4dd-a8d93fa4272b', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN450 · 18"', 450, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('10bce17d-71d6-53f1-83d3-bda5087505c3', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN500 · 20"', 500, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f0a37a23-a60f-5672-a732-83fd76839d2d', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN600 · 24"', 600, NULL, 15)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6adb6f15-c31b-5345-b590-e6246afea430', 'f4ce821c-d304-59dd-b50b-6869a6eae17c', NULL, 'DN700 · 28"', 700, NULL, 16)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('feadfbae-20a5-5ba4-919f-24d384e9b091', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'brida-ciega-iso-2351-pn16-bciega', NULL, 'Brida ciega ISO 2351 PN16 BCIEGA', 'Brida ciega para cerrar el extremo de una línea bridada.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"BCIEGA"},{"label":"Norma","value":"ISO 2351 PN16"},{"label":"Rango nominal","value":"DN50 a DN600"}]'::jsonb, '[{"url":"/media/catalog/brida-ciega.webp","alt":"Brida ciega ISO 2351 PN16 BCIEGA","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 3', false, 'PUBLISHED',
  'brida ciega iso 2351 pn16 bciega brida ciega para cerrar el extremo de una linea bridada. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0f2dc2ba-9a26-5475-b4e4-b99918ecb8d7', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('57c35606-63a5-50ae-8e43-360eb2810d59', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('693d4f72-d188-5b3d-aa29-7bdd94c77246', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cf424d40-d560-5a2d-96e3-1f3b6051251d', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('59ecc18e-57ac-5fcc-8153-85f9e656893a', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('44d86bb6-650e-52c9-89af-268acc75a6c1', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('562eedd3-40f4-5e67-9b26-c78ddcc86f86', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7b769d44-e125-54c5-939c-7794e2b30352', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b7d36539-eec1-52b0-ba0b-19a7876466cb', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN350 · 14"', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ceafcdc0-6dec-53dc-aa91-d0d04d5444d6', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN400 · 16"', 400, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9bafb005-68ca-597a-80b8-d7aac4b14867', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN450 · 18"', 450, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0bb3ddd2-c9ea-50c8-951c-ea0d7afffb41', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN500 · 20"', 500, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3d3476ed-e45c-587e-b7af-bb4f5752e37c', 'feadfbae-20a5-5ba4-919f-24d384e9b091', NULL, 'DN600 · 24"', 600, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c43cf470-6cce-5aeb-b1cd-ee664453a638', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'brida-ansi-pn10-bansi', NULL, 'Brida ANSI PN10 BANSI', 'Brida ANSI para conexiones bridadas.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"BANSI"},{"label":"Norma","value":"ANSI PN10"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/brida-ansi.webp","alt":"Brida ANSI PN10 BANSI","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 3', false, 'PUBLISHED',
  'brida ansi pn10 bansi brida ansi para conexiones bridadas. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn125 · 5" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('aafe70ef-acdf-5085-8144-e36f78ad8eaa', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('167bea50-206d-50dc-b11d-26c6e15fbfee', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('17dbc4f9-5fa5-5a8a-854b-ac2eec478d79', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('07a1d4ef-22f8-5bd6-928e-c79c8f5d35a0', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('772e5721-a94c-588d-9249-c01f42937258', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN125 · 5"', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5546a2ba-1568-5254-8824-fb57fc87716f', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN150 · 6"', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b2dd2043-1868-56e7-a736-3086120a3ba2', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN200 · 8"', 200, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('113bfdeb-88ec-5f2c-a2c2-a6caa2d5f793', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('573b6b52-53c9-5a0a-a969-232878489091', 'c43cf470-6cce-5aeb-b1cd-ee664453a638', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c3758db4-0202-53a4-9314-ee2c0ade2dc2', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'brida-roscada-din-iso-pn16-brosca', NULL, 'Brida roscada DIN / ISO PN16 BROSCA', 'Brida roscada para conexiones sin soldadura.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"BROSCA"},{"label":"Norma","value":"DIN / ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN150"}]'::jsonb, '[{"url":"/media/catalog/brida-roscada.webp","alt":"Brida roscada DIN / ISO PN16 BROSCA","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 3', false, 'PUBLISHED',
  'brida roscada din / iso pn16 brosca brida roscada para conexiones sin soldadura. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0b3d37db-fb61-5367-9f5e-a3393bceb3e2', 'c3758db4-0202-53a4-9314-ee2c0ade2dc2', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c52d8771-f4e0-54ac-9389-8ab1f205ff0f', 'c3758db4-0202-53a4-9314-ee2c0ade2dc2', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c516509a-fc23-52ff-9966-1d521d435667', 'c3758db4-0202-53a4-9314-ee2c0ade2dc2', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('429f5768-f187-5a2d-ae36-e3d6bf042c53', 'c3758db4-0202-53a4-9314-ee2c0ade2dc2', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bdf57353-204b-52df-b92a-ebdee90e2f6f', 'c3758db4-0202-53a4-9314-ee2c0ade2dc2', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('fd46b3c5-29e9-5f90-a8f3-955dfa1e2f5d', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'anillo-acerrojado-aacer', NULL, 'Anillo acerrojado AACER', 'Anillo acerrojado para tubería HDPE.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"AACER"},{"label":"Clase","value":"ISO PN16"},{"label":"Tubería","value":"HDPE"}]'::jsonb, '[{"url":"/media/catalog/anillo-acerrojado.webp","alt":"Anillo acerrojado AACER","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 12', false, 'PUBLISHED',
  'anillo acerrojado aacer anillo acerrojado para tuberia hdpe. acoples acoples dn50 · 63 mm dn80 · 90 mm dn100 · 110 mm dn150 · 160 mm dn200 · 200 mm dn250 · 250 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ee2cd7f5-5248-5d9f-a815-f4a26bb8b954', 'fd46b3c5-29e9-5f90-a8f3-955dfa1e2f5d', NULL, 'DN50 · 63 mm', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ad1ea00c-36d3-5dde-abcd-3398fd99f303', 'fd46b3c5-29e9-5f90-a8f3-955dfa1e2f5d', NULL, 'DN80 · 90 mm', 80, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('82e7d6a3-d30c-5b7b-b434-c326645f8a63', 'fd46b3c5-29e9-5f90-a8f3-955dfa1e2f5d', NULL, 'DN100 · 110 mm', 100, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7b5e5c1a-a3cc-5bf7-9c8b-d38b3416b31b', 'fd46b3c5-29e9-5f90-a8f3-955dfa1e2f5d', NULL, 'DN150 · 160 mm', 150, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('44750755-46f5-5247-a456-a9246d3b2612', 'fd46b3c5-29e9-5f90-a8f3-955dfa1e2f5d', NULL, 'DN200 · 200 mm', 200, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a2cdf758-02fd-51e2-a64b-fb758f41429a', 'fd46b3c5-29e9-5f90-a8f3-955dfa1e2f5d', NULL, 'DN250 · 250 mm', 250, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('3d577837-95bd-5692-9205-1c0e427a6556', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'codo-sch40-90-csch90', NULL, 'Codo SCH40 × 90° CSCH90', 'Codo de 90° serie SCH40.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"CSCH90"},{"label":"Serie","value":"SCH40"},{"label":"Ángulo","value":"90°"}]'::jsonb, '[{"url":"/media/catalog/codo-sch40-90.webp","alt":"Codo SCH40 × 90° CSCH90","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 4', false, 'PUBLISHED',
  'codo sch40 × 90° csch90 codo de 90° serie sch40. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1d07b766-4c71-5a8f-a38d-ed925fc7927a', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('fb571d5f-fb64-5573-b398-7d2cf6f199bb', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7bf7b2b8-94b3-5089-89ba-1279dc7992a8', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4359fac9-8679-597e-b673-c64e421d24f3', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('41ada754-6e51-54bc-b307-5475410aedff', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7a493a6f-a0f1-5c34-b6f6-43c557dce8f3', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3e651ac5-3f4c-527f-8a0a-23f235793592', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8996d85f-5d3f-5307-9a05-be05a4b38686', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f058570f-8494-5f3c-9619-4d56d0d99543', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN350 · 14"', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('31ef43b7-99e8-5417-ab10-29cc9dade375', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN400 · 16"', 400, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9546bbba-0dd4-5440-a8d8-8b96f1501d92', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN450 · 18"', 450, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('07173800-452a-5f70-81d9-fc6ebd3a5408', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN500 · 20"', 500, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ef72aa13-3f35-580e-8c0a-add52991ec80', '3d577837-95bd-5692-9205-1c0e427a6556', NULL, 'DN600 · 24"', 600, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('abf09aca-4d98-5f1e-9c3c-e203e43dbe29', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'codo-sch40-45-csch45', NULL, 'Codo SCH40 × 45° CSCH45', 'Codo de 45° serie SCH40.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"CSCH45"},{"label":"Serie","value":"SCH40"},{"label":"Ángulo","value":"45°"}]'::jsonb, '[{"url":"/media/catalog/codo-sch40-45.webp","alt":"Codo SCH40 × 45° CSCH45","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 4', false, 'PUBLISHED',
  'codo sch40 × 45° csch45 codo de 45° serie sch40. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('817646fb-651e-5503-a2fe-bb65a82516d3', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('622d51b4-8f2a-5a82-a60b-6424195b7ec9', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7c14a628-3879-5916-9781-390276a2555b', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('03af9f77-b651-5e17-8597-7dc261d179c0', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9ea41725-4bac-5226-9927-c347b2dd9198', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('81bfac89-452e-5c12-9d86-ac43be9d5ae3', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('465a21a8-a904-599f-94b1-e53b4b920c9f', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6908c1a7-e39b-5117-a605-18fad00f531f', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2a06c5d0-62fd-5282-8fee-c754cccbf15e', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN350 · 14"', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e8526e32-a23a-5a71-bfe1-da7dd7fce409', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN400 · 16"', 400, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0ad4615e-3feb-51fa-9a54-f7717aa043e0', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN450 · 18"', 450, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c62ed137-9974-5335-8dfa-4f5a8f184e1e', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN500 · 20"', 500, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('383b6dba-149b-5b86-8a4a-d24b60673f69', 'abf09aca-4d98-5f1e-9c3c-e203e43dbe29', NULL, 'DN600 · 24"', 600, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('fd804547-01b1-5114-b78b-a14e0f751541', (SELECT id FROM product_categories WHERE slug = 'acoples'), 'tuberia-sch40-tsch40', NULL, 'Tubería SCH-40 TSCH40', 'Tubería serie SCH-40.', 'acoples',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"TSCH40"},{"label":"Serie","value":"SCH-40"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/tuberia-sch40.webp","alt":"Tubería SCH-40 TSCH40","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 4', false, 'PUBLISHED',
  'tuberia sch-40 tsch40 tuberia serie sch-40. acoples acoples dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cb54bf73-6833-535e-b2ad-6ded9e60283a', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7833ded3-e8c1-5b49-b5ec-7da090062df2', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f99e3c9d-ab94-5c8a-9d68-f1fac2ceb451', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b0dd280f-8a29-54bc-a9d6-7cf4659fa699', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8374ea5d-715b-561c-9fce-5377b14cdce1', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('be971c08-58cf-5f5c-ae92-a6a68b440c07', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a62d546d-49c0-5db3-bd65-1e5266f8b2b2', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4123009e-55eb-5af5-a78c-812fda0fb0f0', 'fd804547-01b1-5114-b78b-a14e0f751541', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('a5190b33-7e02-5d44-a79f-d087f6832a76', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-compuerta-bridada-vbbh', NULL, 'Válvula compuerta bridada VBBH', 'Válvula de compuerta con extremos bridados, listada en el tarifario como «Válvula bridada P».', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VBBH"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN600"}]'::jsonb, '[{"url":"/media/catalog/compuerta-bridada.webp","alt":"Válvula compuerta bridada VBBH","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 6', false, 'PUBLISHED',
  'valvula compuerta bridada vbbh valvula de compuerta con extremos bridados, listada en el tarifario como «valvula bridada p». valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn125 · 5" dn150 · 6" dn200 pn10 · 8" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e47b876f-bf48-5ea3-9b8f-6f438650de68', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a3909148-c439-5a38-8f21-ca18f7198309', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0c16dda5-1e57-52a8-b306-a3183e208843', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2d786213-5b37-534e-9205-212f012d9a10', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('68e18d63-c17c-5546-bace-51bbc9d8a67f', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN125 · 5"', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d80436cc-d7f0-536f-a2aa-71deeaebe7ad', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN150 · 6"', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cd4826a0-b342-5a5c-adcd-17a28ef7eeae', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('641c79f2-8e8f-520c-8c6e-7716d856701b', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN200 · 8"', 200, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6bbc4585-5287-55a6-8e59-91debec81766', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN250 · 10"', 250, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cfbcb61a-df0a-5a57-bcc6-b415fd372e54', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN300 · 12"', 300, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('28b4bed8-2db6-5913-9fee-b9fd3a9c8e81', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN350 · 14"', 350, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ce29a938-0207-5983-9362-80e89af35917', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN400 · 16"', 400, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f0b3ae6b-a14b-59f0-829a-9c20ba4eaac6', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN450 · 18"', 450, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6433d981-b733-5636-b8b3-5a18f49edcd9', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN500 · 20"', 500, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4a84de56-0da4-5c28-8700-f947f6271475', 'a5190b33-7e02-5d44-a79f-d087f6832a76', NULL, 'DN600 · 24"', 600, NULL, 15)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('5688bca8-e073-5aa6-a808-fdbdd64de82b', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-compuerta-bridada-premium-vbbl', NULL, 'Válvula compuerta bridada premium VBBL', 'Válvula de compuerta bridada, línea premium del tarifario.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VBBL"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/compuerta-bridada-premium.webp","alt":"Válvula compuerta bridada premium VBBL","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 6', false, 'PUBLISHED',
  'valvula compuerta bridada premium vbbl valvula de compuerta bridada, linea premium del tarifario. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('73b8d115-11f1-5a3a-a7fa-a908becc7ea1', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1880dff4-7e57-5f00-a196-3439ae582ff8', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3e2cb14b-bca5-5744-aa0d-7120b18f6146', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e8583ca4-c154-57b7-9d85-e17101fb6dbb', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5a1a4e42-4861-5520-98ae-df643bb58cbe', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('01950b55-e1cb-5f9b-939c-9b4fa920caa9', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1aaa66af-f99d-5405-8e57-9133101717de', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f7b066bc-ce65-56df-85bb-f297b3277e0d', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7601f68b-419b-57c3-a7c2-fb7ed2ede4e8', '5688bca8-e073-5aa6-a808-fdbdd64de82b', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ed118f06-9999-570d-b17d-a47e6b773f2c', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-check-vertical-cierre-silencioso-vcver', NULL, 'Válvula check vertical de cierre silencioso VCVER', 'Válvula de retención vertical de cierre silencioso.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VCVER"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/check-vertical.webp","alt":"Válvula check vertical de cierre silencioso VCVER","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 7', false, 'PUBLISHED',
  'valvula check vertical de cierre silencioso vcver valvula de retencion vertical de cierre silencioso. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('495f4c75-4bab-5e17-b7f7-62c9a2d2c661', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('18dfc301-7d2f-5ee5-9142-1beb3519440c', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c2b2c99e-488b-59d8-982a-0e54d686f673', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('33ee0902-0988-5d25-99a8-0ec3357cd995', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e7b538c1-b2b4-56fa-9936-ac4f478c14bf', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f9d6492f-015e-595b-bcd8-4de21f149cc8', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f036451e-ff79-5228-afb5-a61efaeb6c32', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7c99bbbb-d6f0-529c-b117-ea3a4cbef9bf', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dd2ecb65-e349-50ae-b6b8-f28cedf5575c', 'ed118f06-9999-570d-b17d-a47e6b773f2c', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('7e46e667-6b38-504e-a117-19ac562c2724', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-duo-check-wafer-vdcw', NULL, 'Válvula duo check wafer VDCW', 'Válvula de retención de doble disco tipo wafer.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VDCW"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN400"}]'::jsonb, '[{"url":"/media/catalog/duo-check.webp","alt":"Válvula duo check wafer VDCW","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 8', false, 'PUBLISHED',
  'valvula duo check wafer vdcw valvula de retencion de doble disco tipo wafer. valvulas valvulas dn50 · 2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3701780f-698a-59e9-a65e-7bbfade08063', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d842cf38-d484-556c-bb17-ec0b570f1e48', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN80 · 3"', 80, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e3995913-699c-5757-9d44-df18d52bd4e3', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN100 · 4"', 100, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7f815201-131b-5d84-861c-65b7d1c9e400', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN150 · 6"', 150, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4df2de74-d28f-5794-911f-63eb313963db', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN200 · 8"', 200, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cffb5818-9982-5ed2-9cf3-e0761f4ffeaf', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN250 · 10"', 250, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d8046d76-8b2f-532f-9c32-df64d9b97b9f', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN300 · 12"', 300, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a1a4e916-e1a9-59b8-a3c1-0c0870734bb8', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN350 · 14"', 350, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5e9bf57f-8e4e-5998-b281-daaa37b163e2', '7e46e667-6b38-504e-a117-19ac562c2724', NULL, 'DN400 · 16"', 400, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('613196c2-beff-52cd-8b8b-c8b4adab952d', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-de-aire-3-funciones-premium-vdap', NULL, 'Válvula de aire 3 funciones premium VDAP', 'Válvula de aire (ventosa) de tres funciones.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VDAP"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN200"}]'::jsonb, '[{"url":"/media/catalog/ventosa-3-funciones.webp","alt":"Válvula de aire 3 funciones premium VDAP","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 7', false, 'PUBLISHED',
  'valvula de aire 3 funciones premium vdap valvula de aire (ventosa) de tres funciones. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('58670472-5971-5527-8c01-a806f517ebab', '613196c2-beff-52cd-8b8b-c8b4adab952d', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ae90ef7a-2ecf-5b0f-83c4-1b9d7f4daebb', '613196c2-beff-52cd-8b8b-c8b4adab952d', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ee42fd39-2189-5635-a91b-b1e5fdd4e037', '613196c2-beff-52cd-8b8b-c8b4adab952d', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3077e4c2-d43a-5e12-befb-fd009b45caa6', '613196c2-beff-52cd-8b8b-c8b4adab952d', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cd24985f-cc29-5237-9f02-c92b0d3d398e', '613196c2-beff-52cd-8b8b-c8b4adab952d', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cadaaa3e-b5e2-575b-a143-55bd0f2d817d', '613196c2-beff-52cd-8b8b-c8b4adab952d', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('bdcd0302-838b-56be-a376-e270414aa0a7', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-de-aire-roscada-vdar', NULL, 'Válvula de aire roscada VDAR', 'Válvula de aire (ventosa) con conexión roscada.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VDAR"},{"label":"Clase","value":"ISO PN16"},{"label":"Conexión","value":"Roscada"}]'::jsonb, '[{"url":"/media/catalog/ventosa-roscada.webp","alt":"Válvula de aire roscada VDAR","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 7', false, 'PUBLISHED',
  'valvula de aire roscada vdar valvula de aire (ventosa) con conexion roscada. valvulas valvulas dn25 · 1" dn50 · 2"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a69c8e21-ab97-563a-bf53-34148ecfa2de', 'bdcd0302-838b-56be-a376-e270414aa0a7', NULL, 'DN25 · 1"', 25, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b99544be-cf6b-5e67-99bc-f1ff86b0c63a', 'bdcd0302-838b-56be-a376-e270414aa0a7', NULL, 'DN50 · 2"', 50, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa-wafer-palanca-vmtwp', NULL, 'Válvula mariposa wafer con palanca VMTWP', 'Válvula mariposa tipo wafer de accionamiento manual con palanca.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VMTWP"},{"label":"Clase","value":"ISO PN16"},{"label":"Accionamiento","value":"Palanca"}]'::jsonb, '[{"url":"/media/catalog/mariposa-palanca.webp","alt":"Válvula mariposa wafer con palanca VMTWP","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 8', false, 'PUBLISHED',
  'valvula mariposa wafer con palanca vmtwp valvula mariposa tipo wafer de accionamiento manual con palanca. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn125 · 5" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('496262cb-cbac-53ec-a8e3-e6929c49811b', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('30d881f4-4507-50b3-a128-067d06d372f0', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8b33746f-fffb-517f-b12d-679199ce82f9', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a6447f8a-1025-5ce7-8097-f1b65c2c83d7', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('20067133-7582-5af8-885b-adbb0f795a30', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN125 · 5"', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1f3224d5-c431-5f2a-a3cf-7c05b69fb269', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN150 · 6"', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('926dccb7-72aa-5b79-a65f-41b1d4cca45f', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN200 · 8"', 200, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bc524f0c-3c64-501c-9ccf-15a0f0b72ef3', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e615903a-4249-5809-9735-a150d14d8f7d', '6b57f6c9-e16c-5eb8-9e23-4ee49281b15d', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('abfa1c82-30b9-58f6-b916-606e0eaa4f09', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa-wafer-timon-vmtwt', NULL, 'Válvula mariposa wafer con timón VMTWT', 'Válvula mariposa tipo wafer con operador de timón.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VMTWT"},{"label":"Clase","value":"ISO PN16"},{"label":"Accionamiento","value":"Timón"}]'::jsonb, '[{"url":"/media/catalog/mariposa-timon.webp","alt":"Válvula mariposa wafer con timón VMTWT","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 8', false, 'PUBLISHED',
  'valvula mariposa wafer con timon vmtwt valvula mariposa tipo wafer con operador de timon. valvulas valvulas dn50 · 2" dn80 · 3" dn100 · 4" dn125 · 5" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1069ed0b-5abc-5001-8a3f-41211b92a810', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5d51a1ee-ea11-55b0-8673-2a97f69d2db7', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN80 · 3"', 80, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('13ee13ac-1d40-5e8d-84dd-4acf7d6c1c31', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN100 · 4"', 100, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c9be8982-749f-5dc2-af7c-588b31ea08af', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN125 · 5"', 125, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('52412df8-cea8-5074-b4e0-9cc24dad7ca3', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9a3e900e-a414-514f-9246-492bfc74179a', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9bca2255-1cf9-5af4-a0c4-aa9e113308dc', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9b238760-c2b2-52d2-b824-7f6039246d67', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6a54c48c-61d0-5dc3-af60-8cfe1e762dcd', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN350 · 14"', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3fbd891c-e056-50c1-94f5-0b717653679e', 'abfa1c82-30b9-58f6-b916-606e0eaa4f09', NULL, 'DN400 · 16"', 400, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('eb14232e-1e00-5069-bc17-7ea099827a72', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa-wafer-bridada-vmbb', NULL, 'Válvula mariposa wafer bridada VMBB', 'Válvula mariposa con extremos bridados y operador de timón.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"VMBB"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN80 a DN600"}]'::jsonb, '[{"url":"/media/catalog/mariposa-bridada.webp","alt":"Válvula mariposa wafer bridada VMBB","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 8', false, 'PUBLISHED',
  'valvula mariposa wafer bridada vmbb valvula mariposa con extremos bridados y operador de timon. valvulas valvulas dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('95308e05-18a8-5834-a27e-e69b891e8fe1', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN80 · 3"', 80, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c0e9cd1b-31aa-551d-b4b3-4116956eb79c', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN100 · 4"', 100, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('77370642-9de5-53be-94d5-60eae9e28b2a', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN150 · 6"', 150, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('69c0deb1-81dc-51aa-bf8f-bded1c763344', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN200 · 8"', 200, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('afc989e7-fd39-5bcd-85d0-35e60b688713', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN250 · 10"', 250, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ac88ca3f-08d6-58e2-a319-769ded4440a4', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN300 · 12"', 300, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('09b1dfcd-afe3-5e6c-b8cc-c63c641b6e72', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN350 · 14"', 350, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('80941c5a-50a8-5732-89ea-97141b32dad7', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN400 · 16"', 400, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c0a96fa0-0d0d-5510-a71a-f6b41ce417ae', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN450 · 18"', 450, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1fb84f90-f7d2-5b6c-a112-97fa635f57a7', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN500 · 20"', 500, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ae8111fa-27e5-5eec-9b59-664cbc28064d', 'eb14232e-1e00-5069-bc17-7ea099827a72', NULL, 'DN600 · 24"', 600, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ada8b3df-75d7-5723-ab89-dabe0f138d8d', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa-paleta-inox-minoxp', NULL, 'Válvula mariposa paleta inox con palanca MINOXP', 'Válvula mariposa con paleta inox y palanca.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"MINOXP"},{"label":"Clase","value":"ISO PN16"},{"label":"Paleta","value":"Inox"}]'::jsonb, '[{"url":"/media/catalog/mariposa-palanca.webp","alt":"Válvula mariposa paleta inox con palanca MINOXP","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 12', false, 'PUBLISHED',
  'valvula mariposa paleta inox con palanca minoxp valvula mariposa con paleta inox y palanca. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn125 · 5" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('276e05d0-5035-5ff6-b8a1-b4ed9fa8838b', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('edf8a6d9-61c6-5d87-96d5-1258b4dcaf17', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7326440b-1721-515f-a59e-5e245b845f9f', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('20efa7d1-b6e2-5a2e-aadf-bf64f3221064', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0941cfc4-7c32-5066-b30c-f7e3245a195c', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN125 · 5"', 125, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('32f0eb6e-016b-5f44-af7a-957a251ffd04', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN150 · 6"', 150, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('da7b3654-bb32-5e58-8fe4-0249daace94a', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN200 · 8"', 200, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('556811ba-7a47-5adf-aea8-80881da638e5', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e757b4be-22d3-5718-b478-ddbb38ba9db1', 'ada8b3df-75d7-5723-ab89-dabe0f138d8d', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa-paleta-inox-timon-minoxt', NULL, 'Válvula mariposa paleta inox con timón MINOXT', 'Válvula mariposa con paleta inox y operador de timón.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"MINOXT"},{"label":"Clase","value":"ISO PN16"},{"label":"Paleta","value":"Inox"}]'::jsonb, '[{"url":"/media/catalog/mariposa-timon.webp","alt":"Válvula mariposa paleta inox con timón MINOXT","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 12', false, 'PUBLISHED',
  'valvula mariposa paleta inox con timon minoxt valvula mariposa con paleta inox y operador de timon. valvulas valvulas dn50 · 2" dn80 · 3" dn100 · 4" dn125 · 5" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bbe632d2-0d98-5cd0-a333-b7d686b64f12', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4873b4b3-15ef-59b2-8f83-170e098ba063', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN80 · 3"', 80, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9c5d8c2c-f57d-5c4c-9ccf-b6001ba5d732', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN100 · 4"', 100, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5c062592-0c7c-5b0b-8320-ed4952e07d78', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN125 · 5"', 125, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('fb55bfe3-3134-5791-8b4c-8d2f746d472a', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('852a7ace-3e70-5275-adb1-cfd769fa65d1', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('618de545-5101-5367-9cfb-06faec86e371', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('30c8eb36-7bd0-5dea-98bc-b2e026c72cea', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('237e6844-1dde-54e3-9ded-aacf29c7ee52', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN350 · 14"', 350, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('49d04bba-8726-5087-8992-ef831537d082', '1b0b970b-d1cd-527a-8d38-a27bcf9e6c4e', NULL, 'DN400 · 16"', 400, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('95fd2463-14f2-5c0f-a79d-6d349e67ad35', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'valvula-mariposa-inox-actuador-neumatico-mcan', NULL, 'Válvula mariposa inox con actuador neumático MCAN', 'Válvula mariposa inox con actuador neumático.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"MCAN"},{"label":"Clase","value":"ISO PN16"},{"label":"Accionamiento","value":"Actuador neumático"}]'::jsonb, '[{"url":"/media/catalog/mariposa-neumatica.webp","alt":"Válvula mariposa inox con actuador neumático MCAN","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 12', false, 'PUBLISHED',
  'valvula mariposa inox con actuador neumatico mcan valvula mariposa inox con actuador neumatico. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1056f037-b59f-5aab-b957-e06ae0ff6909', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9943447f-33e8-53bf-a5a5-11ca8b85af2e', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e69c2bcc-a632-5212-9057-dc3183c10f23', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('82ebe395-faf1-5b96-b94b-5f8c137b91dc', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('01c5ca6c-2956-522c-8012-b2e72e43af67', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('454b4e29-a033-5e64-92b1-3ae355176219', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('062eb00f-483c-5c78-b9a9-054459ce21ec', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('54f4973e-6e5a-5d90-8269-fb2ca7a619ab', '95fd2463-14f2-5c0f-a79d-6d349e67ad35', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'filtro-tipo-y-bridado-fyb', NULL, 'Filtro tipo Y bridado FYB', 'Filtro tipo Y con extremos bridados para proteger válvulas y equipos de la red.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"FYB"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN400"}]'::jsonb, '[{"url":"/media/catalog/filtro-y.webp","alt":"Filtro tipo Y bridado FYB","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 5', false, 'PUBLISHED',
  'filtro tipo y bridado fyb filtro tipo y con extremos bridados para proteger valvulas y equipos de la red. valvulas valvulas dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn200 pn10 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('fc49f391-636e-5247-b89a-9a4e95359d05', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e6354201-9fd0-51d0-9a16-4739365c95f1', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3434e36d-8d95-5b3c-aa6e-9a7588800a68', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d08d05b7-4003-5950-97c5-8813b887bd06', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ee68209e-54b3-5349-bd5e-a7d0c90061fa', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f9a76d80-af1b-5835-b2c8-2b256f90abfc', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('925bee8a-7694-5bc7-9db4-13a33281ad2e', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN200 PN10 · 8"', 200, 10, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f0087ce0-343f-51d8-89be-19120dfda753', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN250 · 10"', 250, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6ea7b6e1-8c82-5ba5-aabd-094b12bad13d', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN300 · 12"', 300, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1f1725f0-cca0-5a97-94cd-ce1865e1074f', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN350 · 14"', 350, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dfb1f8b9-8962-5303-aae6-d9be8a56ad91', 'fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', NULL, 'DN400 · 16"', 400, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c73376a8-4017-5590-809c-cc59d679acdb', (SELECT id FROM product_categories WHERE slug = 'valvulas'), 'pilotos-alivio-reductora-de-presion', NULL, 'Pilotos de alivio y de reductora de presión', 'Pilotos de ½″ para válvulas de alivio y de reducción de presión.', 'valvulas',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Medida","value":"1/2\""},{"label":"Uso","value":"Piloto de válvula de alivio y de reductora"}]'::jsonb, '[{"url":"/media/catalog/piloto-alivio.webp","alt":"Pilotos de alivio y de reductora de presión","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 5', false, 'PUBLISHED',
  'pilotos de alivio y de reductora de presion pilotos de ½″ para valvulas de alivio y de reduccion de presion. valvulas valvulas piloto de alivio · 1/2" piloto de reductora · 1/2"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1793118d-dccb-5a7c-a1c8-2ec8e24e0d44', 'c73376a8-4017-5590-809c-cc59d679acdb', NULL, 'Piloto de alivio · 1/2"', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('da6c093e-a6a5-5cb2-b5ef-6af3146db796', 'c73376a8-4017-5590-809c-cc59d679acdb', NULL, 'Piloto de reductora · 1/2"', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('f1721e67-98ff-58a9-9b49-3c1f5af783ec', (SELECT id FROM product_categories WHERE slug = 'hierro-ductil'), 'tuberia-hierro-ductil-c40-k9-k7-thd', NULL, 'Tubería de hierro dúctil C40 · K9 · K7 THD', 'Tubería de hierro dúctil para redes de agua. El tarifario la lista bajo EN 545 PN16 e ISO 2531.', 'hierro-ductil',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"THD"},{"label":"Clases","value":"C40 · K9 · K7"},{"label":"Normas","value":"EN 545 PN16 · ISO 2531"},{"label":"Rango publicado","value":"DN80 a DN600 (el documento lista también DN700 a DN1200 sin dato comercial)"}]'::jsonb, '[{"url":"/media/catalog/tuberia-hierro-ductil.webp","alt":"Tubería de hierro dúctil C40 · K9 · K7 THD","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 11', false, 'PUBLISHED',
  'tuberia de hierro ductil c40 · k9 · k7 thd tuberia de hierro ductil para redes de agua. el tarifario la lista bajo en 545 pn16 e iso 2531. hierro-ductil hierro-ductil dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('07a49660-be75-56b3-a44f-45c74fbc4c27', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN80 · 3"', 80, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e19e3d53-3980-5f4b-912b-aaa502f48479', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN100 · 4"', 100, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e9f22118-eff1-5861-b174-43997a8c1165', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN150 · 6"', 150, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1505f2ba-0479-5677-8658-41c8202e20a1', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN200 · 8"', 200, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ffa24a7e-4eb3-584a-b119-69aa1fefa65c', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN250 · 10"', 250, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('992f8f25-70d9-5e2a-ba45-eb3097d31f9c', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN300 · 12"', 300, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7bcbfd1b-046e-5e1f-bf05-0974cf82d1d8', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN350 · 14"', 350, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('78747e7f-341e-5fbc-bee5-58682ed72d15', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN400 · 16"', 400, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2006180f-4d87-5030-91be-ee91bc56621d', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN450 · 18"', 450, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5a9f5aa8-8d76-5b41-b153-fb184ba4c26d', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN500 · 20"', 500, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('65d017a8-d529-5aed-b134-66feb7a254a1', 'f1721e67-98ff-58a9-9b49-3c1f5af783ec', NULL, 'DN600 · 24"', 600, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('49183a35-33f1-5a0a-aa7c-c9d1220ddf93', (SELECT id FROM product_categories WHERE slug = 'hierro-ductil'), 'marco-y-tapa-pesada-2seg', NULL, 'Marco y tapa pesada 2SEG', 'Marco y tapa de hierro para buzones, versión pesada con dos seguros.', 'hierro-ductil',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"2SEG"},{"label":"Modelo","value":"D400"},{"label":"Medida","value":"600 mm"},{"label":"Peso","value":"60 kg"},{"label":"Seguros","value":"2"}]'::jsonb, '[{"url":"/media/catalog/marco-tapa-pesada.webp","alt":"Marco y tapa pesada 2SEG","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 11', false, 'PUBLISHED',
  'marco y tapa pesada 2seg marco y tapa de hierro para buzones, version pesada con dos seguros. hierro-ductil hierro-ductil', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ca933b33-816b-5381-b53c-d77bc5f0c5bd', (SELECT id FROM product_categories WHERE slug = 'hierro-ductil'), 'marco-y-tapa-liviana-1seg', NULL, 'Marco y tapa liviana 1SEG', 'Marco y tapa de hierro para buzones, versión liviana con un seguro.', 'hierro-ductil',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"1SEG"},{"label":"Modelo","value":"D400"},{"label":"Medida","value":"600 mm"},{"label":"Peso","value":"38 kg"},{"label":"Seguros","value":"1"}]'::jsonb, '[{"url":"/media/catalog/marco-tapa-liviana.webp","alt":"Marco y tapa liviana 1SEG","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 11', false, 'PUBLISHED',
  'marco y tapa liviana 1seg marco y tapa de hierro para buzones, version liviana con un seguro. hierro-ductil hierro-ductil', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('a72218eb-314a-5bfb-976f-9cea9a6e1fba', (SELECT id FROM product_categories WHERE slug = 'hierro-ductil'), 'tee-bridada-tbb', NULL, 'Tee bridada TBB', 'Tee de hierro dúctil con tres bridas, en versiones de igual diámetro y con derivación reducida.', 'hierro-ductil',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"TBB"},{"label":"Clase","value":"ISO PN16"},{"label":"Medida","value":"Cabeza × derivación"}]'::jsonb, '[{"url":"/media/catalog/tee-bridada.webp","alt":"Tee bridada TBB","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 10', false, 'PUBLISHED',
  'tee bridada tbb tee de hierro ductil con tres bridas, en versiones de igual diametro y con derivacion reducida. hierro-ductil hierro-ductil dn50 × dn50 dn65 × dn65 dn80 × dn50 dn80 × dn80 dn100 × dn50 dn100 × dn80 dn100 × dn100 dn150 × dn50 dn150 × dn80 dn150 × dn100 dn150 × dn150 dn200 × dn50 dn200 × dn80 dn200 × dn100 dn200 × dn150 dn200 × dn200 dn250 × dn50 dn250 × dn80 dn250 × dn100 dn250 × dn150 dn250 × dn200 dn250 × dn250 dn300 × dn50 dn300 × dn80 dn300 × dn100 dn300 × dn150 dn300 × dn200 dn300 × dn250 dn300 × dn300 dn350 × dn350 dn400 × dn400 dn450 × dn450 dn500 × dn500 dn600 × dn600', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('65c368fb-ba4f-5051-85f1-61ef2ef50108', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN50 × DN50', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('426df1da-5d3e-5246-9593-81e866414536', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN65 × DN65', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4d269ea9-da82-5df1-b7d6-740c21a83cee', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN80 × DN50', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f004c50e-389a-5467-8a95-26865b8fffee', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN80 × DN80', 80, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5c9bcff9-1e37-5479-9bc8-b4889f6eb087', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN100 × DN50', 100, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0a224929-f2f8-56eb-b572-eac99503747c', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN100 × DN80', 100, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('741d531b-23ae-5baa-9057-b958b5ef355d', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN100 × DN100', 100, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7291bc3d-9568-5c24-9d20-720c0f046b24', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN150 × DN50', 150, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('62ca05a6-f997-5c5e-996e-ef473ae102af', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN150 × DN80', 150, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ed623c43-99af-5998-8c32-a21243e53ae3', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN150 × DN100', 150, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0c894ba9-4f26-54b8-af77-d3abf826bac3', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN150 × DN150', 150, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b1b9498d-cbd8-52cf-aacf-2eb3d2231b10', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN200 × DN50', 200, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('023eec70-4743-5ae8-8952-c98a96b5afc6', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN200 × DN80', 200, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1d664b01-0974-573b-aeba-bdbb1486a91c', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN200 × DN100', 200, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d503b5b2-a40a-5f55-8696-da405676b5e6', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN200 × DN150', 200, NULL, 15)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('98e10801-6814-5d7f-9393-4fe2b1538906', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN200 × DN200', 200, NULL, 16)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9dca5a29-0acd-5756-9d88-5fe4085b8e6a', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN250 × DN50', 250, NULL, 17)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bf401e19-ab79-5f4b-9538-7e788f4b5b8f', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN250 × DN80', 250, NULL, 18)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f0a8fcae-118d-56c7-a906-d2a0333d6fb2', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN250 × DN100', 250, NULL, 19)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c8703241-bd1a-50a0-b8de-ae1d461d4847', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN250 × DN150', 250, NULL, 20)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9377fed5-0180-5757-916a-0a114e46d344', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN250 × DN200', 250, NULL, 21)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5c1c5c0b-cd30-5d79-a8d2-608748d8e4e3', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN250 × DN250', 250, NULL, 22)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('14bbde86-dcf3-53e0-a25f-027a5c919eb8', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN300 × DN50', 300, NULL, 23)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('220f41b5-3941-525a-b200-130d14b1d48e', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN300 × DN80', 300, NULL, 24)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('fc514def-56fe-5c91-8aff-312b0ed6bdd5', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN300 × DN100', 300, NULL, 25)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4782484b-2815-534c-b985-e2a7525947cc', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN300 × DN150', 300, NULL, 26)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('90f98318-dc2c-5ca0-a843-b383438722b9', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN300 × DN200', 300, NULL, 27)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e556f304-fed9-5cee-be9b-a5a29bcc69e7', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN300 × DN250', 300, NULL, 28)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dff73df5-e4c7-58b2-b1bd-4bb15635d889', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN300 × DN300', 300, NULL, 29)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1646bf4c-c33d-594d-9917-56c71942826d', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN350 × DN350', 350, NULL, 30)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ec03e455-0552-5a8e-863b-1dfd544bf6d8', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN400 × DN400', 400, NULL, 31)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e6ddd619-11f8-556b-86dd-576c0a34ea3d', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN450 × DN450', 450, NULL, 32)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('342ada23-5d0f-5b5f-b9d4-1080d86b7ad6', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN500 × DN500', 500, NULL, 33)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('30303a8c-9797-5146-a685-119d073748d3', 'a72218eb-314a-5bfb-976f-9cea9a6e1fba', NULL, 'DN600 × DN600', 600, NULL, 34)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ed54df20-767b-5bca-894e-13042bfed9c2', (SELECT id FROM product_categories WHERE slug = 'hierro-ductil'), 'tee-luflex-brida-tlb', NULL, 'Tee Luflex con brida TLB', 'Tee con extremos de embone Luflex y derivación bridada.', 'hierro-ductil',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"TLB"},{"label":"Medida","value":"Tubo (mm) × derivación bridada (DN)"}]'::jsonb, '[{"url":"/media/catalog/tee-luflex.webp","alt":"Tee Luflex con brida TLB","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 10', false, 'PUBLISHED',
  'tee luflex con brida tlb tee con extremos de embone luflex y derivacion bridada. hierro-ductil hierro-ductil 63 mm × dn50 90 mm × dn50 90 mm × dn80 110 mm × dn50 110 mm × dn100 160 mm × dn50 160 mm × dn100 200 mm × dn50 200 mm × dn100', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('87adf156-3d1f-5e36-a8fa-3ddb09ae23d1', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '63 mm × DN50', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4df0ec50-7555-5c8e-9608-e5899a0971fe', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '90 mm × DN50', 50, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('85db4766-9585-5931-a9e4-f4011eb70df3', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '90 mm × DN80', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8f66de13-7b61-5c00-a15a-861e3cbad362', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '110 mm × DN50', 50, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9fd91220-c6d4-5807-ae9c-2c88ad041448', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '110 mm × DN100', 100, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d9d79ae5-501a-5e30-8022-6754ed14a505', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '160 mm × DN50', 50, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('975a858e-8fa8-571f-a9cf-d5903afb4a14', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '160 mm × DN100', 100, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8cd73633-bb30-55f0-99d3-9641922b00e2', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '200 mm × DN50', 50, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4c1f3bea-7114-5849-a75b-21ec6f7c9413', 'ed54df20-767b-5bca-894e-13042bfed9c2', NULL, '200 mm × DN100', 100, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c35df913-6283-5d35-b50b-6cdc3f5652a9', (SELECT id FROM product_categories WHERE slug = 'hierro-ductil'), 'yee-bridado-ybb', NULL, 'Yee bridado YBB', 'Yee de hierro dúctil con tres bridas.', 'hierro-ductil',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"YBB"},{"label":"Clase","value":"ISO PN16"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/yee-bridada.webp","alt":"Yee bridado YBB","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 10', false, 'PUBLISHED',
  'yee bridado ybb yee de hierro ductil con tres bridas. hierro-ductil hierro-ductil dn50 · 2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('73eae87f-1288-5e24-b90d-4fe4380c588d', 'c35df913-6283-5d35-b50b-6cdc3f5652a9', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7616b9ec-c4a1-5ba9-b7b5-8d3dd029c289', 'c35df913-6283-5d35-b50b-6cdc3f5652a9', NULL, 'DN80 · 3"', 80, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('fdadb434-85cc-573a-9247-ced64f1f10e9', 'c35df913-6283-5d35-b50b-6cdc3f5652a9', NULL, 'DN100 · 4"', 100, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d30ff4b9-20df-5b50-8220-2a2e38e6302b', 'c35df913-6283-5d35-b50b-6cdc3f5652a9', NULL, 'DN150 · 6"', 150, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0e6b6aeb-2fe2-5e5e-9432-50bd45377f2f', 'c35df913-6283-5d35-b50b-6cdc3f5652a9', NULL, 'DN200 · 8"', 200, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7967098c-fbe7-5c27-8e8d-fca341012397', 'c35df913-6283-5d35-b50b-6cdc3f5652a9', NULL, 'DN250 · 10"', 250, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('235d81fa-0cfb-564d-a29d-bd215f768761', 'c35df913-6283-5d35-b50b-6cdc3f5652a9', NULL, 'DN300 · 12"', 300, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', (SELECT id FROM product_categories WHERE slug = 'medidores'), 'medidor-electromagnetico-melec', NULL, 'Medidor electromagnético MELEC', 'Medidor de caudal electromagnético con transmisor y cable de señal.', 'medidores',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"MELEC"},{"label":"Rango nominal","value":"DN50 a DN600"}]'::jsonb, '[{"url":"/media/catalog/medidor-electromagnetico.webp","alt":"Medidor electromagnético MELEC","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, página 11', false, 'PUBLISHED',
  'medidor electromagnetico melec medidor de caudal electromagnetico con transmisor y cable de senal. medidores medidores dn50 · 2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12" dn350 · 14" dn400 · 16" dn450 · 18" dn500 · 20" dn600 · 24"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e8e9c97e-a3e2-5995-8e9f-a8b9ef063ac6', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('efa14024-7d6b-5ed3-af23-570ba5786665', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN80 · 3"', 80, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a73b8bcb-54da-5243-9f1f-5bd88964c1a3', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN100 · 4"', 100, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('09878464-6702-514e-9130-f131f6420344', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN150 · 6"', 150, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('70e2510f-ff2f-5baa-a676-336d255e4af0', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN200 · 8"', 200, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('89df2253-d2b8-5e18-8033-aa9a46fd3bfa', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN250 · 10"', 250, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ceb4a8f4-9151-5e85-8f03-d801cb039c0e', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN300 · 12"', 300, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('295e36bd-b07a-55d5-87cd-210e872a2e86', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN350 · 14"', 350, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('151e842f-29ae-5fae-bc90-abf816fe964a', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN400 · 16"', 400, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e100e63b-82b2-516f-b52f-1bcd1197e5c5', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN450 · 18"', 450, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('38860937-c1c7-582f-9241-a1427acedc4c', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN500 · 20"', 500, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4da3640b-d18f-5165-ae6d-92009f5b55ee', '0583ef8f-a1a0-5223-bd66-db4d50ceb7c6', NULL, 'DN600 · 24"', 600, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('bddc0672-24a1-5f0c-a414-eea9e60f6585', (SELECT id FROM product_categories WHERE slug = 'medidores'), 'medidor-tipo-woltman-mwol', NULL, 'Medidor tipo Woltman MWOL', 'Medidor de agua tipo Woltman con extremos bridados.', 'medidores',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Código","value":"MWOL"},{"label":"Rango nominal","value":"DN50 a DN300"}]'::jsonb, '[{"url":"/media/catalog/medidor-woltman.webp","alt":"Medidor tipo Woltman MWOL","illustrative":false}]'::jsonb, 'PRECIO MARZO2026.pdf, páginas 11 y 12', false, 'PUBLISHED',
  'medidor tipo woltman mwol medidor de agua tipo woltman con extremos bridados. medidores medidores dn50 · 2" dn65 · 2 1/2" dn80 · 3" dn100 · 4" dn150 · 6" dn200 · 8" dn250 · 10" dn300 · 12"', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2512193e-1c81-5e1e-a677-bc781a3fdf90', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN50 · 2"', 50, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f49fa264-bed0-548f-91b9-0b18e27c66c6', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN65 · 2 1/2"', 65, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('77fb0c28-20b0-5ba8-8888-2868314989cb', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN80 · 3"', 80, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('705025b5-3dff-5776-98de-50b65c074df7', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN100 · 4"', 100, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f03c34ae-ffcc-5456-b11b-efe185cc386d', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN150 · 6"', 150, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('36d11eb4-f4e8-524c-b598-c1da2b54ee88', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN200 · 8"', 200, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('62c7b398-5ef1-54f0-b695-e705ea15d5ba', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN250 · 10"', 250, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('55ebeed6-f0c5-56c3-b813-ba4ca44203a4', 'bddc0672-24a1-5f0c-a414-eea9e60f6585', NULL, 'DN300 · 12"', 300, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'codo-hdpe-termofusion-225-sdr11', NULL, 'Codo HDPE termofusión 22.5° SDR11', 'Codo de 22.5° para unión por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Ángulo","value":"22.5°"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11 hasta 315 mm; SDR17 desde 355 mm"}]'::jsonb, '[{"url":"/media/catalog/hdpe-codo-225.webp","alt":"Codo HDPE termofusión 22.5° SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 1', false, 'PUBLISHED',
  'codo hdpe termofusion 22.5° sdr11 codo de 22.5° para union por termofusion. hdpe hdpe hdpe 63 mm 75 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm 355 mm · sdr17 400 mm · sdr17 450 mm · sdr17 500 mm · sdr17 630 mm · sdr17', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9b616ca6-6199-58f5-ad39-5993f32ecfe7', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ddebce6c-6773-54a9-b052-e681858ffbd6', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '75 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('79101424-8075-513e-9c35-28af9a24e874', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '90 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('89cd5a13-fa44-55b4-a2e8-f84f2dcfea66', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '110 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('62ac5dcb-ce4e-5d74-a3bd-a27ec4ee7a24', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '160 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6f6eb337-2ba8-5245-9f7d-59b573ab02b3', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '200 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('33fe9051-55f4-5525-ae36-3c4818a7b324', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '250 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('264cec68-964e-50f9-815e-22e8dfe400a6', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '315 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('78079d57-ee59-50bc-9c05-51963c856549', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '355 mm · SDR17', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dfeea551-48f2-54fb-908e-ca05faedcfd8', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '400 mm · SDR17', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c85e3573-75dc-56d9-b681-7cca785228c4', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '450 mm · SDR17', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('607089b3-bdce-50d0-a015-f38d188b89b0', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '500 mm · SDR17', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a7e16d05-7ab3-5640-8f94-829e0c7c6eed', 'eaf7ebe2-4c89-596f-b057-8cf9e9330eaa', NULL, '630 mm · SDR17', NULL, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('ead5ebdf-39f1-502a-bdd2-28404d50c574', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'flange-hdpe-termofusion-sdr11', NULL, 'Flange HDPE termofusión SDR11', 'Flange (porta brida) para unión por termofusión. Se acopla con brida tipo backing ring.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11 hasta 315 mm; SDR17 desde 355 mm"}]'::jsonb, '[{"url":"/media/catalog/hdpe-flange.webp","alt":"Flange HDPE termofusión SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 1', false, 'PUBLISHED',
  'flange hdpe termofusion sdr11 flange (porta brida) para union por termofusion. se acopla con brida tipo backing ring. hdpe hdpe hdpe 63 mm 75 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm 355 mm · sdr17 400 mm · sdr17 450 mm · sdr17 500 mm · sdr17 630 mm · sdr17', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('23e1a4e0-19df-5cef-acbd-7ccf19a89bcf', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dec27736-ae5b-51f5-8bb9-cefa9439d448', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '75 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3c5c3581-a9ee-526e-80ae-43237e7e1dd9', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '90 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d07de973-71e5-56f2-95b4-f33975c1dbae', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '110 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4263f82d-2084-5fd3-80b3-ab1f1ef901c5', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '160 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b2409067-3da2-5ff9-87f1-f1c75946a5eb', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '200 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e19dcf07-373b-5050-b44b-0a47e12dc014', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '250 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4e52da4a-8ac6-5b68-8f0e-73a6e379b0f6', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '315 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9e558ed6-b173-51f4-8b42-141f68d0b818', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '355 mm · SDR17', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c1f9c53f-b53c-5c18-ac86-497ddd603074', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '400 mm · SDR17', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8b257c3a-298d-5691-b1ce-12f04885a2c5', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '450 mm · SDR17', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bfc4de6c-5caa-5d88-b5fd-299f0da9d98c', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '500 mm · SDR17', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8d331153-ab8e-5e9d-8c1b-0282de3f5c30', 'ead5ebdf-39f1-502a-bdd2-28404d50c574', NULL, '630 mm · SDR17', NULL, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'tee-hdpe-termofusion-sdr11', NULL, 'Tee HDPE termofusión SDR11', 'Tee de igual diámetro para unión por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11 hasta 315 mm; SDR17 desde 355 mm"}]'::jsonb, '[{"url":"/media/catalog/hdpe-tee.webp","alt":"Tee HDPE termofusión SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 2', false, 'PUBLISHED',
  'tee hdpe termofusion sdr11 tee de igual diametro para union por termofusion. hdpe hdpe hdpe 63 mm 75 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm 355 mm · sdr17 400 mm · sdr17 450 mm · sdr17 500 mm · sdr17 630 mm · sdr17', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('64ee5b02-833f-5687-b688-d8f7aa7a850e', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4ebaf798-7927-5f91-9436-f4bec05e6563', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '75 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bb3f56eb-564a-5db5-b57c-ec1ba407b82a', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '90 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4be5f91d-8c3c-53e8-9e10-dd009b71b654', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '110 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('407d5442-7dd7-5f55-84d8-790dedda8b04', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '160 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('56c22fb2-6515-5cd3-9e1f-03bfa311d002', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '200 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4ea63abc-f387-51c2-a70f-6b48009ad58a', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '250 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('aaeb601a-8113-5572-882e-71446de67363', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '315 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('266b9efb-da8c-531d-be86-0d94e8ab959b', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '355 mm · SDR17', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7152e7f1-eff2-5f62-8fff-8221bf467d38', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '400 mm · SDR17', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8c6621e2-6c73-5cb5-ab46-6d936f90838e', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '450 mm · SDR17', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('937d00fb-7205-560b-928a-da49b277ea1d', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '500 mm · SDR17', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0aef4aef-d0af-50ff-b3bc-d2f43e968d64', 'fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', NULL, '630 mm · SDR17', NULL, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('c0f89392-5e5d-5bc2-bf72-e6b25eeec599', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'tee-reducida-hdpe-termofusion-sdr11', NULL, 'Tee reducida HDPE termofusión SDR11', 'Tee con derivación de menor diámetro para unión por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Unión","value":"Termofusión"},{"label":"Medida","value":"Cabeza × derivación (mm)"}]'::jsonb, '[{"url":"/media/catalog/hdpe-tee.webp","alt":"Tee reducida HDPE termofusión SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 2', false, 'PUBLISHED',
  'tee reducida hdpe termofusion sdr11 tee con derivacion de menor diametro para union por termofusion. hdpe hdpe hdpe 75 × 63 mm 90 × 63 mm 90 × 75 mm 110 × 63 mm 110 × 75 mm 110 × 90 mm 160 × 63 mm 160 × 90 mm 160 × 110 mm 200 × 90 mm 200 × 110 mm 200 × 160 mm 250 × 90 mm 250 × 110 mm 250 × 160 mm 250 × 200 mm 315 × 90 mm · sdr17 315 × 110 mm · sdr17 315 × 160 mm · sdr17 315 × 200 mm · sdr17 315 × 250 mm · sdr17 355 × 110 mm · sdr17 355 × 160 mm · sdr17 355 × 200 mm · sdr17 355 × 250 mm · sdr17 355 × 315 mm · sdr17 400 × 110 mm · sdr17 400 × 160 mm · sdr17 400 × 200 mm · sdr17 400 × 250 mm · sdr17 400 × 315 mm · sdr17 400 × 355 mm · sdr17 450 × 110 mm · sdr17 450 × 160 mm · sdr17 450 × 200 mm · sdr17 450 × 250 mm · sdr17 450 × 315 mm · sdr17 450 × 355 mm · sdr17 450 × 400 mm · sdr17 500 × 110 mm · sdr17 500 × 160 mm · sdr17 500 × 200 mm · sdr17 500 × 250 mm · sdr17 500 × 315 mm · sdr17 500 × 355 mm · sdr17 500 × 400 mm · sdr17 500 × 450 mm · sdr17', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e808d078-dfe1-5434-8f78-f4724ec351df', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '75 × 63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a6b432cd-ee2a-52f7-b4d9-b7e8399c2b8b', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '90 × 63 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('403ea0aa-c2f0-51f5-a16b-b748830d8629', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '90 × 75 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('297abb47-fd1b-5cd5-919c-1e66018e9530', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '110 × 63 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('445745bb-39c4-5484-ad06-6cdea80bb107', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '110 × 75 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8e359275-4358-58a0-80e4-d1f0ebacd47a', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '110 × 90 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('eea0e8b0-5dd8-5c1e-8070-5824539cbdff', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '160 × 63 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5a44b56d-c3ef-54a4-9ecc-0838efd7c44c', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '160 × 90 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3134ef00-fb5e-50ec-85bb-9ffdac7ce2e7', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '160 × 110 mm', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('13a960ab-ec69-5e1d-8af5-92db86df4f12', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '200 × 90 mm', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b63066eb-0dcc-548b-a77d-45c3858551c9', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '200 × 110 mm', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2a664570-55f6-5af8-9761-0184870c045e', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '200 × 160 mm', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4b78083f-7f4a-5af2-a5fb-1e60730939a9', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '250 × 90 mm', NULL, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3ab0f69c-e7c7-5c3b-9b20-cdce8651e4bc', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '250 × 110 mm', NULL, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c40cdfea-44a6-525a-a057-adbf9fa5e667', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '250 × 160 mm', NULL, NULL, 15)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f49e3f5f-af5f-5cc9-a6ce-7149e0d9ae5a', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '250 × 200 mm', NULL, NULL, 16)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('28703d03-7f20-5f6b-8641-173142ee2a84', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '315 × 90 mm · SDR17', NULL, NULL, 17)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('73e82851-be03-5215-839e-2b70e48e6ae5', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '315 × 110 mm · SDR17', NULL, NULL, 18)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2c3a902e-82c3-52b4-9b97-a8383aba4838', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '315 × 160 mm · SDR17', NULL, NULL, 19)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3adb3233-0b68-516a-a12c-3497537b2114', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '315 × 200 mm · SDR17', NULL, NULL, 20)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c09bc7ab-cee7-5d2c-a7f4-4d62e75e1293', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '315 × 250 mm · SDR17', NULL, NULL, 21)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('beac552f-8a02-57c0-98fa-43484d2f8069', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '355 × 110 mm · SDR17', NULL, NULL, 22)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('fc9714d2-d347-50cf-be2c-87c8b46849ae', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '355 × 160 mm · SDR17', NULL, NULL, 23)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3aa6e9c8-761f-5435-bb3a-757cd561b1fc', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '355 × 200 mm · SDR17', NULL, NULL, 24)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a0a68c2b-8f4b-553d-8ec4-a06f899aa66e', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '355 × 250 mm · SDR17', NULL, NULL, 25)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0a2f3d98-e6b6-5111-b436-f86e0f99e082', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '355 × 315 mm · SDR17', NULL, NULL, 26)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e7b1ce6c-90da-5441-b838-e025273d70e0', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '400 × 110 mm · SDR17', NULL, NULL, 27)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('97c0475b-a864-5d18-badc-4bf64d030fa5', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '400 × 160 mm · SDR17', NULL, NULL, 28)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2214d46b-0e42-505e-adce-cc6e186efb7a', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '400 × 200 mm · SDR17', NULL, NULL, 29)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0d36cde2-a4d1-50ad-a3c2-178f0c445ace', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '400 × 250 mm · SDR17', NULL, NULL, 30)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f95ee7fe-8f1d-5428-9cde-d92f44fd92bd', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '400 × 315 mm · SDR17', NULL, NULL, 31)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('aab1488d-2ff4-5609-bc9a-4019d2bc893f', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '400 × 355 mm · SDR17', NULL, NULL, 32)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b4ab13c6-6a5b-50e4-9e59-384a8f981120', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '450 × 110 mm · SDR17', NULL, NULL, 33)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d7d63899-2142-5778-8f5d-85aa6cfa7728', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '450 × 160 mm · SDR17', NULL, NULL, 34)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('314ae3c6-4e98-558d-957d-7560e4b2dbc4', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '450 × 200 mm · SDR17', NULL, NULL, 35)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2e7b5a06-5e5e-5938-963c-351a56b159ea', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '450 × 250 mm · SDR17', NULL, NULL, 36)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('c33f0146-c5c9-576e-beee-bc983d58beec', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '450 × 315 mm · SDR17', NULL, NULL, 37)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e116fb3d-1f88-58bb-b270-7e7f4fa7f423', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '450 × 355 mm · SDR17', NULL, NULL, 38)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('55809d63-628f-5093-a3f5-d2ab7787a8a6', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '450 × 400 mm · SDR17', NULL, NULL, 39)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b5e9a491-e51a-517b-80ad-9bf30341acbf', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 110 mm · SDR17', NULL, NULL, 40)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('06195458-dd85-58d0-a725-a9290fbec92a', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 160 mm · SDR17', NULL, NULL, 41)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f4a994d5-f216-57ed-b024-39fe96465b64', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 200 mm · SDR17', NULL, NULL, 42)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('dc4c74df-bb75-5b95-89cc-5e4c3ffd67c8', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 250 mm · SDR17', NULL, NULL, 43)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('da07b7cd-944e-5a75-8bb9-964d9e74b548', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 315 mm · SDR17', NULL, NULL, 44)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5adb7625-3a22-5ad8-8e44-bad965b8a0ad', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 355 mm · SDR17', NULL, NULL, 45)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('41f03609-acbd-5c53-8f6c-e06cbd5e6e62', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 400 mm · SDR17', NULL, NULL, 46)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ebfc4e0a-2ac0-53b5-89be-c45e60799d90', 'c0f89392-5e5d-5bc2-bf72-e6b25eeec599', NULL, '500 × 450 mm · SDR17', NULL, NULL, 47)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('353c5449-375e-50c3-8f47-f374859faca9', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'cruz-hdpe-termofusion-sdr11', NULL, 'Cruz HDPE termofusión SDR11', 'Cruz de cuatro salidas para unión por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11"}]'::jsonb, '[{"url":"/media/catalog/hdpe-cruz.webp","alt":"Cruz HDPE termofusión SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 3', false, 'PUBLISHED',
  'cruz hdpe termofusion sdr11 cruz de cuatro salidas para union por termofusion. hdpe hdpe hdpe 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('053f84e4-971e-5bf0-b8f7-1f798e0a9023', '353c5449-375e-50c3-8f47-f374859faca9', NULL, '90 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('172f0b86-ce32-5874-b816-1321745d93ae', '353c5449-375e-50c3-8f47-f374859faca9', NULL, '110 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('bbdd63c5-61c5-540b-a933-740412e67fae', '353c5449-375e-50c3-8f47-f374859faca9', NULL, '160 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('669448d3-32d7-5b61-8a27-ff60434846d7', '353c5449-375e-50c3-8f47-f374859faca9', NULL, '200 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6c0cee3d-c33c-5d46-a94d-b187516dd4b0', '353c5449-375e-50c3-8f47-f374859faca9', NULL, '250 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b632abcd-1027-57ba-9fe8-f527c4054a05', '353c5449-375e-50c3-8f47-f374859faca9', NULL, '315 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('1c92f0d5-7ce8-59d7-a75a-96670e24f203', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'yee-hdpe-termofusion-sdr11', NULL, 'Yee HDPE termofusión SDR11', 'Yee para derivaciones inclinadas por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11"}]'::jsonb, '[{"url":"/media/catalog/hdpe-yee.webp","alt":"Yee HDPE termofusión SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 3', false, 'PUBLISHED',
  'yee hdpe termofusion sdr11 yee para derivaciones inclinadas por termofusion. hdpe hdpe hdpe 63 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a1573abe-013c-5f5b-beab-d254ec29bf7a', '1c92f0d5-7ce8-59d7-a75a-96670e24f203', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e565aa6e-dfd6-5c2d-bdcd-d8b9ba98bb8d', '1c92f0d5-7ce8-59d7-a75a-96670e24f203', NULL, '90 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1b943cb8-ff52-5283-b985-a613dec579a0', '1c92f0d5-7ce8-59d7-a75a-96670e24f203', NULL, '110 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('9f0c50ec-da30-529b-bdbe-6174b29feaa5', '1c92f0d5-7ce8-59d7-a75a-96670e24f203', NULL, '160 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f20e68c5-e43c-5da3-a104-95e59d9a411f', '1c92f0d5-7ce8-59d7-a75a-96670e24f203', NULL, '200 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('09b4c753-5061-500b-a0b1-bb284b5f5c64', '1c92f0d5-7ce8-59d7-a75a-96670e24f203', NULL, '250 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ac780d9a-5bbc-5e95-ba7b-e7d63f3540f4', '1c92f0d5-7ce8-59d7-a75a-96670e24f203', NULL, '315 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('3c2c8abc-ef85-5f5b-86ad-82d722bd214e', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'reduccion-hdpe-termofusion-sdr11', NULL, 'Reducción HDPE termofusión SDR11', 'Reducción concéntrica para cambiar de diámetro por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Unión","value":"Termofusión"},{"label":"Medida","value":"Mayor × menor (mm)"}]'::jsonb, '[{"url":"/media/catalog/hdpe-reduccion.webp","alt":"Reducción HDPE termofusión SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 3', false, 'PUBLISHED',
  'reduccion hdpe termofusion sdr11 reduccion concentrica para cambiar de diametro por termofusion. hdpe hdpe hdpe 75 × 63 mm 90 × 63 mm 90 × 75 mm 110 × 63 mm 110 × 75 mm 110 × 90 mm 160 × 63 mm 160 × 90 mm 160 × 110 mm 200 × 110 mm 200 × 160 mm 250 × 160 mm 250 × 200 mm 315 × 200 mm 315 × 250 mm 355 × 160 mm 355 × 200 mm 355 × 250 mm 355 × 315 mm 400 × 160 mm 400 × 200 mm 400 × 250 mm 400 × 315 mm 400 × 355 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('87d82206-89a4-5161-bde1-de514da670ff', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '75 × 63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('449f2abc-65c2-5794-b325-92e9a76a9ae6', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '90 × 63 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7ebf89cc-efa0-5cce-a150-c33a75abc5c9', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '90 × 75 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('971588cb-4216-58f3-b6b6-9bd65bbc9529', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '110 × 63 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b8213f6d-655b-5a76-9e35-d95ecb35f161', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '110 × 75 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7f9baafc-7422-5af3-88b5-9765d84b52e0', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '110 × 90 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f7ec8970-6a79-571f-9f77-643eeb3f5f67', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '160 × 63 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1b710047-47f2-5845-af9f-e644a7bafa94', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '160 × 90 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('182e4ba5-e028-578b-ae2a-263ac82e43ab', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '160 × 110 mm', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ab36dc9a-504a-51a1-a8b1-82343a97076c', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '200 × 110 mm', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4d964019-13b2-535c-883d-216828f5eb99', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '200 × 160 mm', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('744203c9-3b5e-50ff-b0d0-1b0d1dba70af', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '250 × 160 mm', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('12494d37-d392-5f08-95bf-abf0ea8228f8', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '250 × 200 mm', NULL, NULL, 13)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('29106878-ad5c-5bbf-9ce7-113b020b5a3d', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '315 × 200 mm', NULL, NULL, 14)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('63c75be0-a1b2-56ec-9455-04422a2602e6', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '315 × 250 mm', NULL, NULL, 15)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f4c418e6-8bb0-5d56-9d3e-5629000321a7', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '355 × 160 mm', NULL, NULL, 16)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f1bdb926-3fac-54d2-bfe9-622ffe4ba420', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '355 × 200 mm', NULL, NULL, 17)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0956e7e6-23a6-519e-8730-14a651fe5209', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '355 × 250 mm', NULL, NULL, 18)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('70cf5b14-ed11-5153-ab55-9770e35fe286', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '355 × 315 mm', NULL, NULL, 19)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e5a9554b-cf0e-5168-bcda-9bfa5c84df40', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '400 × 160 mm', NULL, NULL, 20)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8f3a422b-b97c-5cfd-90d3-8dac73dffced', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '400 × 200 mm', NULL, NULL, 21)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4a05d582-d198-5ff2-8ed1-e98b5ca10c7f', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '400 × 250 mm', NULL, NULL, 22)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6fee9304-524f-55cd-9c23-719d76612c32', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '400 × 315 mm', NULL, NULL, 23)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('57ac16e4-33fb-5173-8569-143071a48bc4', '3c2c8abc-ef85-5f5b-86ad-82d722bd214e', NULL, '400 × 355 mm', NULL, NULL, 24)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('59f4cf20-5136-5bf7-9258-57626f602d50', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'tapon-hdpe-termofusion-sdr11', NULL, 'Tapón HDPE termofusión SDR11', 'Tapón para cerrar el extremo de una línea HDPE por termofusión.', 'hdpe',
  'HDPE', NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Material","value":"HDPE"},{"label":"Unión","value":"Termofusión"},{"label":"Serie","value":"SDR11"}]'::jsonb, '[{"url":"/media/catalog/hdpe-tapon.webp","alt":"Tapón HDPE termofusión SDR11","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, páginas 3 y 4', false, 'PUBLISHED',
  'tapon hdpe termofusion sdr11 tapon para cerrar el extremo de una linea hdpe por termofusion. hdpe hdpe hdpe 63 mm 75 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm 355 mm 400 mm 450 mm 500 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('6aa435a7-fd85-5a81-a962-098e79de2c22', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e23f0882-74c8-578f-a9c2-05e30685a5c7', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '75 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('abfd2762-5484-541d-8b1f-4f1a0b4e7e3a', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '90 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('396f01ba-adff-5167-968d-46bbd6aa4de7', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '110 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a9180a3e-170d-5ce5-bda3-594112937e39', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '160 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('59530980-297c-574d-8005-410907e3e2d9', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '200 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('4a4069b8-c45c-51fe-a9f1-34cb59cd0dda', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '250 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('73f0a4e6-453b-599c-a174-b03dc0727bf0', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '315 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('5213cefa-a09f-56b7-a338-9355cccb12fe', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '355 mm', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b7be6bd5-9e42-501d-b374-f9dd54fab0a5', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '400 mm', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ae178628-48f1-5f1a-bfde-399e93b3c109', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '450 mm', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d52b7cd4-d138-571e-ae23-755b12f028ed', '59f4cf20-5136-5bf7-9258-57626f602d50', NULL, '500 mm', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('191318b1-6e46-54db-b9be-7d6bb1ad92d7', (SELECT id FROM product_categories WHERE slug = 'hdpe'), 'brida-backing-ring-sdr17-pn16', NULL, 'Brida tipo backing ring SDR17 PN16', 'Brida suelta tipo backing ring para flange HDPE.', 'hdpe',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Serie","value":"SDR17"},{"label":"Presión","value":"PN16"},{"label":"Uso","value":"Con flange HDPE termofusión"}]'::jsonb, '[{"url":"/media/catalog/hdpe-brida-backing.webp","alt":"Brida tipo backing ring SDR17 PN16","illustrative":false}]'::jsonb, 'PRECIOS HDPE TERMO.pdf, página 4', false, 'PUBLISHED',
  'brida tipo backing ring sdr17 pn16 brida suelta tipo backing ring para flange hdpe. hdpe hdpe 63 mm 75 mm 90 mm 110 mm 160 mm 200 mm 250 mm 315 mm 355 mm 400 mm 450 mm 500 mm', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('30f37a37-f5b0-57b8-8cf0-67981f295d6f', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '63 mm', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('21955140-1179-57bc-bbc5-ba1563f385ba', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '75 mm', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('350eac90-510e-5534-b99c-2175401d83d5', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '90 mm', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b3204010-e2aa-5798-b5b1-2acb4b0dd599', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '110 mm', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('18c86ca9-9800-5399-ac10-ab3151c44ea4', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '160 mm', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('12788025-3956-50fd-944c-5a96b5bec804', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '200 mm', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3618e983-5dbc-56b0-8799-e15a3a421297', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '250 mm', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('8573d24a-07e1-5f1b-9e8f-43b26d3c03ee', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '315 mm', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ea5d5573-5ec1-520e-be00-d560b16c68d1', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '355 mm', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cc817a36-dc28-55a4-bc18-8408a6c36086', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '400 mm', NULL, NULL, 10)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('012af64c-478f-56ec-ae3c-e0aebca3e21b', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '450 mm', NULL, NULL, 11)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('2bc151e9-0da7-50ea-b929-166f169f795f', '191318b1-6e46-54db-b9be-7d6bb1ad92d7', NULL, '500 mm', NULL, NULL, 12)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('b2d065ca-ad09-5558-bb26-6fa5f41a28b1', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-termofusion-hdl-4-mordazas', NULL, 'Máquina de termofusión HDL, 4 mordazas, timón', 'Máquina manual de soldar PE con cuatro mordazas y timón.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje","value":"220 V"},{"label":"Placa calefactora máxima","value":"270 °C"},{"label":"Mordazas","value":"4"}]'::jsonb, '[{"url":"/media/catalog/maq-hdl-4m.webp","alt":"Máquina de termofusión HDL, 4 mordazas, timón","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 1', false, 'PUBLISHED',
  'maquina de termofusion hdl, 4 mordazas, timon maquina manual de soldar pe con cuatro mordazas y timon. equipos equipos hdl 160-4m · 40–160 mm · 53 kg hdl 200-4m · 63–200 mm · 67 kg hdl 250-4m · 63–250 mm · 131 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('1e9fbbba-b093-5572-a897-35775efea771', 'b2d065ca-ad09-5558-bb26-6fa5f41a28b1', NULL, 'HDL 160-4M · 40–160 mm · 53 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('cf697938-35c3-579b-b3ca-f16eab2050ad', 'b2d065ca-ad09-5558-bb26-6fa5f41a28b1', NULL, 'HDL 200-4M · 63–200 mm · 67 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d1118175-1e2b-598a-b36d-50b07e30e7c0', 'b2d065ca-ad09-5558-bb26-6fa5f41a28b1', NULL, 'HDL 250-4M · 63–250 mm · 131 kg', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('458e7040-5d9b-5414-a02a-79eefbbfb0e6', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-termofusion-hdt-palanca', NULL, 'Máquina de termofusión HDT, 2 mordazas, palanca', 'Máquina manual de soldar PE con dos mordazas y accionamiento por palanca.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje","value":"220 V"},{"label":"Placa calefactora máxima","value":"270 °C"},{"label":"Mordazas","value":"2"}]'::jsonb, '[{"url":"/media/catalog/maq-hdt.webp","alt":"Máquina de termofusión HDT, 2 mordazas, palanca","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 1', false, 'PUBLISHED',
  'maquina de termofusion hdt, 2 mordazas, palanca maquina manual de soldar pe con dos mordazas y accionamiento por palanca. equipos equipos hdt 160-2m · 40–160 mm · 38 kg hdt 200-2m · 63–200 mm · 43 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('05c59c8b-5f56-51c9-94b5-413d09448845', '458e7040-5d9b-5414-a02a-79eefbbfb0e6', NULL, 'HDT 160-2M · 40–160 mm · 38 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3fc60cd0-a6fa-5efd-a565-45c059cb3ce7', '458e7040-5d9b-5414-a02a-79eefbbfb0e6', NULL, 'HDT 200-2M · 63–200 mm · 43 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('6c9b39ab-1566-5afd-8bdb-0d80e07bb988', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-termofusion-hdy-manivela', NULL, 'Máquina de termofusión HDY, 4 mordazas, manivela', 'Máquina manual de soldar PE con cuatro mordazas, manivela y tres bielas de fuerza.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje","value":"220 V"},{"label":"Placa calefactora máxima","value":"270 °C"},{"label":"Mordazas","value":"4"}]'::jsonb, '[{"url":"/media/catalog/maq-hdy.webp","alt":"Máquina de termofusión HDY, 4 mordazas, manivela","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 1', false, 'PUBLISHED',
  'maquina de termofusion hdy, 4 mordazas, manivela maquina manual de soldar pe con cuatro mordazas, manivela y tres bielas de fuerza. equipos equipos hdy 160-4m · 40–160 mm · 45 kg hdy 200-4m · 63–200 mm · 50 kg hdy 250-4m · 63–250 mm · 80 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f1d2dd15-de9f-5447-8c4f-0a71a2a9ebf2', '6c9b39ab-1566-5afd-8bdb-0d80e07bb988', NULL, 'HDY 160-4M · 40–160 mm · 45 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e83ef348-96b2-5574-9d73-0154f748f1ee', '6c9b39ab-1566-5afd-8bdb-0d80e07bb988', NULL, 'HDY 200-4M · 63–200 mm · 50 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('a82262c9-33e7-5006-b1c8-f9842af9db20', '6c9b39ab-1566-5afd-8bdb-0d80e07bb988', NULL, 'HDY 250-4M · 63–250 mm · 80 kg', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('10006b1e-776e-5875-8934-3b0d5899094a', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-termofusion-serie-hdc', NULL, 'Máquina de termofusión serie HDC', 'Serie de máquinas de soldar PE para rangos de 40 a 630 mm.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje","value":"220 V"},{"label":"Placa calefactora máxima","value":"270 °C"},{"label":"Rango de operación","value":"40 a 630 mm según modelo"}]'::jsonb, '[{"url":"/media/catalog/maq-hdc.webp","alt":"Máquina de termofusión serie HDC","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 2', false, 'PUBLISHED',
  'maquina de termofusion serie hdc serie de maquinas de soldar pe para rangos de 40 a 630 mm. equipos equipos hdc 160 · 40–160 mm · 111 kg hdc 200 · 63–200 mm · 117 kg hdc 250 · 63–250 mm · 148 kg hdc 315 · 63–315 mm · 188 kg hdc 355 · 90–355 mm · 245 kg hdc 400 · 160–400 mm · 370 kg hdc 450 · 180–450 mm · 400 kg hdc 500 · 180–500 mm · 480 kg hdc 630 · 315–630 mm · 645 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0d3a26e5-911d-5361-a23f-bd1231a0d11e', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 160 · 40–160 mm · 111 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('077ecd6b-9a6c-5e29-aa3c-44e973390fdc', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 200 · 63–200 mm · 117 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('e996e292-ad1f-57c4-a3b1-706b7fb21e8a', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 250 · 63–250 mm · 148 kg', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ca793638-0a89-5a51-a041-c47fb7358e88', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 315 · 63–315 mm · 188 kg', NULL, NULL, 4)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('b91422c2-32bb-5a59-9ada-747f7f3671c0', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 355 · 90–355 mm · 245 kg', NULL, NULL, 5)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('90dce58a-8367-5897-b619-6d10d175d513', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 400 · 160–400 mm · 370 kg', NULL, NULL, 6)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('d6c878d5-c3c8-583b-9258-772b45f43c91', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 450 · 180–450 mm · 400 kg', NULL, NULL, 7)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('3f2c7317-8223-58b8-87ba-3c6608bacde5', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 500 · 180–500 mm · 480 kg', NULL, NULL, 8)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('632ac6f9-b66b-5818-a137-0d48e253e8a5', '10006b1e-776e-5875-8934-3b0d5899094a', NULL, 'HDC 630 · 315–630 mm · 645 kg', NULL, NULL, 9)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('fef60d0d-1fae-5dd8-98e4-94361221b7dc', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-electrofusion-hdm', NULL, 'Máquina de electrofusión HDM', 'Equipo de electrofusión para accesorios con resistencia integrada.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje de entrada","value":"190–240 V"},{"label":"Voltaje de salida","value":"10–48 V"}]'::jsonb, '[{"url":"/media/catalog/maq-hdm.webp","alt":"Máquina de electrofusión HDM","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 3', false, 'PUBLISHED',
  'maquina de electrofusion hdm equipo de electrofusion para accesorios con resistencia integrada. equipos equipos hdm 200 · 20–200 mm · 20 kg hdm 315 · 20–315 mm · 20 kg hdm 500 · 20–500 mm · 25 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('67401052-3529-5ece-8fd4-ac5f48a10ec3', 'fef60d0d-1fae-5dd8-98e4-94361221b7dc', NULL, 'HDM 200 · 20–200 mm · 20 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('41eff975-8a00-56f9-83ca-c088375ecdbf', 'fef60d0d-1fae-5dd8-98e4-94361221b7dc', NULL, 'HDM 315 · 20–315 mm · 20 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ea5be143-631d-51ed-991c-f4855764e8ac', 'fef60d0d-1fae-5dd8-98e4-94361221b7dc', NULL, 'HDM 500 · 20–500 mm · 25 kg', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('373b3828-45b3-5026-828a-570132161c67', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-termofusion-sud-sdp-4-mordazas', NULL, 'Máquina de termofusión SUD / SDP, 4 mordazas', 'Máquinas manuales de soldar PE con cuatro mordazas, con manivela o palanca.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje","value":"220 V"},{"label":"Placa calefactora máxima","value":"270 °C"},{"label":"Mordazas","value":"4"}]'::jsonb, '[{"url":"/media/catalog/maq-sdp-4m.webp","alt":"Máquina de termofusión SUD / SDP, 4 mordazas","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 3', false, 'PUBLISHED',
  'maquina de termofusion sud / sdp, 4 mordazas maquinas manuales de soldar pe con cuatro mordazas, con manivela o palanca. equipos equipos sud 40-200m4 · manivela · 40–200 mm · 42,5 kg sdp40-200m4 · palanca · 40–200 mm · 45 kg sdp40-160m4 · palanca · 40–200 mm segun documento · 45 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('7c8dbfe4-d325-5aac-95e0-fbc55affe8a3', '373b3828-45b3-5026-828a-570132161c67', NULL, 'SUD 40-200M4 · manivela · 40–200 mm · 42,5 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('ccc1e01b-bc43-549d-87f7-e68aa65c69b7', '373b3828-45b3-5026-828a-570132161c67', NULL, 'SDP40-200M4 · palanca · 40–200 mm · 45 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('f6adf21e-0233-5a23-a473-9ddaf44ee5d8', '373b3828-45b3-5026-828a-570132161c67', NULL, 'SDP40-160M4 · palanca · 40–200 mm según documento · 45 kg', NULL, NULL, 3)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO products (id, category_id, slug, sku, name, summary, family, material, joint_type, dn_min_mm, dn_max_mm,
  pressure_ratings, standards, homologations, specs, images, source_reference, data_verified, status, search_text,
  published_at, created_at, updated_at)
VALUES ('1e597c6e-1223-52f6-8d26-f37b96d04fd7', (SELECT id FROM product_categories WHERE slug = 'equipos'), 'maquina-termofusion-sdp-2-mordazas', NULL, 'Máquina de termofusión SDP, 2 mordazas, palanca', 'Máquina manual de soldar PE con dos mordazas y palanca.', 'equipos',
  NULL, NULL, NULL, NULL, '{}'::smallint[],
  '{}'::text[], '{}'::text[], '[{"label":"Voltaje","value":"220 V"},{"label":"Placa calefactora máxima","value":"270 °C"},{"label":"Mordazas","value":"2"}]'::jsonb, '[{"url":"/media/catalog/maq-sdp-2m.webp","alt":"Máquina de termofusión SDP, 2 mordazas, palanca","illustrative":false}]'::jsonb, 'PRECIO Y STOCK REAL - IMPORTACION 1511.pdf, página 3', false, 'PUBLISHED',
  'maquina de termofusion sdp, 2 mordazas, palanca maquina manual de soldar pe con dos mordazas y palanca. equipos equipos sdp40-160m2 · 40–160 mm · 53 kg sdp40-200m2 · 40–200 mm · 40 kg', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('928407d2-8054-5016-9ade-5a07c6e82888', '1e597c6e-1223-52f6-8d26-f37b96d04fd7', NULL, 'SDP40-160M2 · 40–160 mm · 53 kg', NULL, NULL, 1)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
VALUES ('0bc1f7a7-80dc-55c9-ae0e-85bb89260288', '1e597c6e-1223-52f6-8d26-f37b96d04fd7', NULL, 'SDP40-200M2 · 40–200 mm · 40 kg', NULL, NULL, 2)
ON CONFLICT (product_id, label) DO NOTHING;

INSERT INTO industry_sectors (id, slug, name, description, position)
VALUES ('1d0f1bd7-6787-594b-a392-9b5040e8d876', 'saneamiento', 'Saneamiento', 'Redes de agua potable y alcantarillado que requieren conducción, control y medición confiables.', 1)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('f1721e67-98ff-58a9-9b49-3c1f5af783ec', '1d0f1bd7-6787-594b-a392-9b5040e8d876') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('a5190b33-7e02-5d44-a79f-d087f6832a76', '1d0f1bd7-6787-594b-a392-9b5040e8d876') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('49183a35-33f1-5a0a-aa7c-c9d1220ddf93', '1d0f1bd7-6787-594b-a392-9b5040e8d876') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('a997e1b5-45ec-5bf7-82e0-b090afad0143', '1d0f1bd7-6787-594b-a392-9b5040e8d876') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('2a6bebd4-b4c2-5363-a167-a8d9490e9b78', '1d0f1bd7-6787-594b-a392-9b5040e8d876') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('51b16ed9-0d23-55f1-8830-a9b828e6dabf', '1d0f1bd7-6787-594b-a392-9b5040e8d876') ON CONFLICT DO NOTHING;

INSERT INTO industry_sectors (id, slug, name, description, position)
VALUES ('0313d5a4-d676-566b-8fc9-4602335d469c', 'mineria', 'Minería', 'Conducción de agua y procesos en operaciones donde las líneas deben resistir uso exigente.', 2)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('b471ff7f-bf3f-5316-b15c-0acd7ff21509', '0313d5a4-d676-566b-8fc9-4602335d469c') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('7334f4f8-391d-5513-98a5-d435afe5cefb', '0313d5a4-d676-566b-8fc9-4602335d469c') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('10006b1e-776e-5875-8934-3b0d5899094a', '0313d5a4-d676-566b-8fc9-4602335d469c') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('5c0e4a07-364f-590f-9ea1-7d7e281943de', '0313d5a4-d676-566b-8fc9-4602335d469c') ON CONFLICT DO NOTHING;

INSERT INTO industry_sectors (id, slug, name, description, position)
VALUES ('16998ac0-46d2-5718-a6ec-b47acc3cf898', 'agricultura', 'Agricultura', 'Sistemas de riego y distribución de agua en campo, con control de aire, presión y filtrado.', 3)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('bdcd0302-838b-56be-a376-e270414aa0a7', '16998ac0-46d2-5718-a6ec-b47acc3cf898') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('fe7a30e6-7b6e-5a3d-ac78-234dab2d8e9d', '16998ac0-46d2-5718-a6ec-b47acc3cf898') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('fcc71f3e-2fdb-5c68-8234-1cc9d176ad79', '16998ac0-46d2-5718-a6ec-b47acc3cf898') ON CONFLICT DO NOTHING;

INSERT INTO industry_sectors (id, slug, name, description, position)
VALUES ('701b8180-8a52-5aec-8066-72a357cb57c6', 'industria', 'Industria', 'Líneas de proceso y servicios de planta que necesitan bridas, juntas y válvulas con la clase correcta.', 4)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('f4ce821c-d304-59dd-b50b-6869a6eae17c', '701b8180-8a52-5aec-8066-72a357cb57c6') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('c34937de-57fb-5085-b431-2602811675aa', '701b8180-8a52-5aec-8066-72a357cb57c6') ON CONFLICT DO NOTHING;

INSERT INTO product_sectors (product_id, sector_id) VALUES ('abfa1c82-30b9-58f6-b916-606e0eaa4f09', '701b8180-8a52-5aec-8066-72a357cb57c6') ON CONFLICT DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('5fac81d1-c381-5f2d-8876-b47ab5b66ee7', 'catalogo-general-2026', 'FUND-CAT-2026-REV1', 'Catálogo General Técnico FUNDIGSAC 2026', 'Incluye tablas dimensionales completas, curvas de caudal, presiones de servicio nominal y tolerancias de fundición según norma ISO 2531 y NTP 350.085.', 'CATALOG', ARRAY['tuberias', 'valvulas', 'marcos-y-tapas']::text[], 'ON_REQUEST',
  NULL, NULL, NULL, NULL, 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('c02dd40c-c686-5b1a-bc69-b9a2749537b4', 'manual-instalacion-zanja', 'MAN-INST-STP-04', 'Manual de Manipulación, Instalación en Zanja y Pruebas STP', 'Directivas de manipulación segura de tuberías Tyton, preparación de cama de apoyo, ángulos de deflexión permitidos y protocolos de pruebas hidrostáticas en campo.', 'MANUAL', ARRAY['tuberias']::text[], 'ON_REQUEST',
  NULL, NULL, NULL, NULL, 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('e7ac0ca0-a600-5ce1-bb74-4df320ff1e8c', 'planos-cad-buzones', 'CAD-PACK-BUZ-2026', 'Compendio de Planos Tipo y Secciones CAD para Expedientes', 'Cortes transversales de buzones estándar con marcos D400 y cámaras de válvulas subterráneas en bloques dinámicos DWG y plantillas vectorizadas listas para impresión.', 'DRAWING', ARRAY['marcos-y-tapas', 'valvulas']::text[], 'ON_REQUEST',
  NULL, NULL, NULL, NULL, 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('8aedb3ec-b5e7-55e3-b341-fcb7d3d92120', 'ficha-valvulas-compuerta', 'FT-VAL-F4F5-PN25', 'Ficha Técnica Unificada: Válvulas de Compuerta F4 y F5 PN 16/25', 'Dimensiones cara a cara según DIN EN 558-1 serie 14/15, torques de apriete, compatibilidad con actuadores eléctricos ISO 5210 y recubrimiento epóxico RAL 5005.', 'DATASHEET', ARRAY['valvulas']::text[], 'ON_REQUEST',
  NULL, NULL, NULL, NULL, 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('50794a76-58fb-5237-8951-1c59aaca2c5c', 'guia-golpe-ariete', 'GUIA-HID-TRANS-02', 'Guía de Cálculo Hidráulico y Supresión de Golpe de Ariete', 'Metodología de dimensionamiento de cámaras de aire, válvulas anticipadoras de onda y criterios para protección contra transitorios en líneas de impulsión principales.', 'MANUAL', ARRAY['valvulas', 'tuberias']::text[], 'ON_REQUEST',
  NULL, NULL, NULL, NULL, 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('4a065c9e-ba8f-51cc-b6ab-a838927af940', 'protocolo-ensayos-en124', 'MET-ENSAYO-EN124-CL400', 'Protocolo de Ensayos Metrológicos y de Carga NTP-EN 124', 'Procedimientos de ensayo de carga residual, resistencia de bancos de pruebas para marcos D400/E600 y ensayos destructivos ejecutados en laboratorios acreditados INACAL.', 'DATASHEET', ARRAY['marcos-y-tapas']::text[], 'ON_REQUEST',
  NULL, NULL, NULL, NULL, 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('11ff538d-ba73-5ac9-af58-6b380d4ebd1d', 'ficha-tecnica-valvula-compuerta-bridada', NULL, 'Ficha técnica: válvula de compuerta bridada', NULL, 'DATASHEET', ARRAY['valvulas']::text[], 'DOWNLOAD',
  '/docs/ficha-tecnica-valvula-compuerta-bridada.pdf', 'application/pdf', 5630449, '2ee56739a7038f7c1c3ce40b9615fec1dccab56d5cf6fca97dad5d86505e074f', 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_documents (product_id, document_id) VALUES ('b18ae21a-8aeb-5cde-9e63-ec7d16925d8b', '11ff538d-ba73-5ac9-af58-6b380d4ebd1d') ON CONFLICT DO NOTHING;

INSERT INTO product_documents (product_id, document_id) VALUES ('b7fdb30d-2784-5412-bde9-cea3b1d6adcb', '11ff538d-ba73-5ac9-af58-6b380d4ebd1d') ON CONFLICT DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('ab280c7a-aea7-5239-9c00-d671f6c299c9', 'ficha-tecnica-valvula-compuerta-fundigsac', NULL, 'Ficha técnica: válvula de compuerta bridada FUNDIGSAC', NULL, 'DATASHEET', ARRAY['valvulas']::text[], 'DOWNLOAD',
  '/docs/ficha-tecnica-valvula-compuerta-fundigsac.pdf', 'application/pdf', 4414237, '0a2c86f4c5234cbd538a50bf9836c62d95de283a3de94f459082953751d99ee9', 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_documents (product_id, document_id) VALUES ('ccd28b46-175a-5677-acb6-f4d1ac5085f7', 'ab280c7a-aea7-5239-9c00-d671f6c299c9') ON CONFLICT DO NOTHING;

INSERT INTO product_documents (product_id, document_id) VALUES ('84a9de30-bc4e-54a4-9f37-17a1ea546156', 'ab280c7a-aea7-5239-9c00-d671f6c299c9') ON CONFLICT DO NOTHING;

INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability, file_url, media_type,
  size_bytes, sha256, language, status, published_at, created_at, updated_at)
VALUES ('162776d8-77e5-57a4-8d3f-c218a165d26e', 'ficha-tecnica-valvula-compuerta-fundigsac-ft-hd-002-2024', 'FT-HD-002-2024', 'Ficha técnica FT-HD-002-2024: válvula de compuerta FUNDIGSAC', NULL, 'DATASHEET', ARRAY['valvulas']::text[], 'DOWNLOAD',
  '/docs/ficha-tecnica-valvula-compuerta-fundigsac-ft-hd-002-2024.pdf', 'application/pdf', 519284, '703334e7fa4e58a739de3100519f6e792acc3d3f7d50a5f7ff59210afe2689cb', 'es', 'PUBLISHED', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00', TIMESTAMPTZ '2026-10-16 00:00:00+00')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO product_documents (product_id, document_id) VALUES ('ccd28b46-175a-5677-acb6-f4d1ac5085f7', '162776d8-77e5-57a4-8d3f-c218a165d26e') ON CONFLICT DO NOTHING;
