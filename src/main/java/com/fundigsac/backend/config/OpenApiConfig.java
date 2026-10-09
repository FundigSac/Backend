package com.fundigsac.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fundigsacOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FUNDIGSAC B2B - Enterprise API")
                        .description("API REST Oficial de FUNDIGSAC para el ecosistema B2B industrial.\n\n" +
                                "Cubre los módulos de:\n" +
                                "- Catálogo Técnico y Fichas de Hierro Dúctil / HDPE\n" +
                                "- Cotizaciones Rápidas B2B con Folio e Idempotencia\n" +
                                "- Libro de Reclamaciones Público (Normativa Indecopi / D.S. 016-2024-JUS)\n" +
                                "- Identidad y Cuentas de Clientes (RBAC)\n" +
                                "- E-Commerce B2B con SKU elegibles y Cálculo de Flete\n" +
                                "- Pasarela de Pagos Izipay (Sandbox & Webhooks firmados)\n" +
                                "- Mesa de Ayuda y Backoffice Administrativo\n" +
                                "- Auditoría, Outbox y Analítica Responsable")
                        .version("v2.0-2026")
                        .contact(new Contact()
                                .name("Equipo de Ingeniería FUNDIGSAC")
                                .email("ventas@fundigsac.com")
                                .url("https://fundigsac.com"))
                        .license(new License()
                                .name("Propietario - FUNDIGSAC S.A.C.")
                                .url("https://fundigsac.com/terminos")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Entorno Local / Docker"),
                        new Server().url("https://staging.fundigsac.com").description("Entorno Staging"),
                        new Server().url("https://api.fundigsac.com").description("Entorno Producción")
                ));
    }
}
