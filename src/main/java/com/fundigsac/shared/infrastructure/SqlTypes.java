package com.fundigsac.shared.infrastructure;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Conversión explícita entre tipos de Java y de PostgreSQL para los adaptadores JDBC de todos los módulos. */
public final class SqlTypes {

	private SqlTypes() {
	}

	public static @Nullable OffsetDateTime ts(@Nullable Instant instant) {
		return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
	}

	public static @Nullable Instant instant(ResultSet rs, String column) throws SQLException {
		OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
		return value == null ? null : value.toInstant();
	}

	public static @Nullable LocalDate date(ResultSet rs, String column) throws SQLException {
		return rs.getObject(column, LocalDate.class);
	}

	public static @Nullable UUID uuid(ResultSet rs, String column) throws SQLException {
		return rs.getObject(column, UUID.class);
	}

	public static List<String> textArray(ResultSet rs, String column) throws SQLException {
		Array array = rs.getArray(column);
		if (array == null) {
			return List.of();
		}
		Object[] values = (Object[]) array.getArray();
		return Arrays.stream(values).map(String::valueOf).toList();
	}

	public static List<Integer> intArray(ResultSet rs, String column) throws SQLException {
		Array array = rs.getArray(column);
		if (array == null) {
			return List.of();
		}
		Object[] values = (Object[]) array.getArray();
		return Arrays.stream(values).map((v) -> ((Number) v).intValue()).toList();
	}

	public static String[] texts(List<String> values) {
		return values.toArray(String[]::new);
	}

	public static short[] shorts(List<Integer> values) {
		short[] result = new short[values.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = values.get(i).shortValue();
		}
		return result;
	}

	public static @Nullable Integer nullableInt(ResultSet rs, String column) throws SQLException {
		int value = rs.getInt(column);
		return rs.wasNull() ? null : value;
	}

}
