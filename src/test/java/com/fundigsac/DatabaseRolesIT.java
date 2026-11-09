package com.fundigsac;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Separación de roles real: mismo script de creación de roles y mismas migraciones que Docker, conectando como
 * fundigsac_app. Las pruebas de integración usan un superusuario de Testcontainers y por eso no pueden comprobar esto.
 */
class DatabaseRolesIT {

	private static final String MIGRATOR_PASSWORD = "clave-solo-para-esta-prueba-1";

	private static final String APP_PASSWORD = "clave-solo-para-esta-prueba-2";

	private static final String INSUFFICIENT_PRIVILEGE = "42501";

	/** Privilegios retirados por tabla, según las migraciones. */
	private static final Map<String, List<String>> REVOKED = revoked();

	private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6-alpine3.24"))
		.withDatabaseName("fundigsac")
		.withUsername("fundigsac_admin")
		.withPassword("clave-solo-para-esta-prueba-0")
		.withCopyFileToContainer(MountableFile.forHostPath("docker/postgres/initdb/01-roles.sh", 0755), "/docker-entrypoint-initdb.d/01-roles.sh")
		.withCopyToContainer(Transferable.of(MIGRATOR_PASSWORD), "/run/secrets/db_migrator_password")
		.withCopyToContainer(Transferable.of(APP_PASSWORD), "/run/secrets/db_app_password");

	private static Map<String, List<String>> revoked() {
		List<String> evidence = List.of("UPDATE", "DELETE", "TRUNCATE");
		List<String> noDelete = List.of("DELETE", "TRUNCATE");
		Map<String, List<String>> tables = new LinkedHashMap<>();
		tables.put("audit_events", evidence);
		tables.put("inquiry_events", evidence);
		tables.put("quote_items", evidence);
		tables.put("quote_status_events", evidence);
		tables.put("complaint_events", evidence);
		tables.put("legal_notices", evidence);
		tables.put("consent_records", evidence);
		tables.put("flyway_schema_history", List.of("INSERT", "UPDATE", "DELETE", "TRUNCATE"));
		tables.put("complaints", noDelete);
		tables.put("quote_requests", noDelete);
		tables.put("inquiries", noDelete);
		tables.put("data_subject_requests", noDelete);
		tables.put("products", noDelete);
		tables.put("technical_documents", noDelete);
		tables.put("folio_sequences", noDelete);
		return Map.copyOf(tables);
	}

	@BeforeAll
	static void migrateWithTheMigratorRole() {
		POSTGRES.start();
		Flyway.configure()
			.dataSource(POSTGRES.getJdbcUrl(), "fundigsac_migrator", MIGRATOR_PASSWORD)
			.schemas("fundigsac")
			.defaultSchema("fundigsac")
			.locations("classpath:db/migration")
			.placeholders(Map.of("appRole", "fundigsac_app"))
			.load()
			.migrate();
	}

	@AfterAll
	static void stop() {
		POSTGRES.stop();
	}

	private static Connection app() throws SQLException {
		return DriverManager.getConnection(POSTGRES.getJdbcUrl(), "fundigsac_app", APP_PASSWORD);
	}

	private static boolean hasPrivilege(Connection connection, String table, String privilege) throws SQLException {
		try (Statement statement = connection.createStatement();
				ResultSet row = statement.executeQuery("SELECT has_table_privilege(current_user, 'fundigsac." + table + "', '" + privilege + "')")) {
			row.next();
			return row.getBoolean(1);
		}
	}

	private static void assertDenied(String sql) throws SQLException {
		try (Connection connection = app(); Statement statement = connection.createStatement()) {
			assertThatThrownBy(() -> statement.execute(sql)).as(sql)
				.isInstanceOfSatisfying(SQLException.class, (ex) -> assertThat(ex.getSQLState()).isEqualTo(INSUFFICIENT_PRIVILEGE));
		}
	}

	@Test
	void theAppRoleLacksEveryRevokedPrivilege() throws SQLException {
		try (Connection connection = app()) {
			for (Map.Entry<String, List<String>> table : REVOKED.entrySet()) {
				for (String privilege : table.getValue()) {
					assertThat(hasPrivilege(connection, table.getKey(), privilege)).as("%s en %s", privilege, table.getKey()).isFalse();
				}
			}
		}
	}

	@Test
	void theAppRoleKeepsWhatTheApplicationNeeds() throws SQLException {
		try (Connection connection = app()) {
			assertThat(hasPrivilege(connection, "audit_events", "INSERT")).isTrue();
			assertThat(hasPrivilege(connection, "audit_events", "SELECT")).isTrue();
			assertThat(hasPrivilege(connection, "complaints", "UPDATE")).isTrue();
			assertThat(hasPrivilege(connection, "user_accounts", "DELETE")).isTrue();
		}
	}

	@Test
	void evidenceCannotBeRewrittenEvenWithDirectSql() throws SQLException {
		assertDenied("UPDATE audit_events SET action = 'ALTERADO'");
		assertDenied("DELETE FROM audit_events");
		assertDenied("TRUNCATE consent_records");
		assertDenied("UPDATE legal_notices SET title = 'Alterado'");
		assertDenied("DELETE FROM complaint_events");
		assertDenied("DELETE FROM complaints");
	}

	@Test
	void theAppRoleCannotChangeTheSchema() throws SQLException {
		assertDenied("CREATE TABLE fundigsac.tabla_intrusa (id integer)");
		assertDenied("DROP TABLE audit_events");
		assertDenied("ALTER TABLE audit_events DISABLE TRIGGER ALL");
		assertDenied("CREATE TABLE public.tabla_intrusa (id integer)");
	}

}
