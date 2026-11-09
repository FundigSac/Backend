package com.fundigsac.shared.web;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Un cuerpo sin longitud declarada (chunked) se corta al superar el límite, se lea byte a byte o por bloques. */
class PayloadLimitFilterTests {

	private static final int LIMIT = 16;

	private final PayloadLimitFilter filter = new PayloadLimitFilter(new WebProperties(List.of(), LIMIT), new ProblemWriter(JsonMapper.builder().build()));

	/** Petición chunked: el contenido existe, pero la longitud no se conoce de antemano. */
	private static MockHttpServletRequest chunked(int size) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/inquiries") {

			@Override
			public int getContentLength() {
				return -1;
			}

			@Override
			public long getContentLengthLong() {
				return -1;
			}

		};
		request.setContent(new byte[size]);
		return request;
	}

	private ServletInputStream streamThrough(MockHttpServletRequest request, MockHttpServletResponse response) throws Exception {
		ServletInputStream[] stream = new ServletInputStream[1];
		FilterChain chain = (req, res) -> stream[0] = ((HttpServletRequest) req).getInputStream();
		this.filter.doFilter(request, response, chain);
		return stream[0];
	}

	@Test
	void aDeclaredOversizedBodyIsRejectedWithoutReadingIt() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/inquiries");
		request.setContent(new byte[LIMIT + 1]);
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean[] called = new boolean[1];

		this.filter.doFilter(request, response, (req, res) -> called[0] = true);

		assertThat(called[0]).isFalse();
		assertThat(response.getStatus()).isEqualTo(413);
		assertThat(response.getContentType()).startsWith("application/problem+json");
		assertThat(response.getContentAsString()).contains("PAYLOAD_TOO_LARGE");
	}

	@Test
	void aBodyAtTheLimitReadsCompletely() throws Exception {
		ServletInputStream stream = streamThrough(chunked(LIMIT), new MockHttpServletResponse());
		assertThat(stream.readAllBytes()).hasSize(LIMIT);
		assertThat(stream.isFinished()).isTrue();
	}

	@Test
	void anUndeclaredOversizedBodyStopsWhenReadInBlocks() throws Exception {
		ServletInputStream stream = streamThrough(chunked(LIMIT + 1), new MockHttpServletResponse());
		byte[] buffer = new byte[64];
		assertThatThrownBy(() -> stream.read(buffer, 0, buffer.length)).isInstanceOf(PayloadLimitFilter.PayloadTooLargeException.class);
	}

	@Test
	void anUndeclaredOversizedBodyStopsWhenReadByteByByte() throws Exception {
		ServletInputStream stream = streamThrough(chunked(LIMIT + 1), new MockHttpServletResponse());
		for (int i = 0; i < LIMIT; i++) {
			assertThat(stream.read()).isZero();
		}
		assertThatThrownBy(stream::read).isInstanceOf(PayloadLimitFilter.PayloadTooLargeException.class);
	}

	@Test
	void theEndOfTheBodyIsReportedAndTheStreamIsReused() throws Exception {
		MockHttpServletRequest request = chunked(2);
		ServletInputStream[] streams = new ServletInputStream[2];
		this.filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
			try {
				streams[0] = ((HttpServletRequest) req).getInputStream();
				streams[1] = ((HttpServletRequest) req).getInputStream();
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		});
		assertThat(streams[0]).isSameAs(streams[1]);
		byte[] buffer = new byte[8];
		assertThat(streams[0].read(buffer, 0, 8)).isEqualTo(2);
		assertThat(streams[0].read(buffer, 0, 8)).isEqualTo(-1);
		assertThat(streams[0].read()).isEqualTo(-1);
		assertThat(streams[0].isReady()).isTrue();
	}

}
