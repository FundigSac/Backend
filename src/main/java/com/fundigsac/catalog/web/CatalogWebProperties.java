package com.fundigsac.catalog.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param filesBaseUrl origen público de los archivos con ruta relativa (/docs/…, /media/…): el sitio web
 */
@ConfigurationProperties("fundigsac.catalog")
record CatalogWebProperties(@DefaultValue("https://fundigsac-web.vercel.app") String filesBaseUrl) {
}
