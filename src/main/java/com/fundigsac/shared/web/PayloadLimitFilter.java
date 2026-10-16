package com.fundigsac.shared.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Límite de tamaño del cuerpo (por defecto 64 KB): se rechaza por Content-Length y también si un cuerpo sin longitud
 * declarada (chunked) supera el límite mientras se lee. Evita agotar memoria con JSON enormes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class PayloadLimitFilter extends OncePerRequestFilter {

	private final long maxBytes;

	private final ProblemWriter problems;

	PayloadLimitFilter(WebProperties properties, ProblemWriter problems) {
		this.maxBytes = properties.maxBodyBytes();
		this.problems = problems;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (request.getContentLengthLong() > this.maxBytes) {
			this.problems.write(response, HttpStatus.CONTENT_TOO_LARGE, "PAYLOAD_TOO_LARGE",
					"El contenido enviado supera el tamaño permitido.");
			return;
		}
		chain.doFilter(new LimitedRequest(request, this.maxBytes), response);
	}

	/** Excepción que ApiExceptionHandler traduce a 413 cuando el límite se supera durante la lectura. */
	public static final class PayloadTooLargeException extends IOException {

		PayloadTooLargeException() {
			super("Payload too large");
		}

	}

	private static final class LimitedRequest extends HttpServletRequestWrapper {

		private final long maxBytes;

		private ServletInputStream stream;

		LimitedRequest(HttpServletRequest request, long maxBytes) {
			super(request);
			this.maxBytes = maxBytes;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if (this.stream == null) {
				ServletInputStream delegate = super.getInputStream();
				this.stream = new ServletInputStream() {

					private long count;

					@Override
					public int read() throws IOException {
						int b = delegate.read();
						if (b != -1 && ++this.count > LimitedRequest.this.maxBytes) {
							throw new PayloadTooLargeException();
						}
						return b;
					}

					@Override
					public int read(byte[] buffer, int off, int len) throws IOException {
						int n = delegate.read(buffer, off, len);
						if (n > 0) {
							this.count += n;
							if (this.count > LimitedRequest.this.maxBytes) {
								throw new PayloadTooLargeException();
							}
						}
						return n;
					}

					@Override
					public boolean isFinished() {
						return delegate.isFinished();
					}

					@Override
					public boolean isReady() {
						return delegate.isReady();
					}

					@Override
					public void setReadListener(ReadListener listener) {
						delegate.setReadListener(listener);
					}
				};
			}
			return this.stream;
		}

	}

}
