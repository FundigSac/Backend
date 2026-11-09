package com.fundigsac;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Comportamiento bajo peticiones simultáneas reales: folios sin duplicados, idempotencia y concurrencia optimista. */
class ConcurrencyIT extends IntegrationTest {

	private static final int THREADS = 8;

	/** Lanza todas las tareas a la vez, tras una señal común, para que realmente se solapen. */
	private <T> List<T> runTogether(List<Callable<T>> tasks) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		CountDownLatch start = new CountDownLatch(1);
		try {
			List<Future<T>> futures = new ArrayList<>();
			for (Callable<T> task : tasks) {
				futures.add(pool.submit(() -> {
					start.await();
					return task.call();
				}));
			}
			start.countDown();
			List<T> results = new ArrayList<>();
			for (Future<T> future : futures) {
				results.add(future.get(60, TimeUnit.SECONDS));
			}
			return results;
		}
		finally {
			pool.shutdownNow();
		}
	}

	@Test
	void parallelSubmissionsGetConsecutiveFoliosWithoutDuplicates() throws Exception {
		List<Callable<MvcResult>> tasks = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			tasks.add(() -> this.mvc.perform(postJson("/api/v1/inquiries", ApiFixtures.inquiry("paralelo@correo.pe"))).andReturn());
		}
		List<Integer> numbers = new ArrayList<>();
		for (MvcResult result : runTogether(tasks)) {
			assertThat(result.getResponse().getStatus()).isEqualTo(201);
			String folio = body(result).get("folio").asString();
			numbers.add(Integer.parseInt(folio.substring(folio.lastIndexOf('-') + 1)));
		}
		numbers.sort(Integer::compareTo);
		assertThat(numbers).doesNotHaveDuplicates().hasSize(30);
		assertThat(numbers.get(29) - numbers.get(0)).as("sin huecos en el correlativo").isEqualTo(29);
	}

	@Test
	void parallelRetriesWithTheSameKeyCreateASingleRecord() throws Exception {
		String key = "paralelo-" + ApiFixtures.unique();
		Map<String, Object> body = ApiFixtures.inquiry("idempotente@correo.pe");
		List<Callable<MvcResult>> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			tasks.add(() -> this.mvc.perform(postJson("/api/v1/inquiries", body).header("Idempotency-Key", key)).andReturn());
		}
		List<String> folios = new ArrayList<>();
		for (MvcResult result : runTogether(tasks)) {
			assertThat(result.getResponse().getStatus()).isEqualTo(201);
			folios.add(body(result).get("folio").asString());
		}
		assertThat(folios).hasSize(10).containsOnly(folios.get(0));
		assertThat(count("SELECT count(*) FROM inquiries WHERE folio = :f", Map.of("f", folios.get(0)))).isEqualTo(1);
	}

	@Test
	void parallelEditsWithTheSameVersionLetOnlyOneThrough() throws Exception {
		String admin = adminToken();
		String email = "paralelo.optimista" + ApiFixtures.unique() + "@empresa.pe";
		customerToken(email, "frase-de-paso-muy-larga");
		String id = userId(email);
		String etag = etagOf("/api/v1/staff/accounts/" + id, admin);

		List<Callable<Integer>> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			int attempt = i;
			tasks.add(() -> this.mvc.perform(authed(patchJson("/api/v1/staff/accounts/" + id, Map.of("fullName", "Nombre " + attempt)), admin)
				.header("If-Match", etag)).andReturn().getResponse().getStatus());
		}
		List<Integer> statuses = runTogether(tasks);

		assertThat(statuses).filteredOn((status) -> status == 200).hasSize(1);
		assertThat(statuses).filteredOn((status) -> status == 412).hasSize(9);
		this.mvc.perform(getAs("/api/v1/staff/accounts/" + id, admin)).andExpect(status().isOk());
		assertThat(etagOf("/api/v1/staff/accounts/" + id, admin)).isNotEqualTo(etag);
	}

}
