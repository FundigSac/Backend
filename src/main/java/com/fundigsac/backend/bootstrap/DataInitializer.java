package com.fundigsac.backend.bootstrap;

import com.fundigsac.backend.model.entity.*;
import com.fundigsac.backend.model.enums.*;
import com.fundigsac.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final TechnicalAttributeRepository attributeRepository;
    private final VariantAttributeValueRepository attributeValueRepository;
    private final TechnicalDocumentRepository documentRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final SitePageRepository pageRepository;
    private final LegalNoticeRepository legalNoticeRepository;
    private final FaqEntryRepository faqEntryRepository;
    private final PriceEntryRepository priceRepository;
    private final InventoryPositionRepository inventoryRepository;
    private final CustomerAccountRepository accountRepository;
    private final CustomerProfileRepository profileRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Base de datos FUNDIGSAC ya inicializada. Omitiendo seed.");
            return;
        }

        log.info("Iniciando sembrado inicial de datos maestros de FUNDIGSAC...");

        // 1. Categorías
        ProductCategory catValvulas = categoryRepository.save(ProductCategory.builder()
                .name("Válvulas de Hierro Dúctil")
                .slug("valvulas")
                .description("Válvulas compuerta, mariposa, retención y control en hierro dúctil GGG50 de alta resistencia.")
                .sortOrder(1)
                .published(true)
                .build());

        ProductCategory catTuberias = categoryRepository.save(ProductCategory.builder()
                .name("Tuberías de Hierro Dúctil y HDPE")
                .slug("tuberias")
                .description("Tuberías para conducción de agua potable, desagüe y minería según norma ISO 2531 / EN 545.")
                .sortOrder(2)
                .published(true)
                .build());

        ProductCategory catTapas = categoryRepository.save(ProductCategory.builder()
                .name("Marcos y Tapas de Hierro Dúctil")
                .slug("marcos-y-tapas")
                .description("Sistemas de coronamiento de calzada y vereda para redes de agua potable y saneamiento clase C250 y D400.")
                .sortOrder(3)
                .published(true)
                .build());

        // 2. Atributos Técnicos
        TechnicalAttribute attrDn = attributeRepository.save(TechnicalAttribute.builder()
                .code("dn").label("Diámetro Nominal (DN)").dataType(AttributeDataType.text).unit("mm").filterable(true).build());
        TechnicalAttribute attrPn = attributeRepository.save(TechnicalAttribute.builder()
                .code("pn").label("Presión Nominal (PN)").dataType(AttributeDataType.text).unit("bar").filterable(true).build());
        TechnicalAttribute attrMaterial = attributeRepository.save(TechnicalAttribute.builder()
                .code("material").label("Material del Cuerpo").dataType(AttributeDataType.text).unit("").filterable(true).build());

        // 3. Productos y Variantes
        // Producto 1: Válvula Compuerta Bridada
        Product p1 = productRepository.save(Product.builder()
                .categoryId(catValvulas.getId())
                .internalCode("VAL-CB-001")
                .name("Válvula Compuerta Bridada")
                .slug("valvula-compuerta-bridada")
                .shortDescription("Válvula de asiento elástico en hierro dúctil para agua potable y saneamiento.")
                .technicalDescription("Cuerpo y bonete en hierro dúctil GGG50. Compuerta vulcanizada con EPDM. Vástago de acero inoxidable AISI 420. Revestimiento epóxico electrostático azul mínimo 250 micras (RAL 5005). Conexión brida según ISO 7005-2 / EN 1092-2.")
                .visibility(ProductVisibility.published)
                .commercialMode(CommercialMode.buy_enabled)
                .approvedBy("INGENIERIA_FUNDIGSAC")
                .approvedAt(Instant.now())
                .build());

        ProductVariant v1_1 = variantRepository.save(ProductVariant.builder()
                .productId(p1.getId())
                .referenceCode("FG-VCB-100-16")
                .dn("100")
                .pn("16")
                .material("Hierro Dúctil GGG50")
                .uom("UND")
                .status(VariantStatus.active)
                .sortOrder(1)
                .build());

        ProductVariant v1_2 = variantRepository.save(ProductVariant.builder()
                .productId(p1.getId())
                .referenceCode("FG-VCB-150-16")
                .dn("150")
                .pn("16")
                .material("Hierro Dúctil GGG50")
                .uom("UND")
                .status(VariantStatus.active)
                .sortOrder(2)
                .build());

        ProductVariant v1_3 = variantRepository.save(ProductVariant.builder()
                .productId(p1.getId())
                .referenceCode("FG-VCB-200-16")
                .dn("200")
                .pn("16")
                .material("Hierro Dúctil GGG50")
                .uom("UND")
                .status(VariantStatus.active)
                .sortOrder(3)
                .build());

        // Precios y Stock iniciales
        priceRepository.save(PriceEntry.builder()
                .variantId(v1_1.getId())
                .currency("PEN")
                .netPrice(new BigDecimal("485.00"))
                .taxClass("IGV_18")
                .build());

        inventoryRepository.save(InventoryPosition.builder()
                .variantId(v1_1.getId())
                .availableQty(new BigDecimal("45.000"))
                .build());

        priceRepository.save(PriceEntry.builder()
                .variantId(v1_2.getId())
                .currency("PEN")
                .netPrice(new BigDecimal("790.00"))
                .taxClass("IGV_18")
                .build());

        inventoryRepository.save(InventoryPosition.builder()
                .variantId(v1_2.getId())
                .availableQty(new BigDecimal("28.000"))
                .build());

        // Producto 2: Válvula Mariposa Excéntrica
        Product p2 = productRepository.save(Product.builder()
                .categoryId(catValvulas.getId())
                .internalCode("VAL-ME-002")
                .name("Válvula Mariposa Excéntrica")
                .slug("valvula-mariposa-excentrica")
                .shortDescription("Válvula mariposa doble excentricidad para conducciones principales de agua.")
                .technicalDescription("Diseño con actuador reductor manual o volante. Disco y cuerpo de hierro dúctil con anillo de cierre en acero inoxidable.")
                .visibility(ProductVisibility.published)
                .commercialMode(CommercialMode.quote_only)
                .approvedBy("INGENIERIA_FUNDIGSAC")
                .approvedAt(Instant.now())
                .build());

        variantRepository.save(ProductVariant.builder()
                .productId(p2.getId())
                .referenceCode("FG-VME-250-16")
                .dn("250")
                .pn("16")
                .material("Hierro Dúctil GGG50")
                .uom("UND")
                .status(VariantStatus.active)
                .sortOrder(1)
                .build());

        // Producto 3: Marco y Tapa para Buzón D400
        Product p3 = productRepository.save(Product.builder()
                .categoryId(catTapas.getId())
                .internalCode("TAP-D400-001")
                .name("Marco y Tapa de Hierro Dúctil Clase D400")
                .slug("marco-y-tapa-hd-clase-d400")
                .shortDescription("Conjunto marco y tapa circular de 600 mm para tránsito pesado normalizado EN 124.")
                .technicalDescription("Hierro Dúctil clase D400 (40 toneladas de resistencia). Junta elástica antirruido de EPDM, sistema de apertura articulada con bloqueo de seguridad a 90°.")
                .visibility(ProductVisibility.published)
                .commercialMode(CommercialMode.buy_enabled)
                .approvedBy("INGENIERIA_FUNDIGSAC")
                .approvedAt(Instant.now())
                .build());

        ProductVariant v3_1 = variantRepository.save(ProductVariant.builder()
                .productId(p3.getId())
                .referenceCode("FG-MT-D400-600")
                .dn("600")
                .pn("D400")
                .material("Hierro Dúctil GGG50")
                .uom("JGO")
                .status(VariantStatus.active)
                .sortOrder(1)
                .build());

        priceRepository.save(PriceEntry.builder()
                .variantId(v3_1.getId())
                .currency("PEN")
                .netPrice(new BigDecimal("340.00"))
                .taxClass("IGV_18")
                .build());

        inventoryRepository.save(InventoryPosition.builder()
                .variantId(v3_1.getId())
                .availableQty(new BigDecimal("120.000"))
                .build());

        // 4. Documentos Técnicos
        TechnicalDocument doc1 = documentRepository.save(TechnicalDocument.builder()
                .title("Ficha Técnica Oficial - Válvula Compuerta Bridada ISO 2531")
                .storageKey("FT_VALVULA_COMPUERTA_BRIDADA_2026.pdf")
                .versionLabel("REV-2026.1")
                .issuedAt(LocalDate.of(2026, 1, 15))
                .status(DocumentStatus.approved)
                .checksumSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .approvedAt(Instant.now())
                .build());

        productDocumentRepository.save(ProductDocument.builder()
                .productId(p1.getId())
                .documentId(doc1.getId())
                .documentType(DocumentType.ficha)
                .build());

        // 5. Páginas del Sitio
        pageRepository.save(SitePage.builder()
                .slug("inicio")
                .title("FUNDIGSAC | Soluciones en Hierro Dúctil y Saneamiento")
                .bodyBlocks("{\"hero\": \"Especialistas en fundición de hierro dúctil, válvulas, tuberías y accesorios para saneamiento, minería e infraestructura nacional.\", \"cta\": \"Cotizar Productos\"}")
                .status(PageStatus.published)
                .publishedAt(Instant.now())
                .build());

        pageRepository.save(SitePage.builder()
                .slug("nosotros")
                .title("Acerca de FUNDIGSAC S.A.C.")
                .bodyBlocks("{\"historia\": \"Empresa peruana líder con más de 20 años suministrando soluciones de fundición dúctil certificadas para las principales obras de agua potable de Sedapal y EPS nacionales.\"}")
                .status(PageStatus.published)
                .publishedAt(Instant.now())
                .build());

        // 6. Textos Legales Aprobados
        legalNoticeRepository.save(LegalNotice.builder()
                .noticeType(LegalNoticeType.privacy)
                .version("v1.0-PE")
                .body("En cumplimiento de la Ley N° 29733 de Protección de Datos Personales del Perú y el D.S. N° 016-2024-JUS, FUNDIGSAC S.A.C. garantiza la confidencialidad de la información proporcionada con finalidades exclusivas de cotización y servicio al cliente.")
                .publishedAt(Instant.now())
                .approvedBy("ASESORIA_LEGAL")
                .build());

        legalNoticeRepository.save(LegalNotice.builder()
                .noticeType(LegalNoticeType.claims)
                .version("v1.0-PE")
                .body("Libro de Reclamaciones conforme al Código de Protección y Defensa del Consumidor de Indecopi. Plazo de atención legal según normativa vigente.")
                .publishedAt(Instant.now())
                .approvedBy("ASESORIA_LEGAL")
                .build());

        // 7. Preguntas Frecuentes
        faqEntryRepository.save(FaqEntry.builder()
                .question("¿Qué normas técnicas cumplen las válvulas de hierro dúctil FUNDIGSAC?")
                .answer("Nuestras válvulas cumplen estrictamente con las normas ISO 2531, ISO 7259, EN 545 y EN 1092-2, con certificación de presión hidrostática al 100% en fábrica.")
                .status(FaqStatus.approved)
                .build());

        faqEntryRepository.save(FaqEntry.builder()
                .question("¿Cómo se calculan los costos de flete para despachos a provincias?")
                .answer("Los despachos en Lima Metropolitana se realizan con tarifa plana para pedidos industriales. Para despachos a provincia se coordina mediante transportista de carga pesada según cubicaje y tonelaje.")
                .status(FaqStatus.approved)
                .build());

        // 8. Cuenta Demo Inicial
        CustomerAccount demoAccount = accountRepository.save(CustomerAccount.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .email("contacto@constructora-andina.pe")
                .passwordHash("BCRYPT_SECURE_HASH_DEMO")
                .status(CustomerAccountStatus.active)
                .emailVerifiedAt(Instant.now())
                .build());

        profileRepository.save(CustomerProfile.builder()
                .customerId(demoAccount.getId())
                .companyName("Constructora Andina S.A.C.")
                .taxId("20549812345")
                .contactPhone("+51 987 654 321")
                .build());

        log.info("Sembrado de datos iniciales FUNDIGSAC completado exitosamente.");
    }
}
