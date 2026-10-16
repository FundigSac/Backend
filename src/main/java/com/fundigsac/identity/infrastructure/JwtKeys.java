package com.fundigsac.identity.infrastructure;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.shared.infrastructure.SecretsProperties;

/**
 * Par de claves RSA para firmar (RS256) los JWT. La privada llega como secreto PEM (PKCS#8); la pública se deriva de
 * ella. Firmar con RSA permite que, si un módulo se separa como servicio, valide tokens solo con la clave pública.
 */
@Component
class JwtKeys {

	private static final Logger log = LoggerFactory.getLogger(JwtKeys.class);

	private final RSAKey rsaKey;

	JwtKeys(IdentityProperties properties, SecretsProperties secrets) {
		this.rsaKey = load(properties.jwt().privateKey(), secrets.allowEphemeralKeys());
	}

	RSAKey rsaKey() {
		return this.rsaKey;
	}

	RSAPublicKey publicKey() {
		try {
			return this.rsaKey.toRSAPublicKey();
		}
		catch (JOSEException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static RSAKey load(String pem, boolean allowEphemeral) {
		try {
			KeyPair pair;
			if (pem != null && !pem.isBlank()) {
				pair = fromPem(pem);
			}
			else if (allowEphemeral) {
				log.warn("Sin clave JWT configurada: se usa una clave temporal (solo desarrollo). Las sesiones no sobreviven a un reinicio.");
				KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
				generator.initialize(3072);
				pair = generator.generateKeyPair();
			}
			else {
				throw new IllegalStateException("Falta el secreto jwt_private_key (fundigsac.identity.jwt.private-key)");
			}
			RSAPublicKey publicKey = (RSAPublicKey) pair.getPublic();
			if (publicKey.getModulus().bitLength() < 2048) {
				throw new IllegalStateException("La clave JWT debe tener al menos 2048 bits");
			}
			return new RSAKey.Builder(publicKey).privateKey(pair.getPrivate()).keyIDFromThumbprint().build();
		}
		catch (GeneralSecurityException | JOSEException ex) {
			throw new IllegalStateException("No se pudo cargar la clave JWT", ex);
		}
	}

	private static KeyPair fromPem(String pem) throws GeneralSecurityException {
		String base64 = pem.replaceAll("-----(BEGIN|END) PRIVATE KEY-----", "").replaceAll("\\s", "");
		KeyFactory factory = KeyFactory.getInstance("RSA");
		RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) factory
			.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
		RSAPublicKey publicKey = (RSAPublicKey) factory
			.generatePublic(new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
		return new KeyPair(publicKey, privateKey);
	}

}
