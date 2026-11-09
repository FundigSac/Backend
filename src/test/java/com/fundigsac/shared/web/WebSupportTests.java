package com.fundigsac.shared.web;

import java.time.Duration;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import com.fundigsac.shared.domain.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Identificador de petición, límites por IP y lectura de If-Match. */
class WebSupportTests {

	static class Handlers {

		void withoutLimit() {
		}

		@RateLimited("unknown-policy")
		void withUnknownPolicy() {
		}

	}

	private static HandlerMethod handler(String name) throws Exception {
		return new HandlerMethod(new Handlers(), Handlers.class.getDeclaredMethod(name));
	}

	private static String requestIdFor(String incoming) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/catalog/categories");
		if (incoming != null) {
			request.addHeader(RequestIdFilter.HEADER, incoming);
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		String[] during = new String[1];
		new RequestIdFilter().doFilter(request, response, (req, res) -> during[0] = MDC.get(RequestIdFilter.MDC_KEY));
		assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo(during[0]);
		assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
		return during[0];
	}

	@Test
	void aSafeRequestIdIsReused() throws Exception {
		assertThat(requestIdFor("peticion-123.abc")).isEqualTo("peticion-123.abc");
	}

	@Test
	void anUnsafeOrAbsentRequestIdIsReplaced() throws Exception {
		assertThat(requestIdFor("corta")).hasSize(36);
		assertThat(requestIdFor("id con espacios y\nsaltos")).hasSize(36);
		assertThat(requestIdFor(null)).hasSize(36);
	}

	@Test
	void actuatorRequestsAlsoCarryAnIdentifier() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
		MockHttpServletResponse response = new MockHttpServletResponse();
		new RequestIdFilter().doFilter(request, response, (req, res) -> {
		});
		assertThat(response.getHeader(RequestIdFilter.HEADER)).isNotBlank();
	}

	private static RateLimitInterceptor interceptor(boolean enabled) {
		return new RateLimitInterceptor(new RateLimitProperties(enabled, Map.of("uno", new RateLimitProperties.Policy(1, Duration.ofMinutes(1)))));
	}

	@Test
	void disabledLimitsAndOtherHandlersPassThrough() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertThat(interceptor(false).preHandle(request, response, handler("withUnknownPolicy"))).isTrue();
		assertThat(interceptor(true).preHandle(request, response, new Object())).isTrue();
		assertThat(interceptor(true).preHandle(request, response, handler("withoutLimit"))).isTrue();
	}

	@Test
	void aMissingPolicyFailsInsteadOfLeavingTheOperationUnprotected() throws Exception {
		HandlerMethod method = handler("withUnknownPolicy");
		assertThatThrownBy(() -> interceptor(true).preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), method))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("unknown-policy");
	}

	@Test
	void ifMatchAcceptsStrongAndWeakFormats() {
		assertThat(ETags.requireVersion("\"7\"")).isEqualTo(7);
		assertThat(ETags.requireVersion("W/\"7\"")).isEqualTo(7);
		assertThat(ETags.requireVersion(" 7 ")).isEqualTo(7);
		assertThat(ETags.of(7)).isEqualTo("\"7\"");
	}

	@Test
	void ifMatchRejectsMissingAndInvalidValues() {
		assertThatThrownBy(() -> ETags.requireVersion(null)).isInstanceOf(ETags.PreconditionRequiredException.class);
		assertThatThrownBy(() -> ETags.requireVersion("  ")).isInstanceOf(ETags.PreconditionRequiredException.class);
		assertThatThrownBy(() -> ETags.requireVersion("\"abc\"")).isInstanceOf(ValidationException.class);
	}

}
