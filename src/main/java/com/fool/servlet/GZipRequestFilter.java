package com.fool.servlet;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class GZipRequestFilter implements Filter
{

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		
		String contentEncoding = httpRequest.getHeader("Content-Encoding");
		if (contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding)) {
			request = new HttpServletRequestWrapper(httpRequest) {
				@Override
				public ServletInputStream getInputStream() throws IOException {
					return new GZIPServletInputStream(new GZIPInputStream(super.getInputStream()));
				}
			};
		}
		filterChain.doFilter(request, response);
	}
	
	public void destroy() {
	}
	
	class GZIPServletInputStream extends ServletInputStream {
		GZIPInputStream stream;

		public GZIPServletInputStream(GZIPInputStream stream) {
			this.stream = stream;
		}

		public int read(byte[] buf, int off, int len) throws IOException {
			return stream.read(buf, off, len);
		}

		public int read(byte[] b) throws IOException {
			return stream.read(b);
		}

		public int read() throws IOException {
			return stream.read();
		}

		public void close() throws IOException {
			stream.close();
		}

		public int available() throws IOException {
			return stream.available();
		}

		public long skip(long n) throws IOException {
			return stream.skip(n);
		}

		public boolean markSupported() {
			return stream.markSupported();
		}

		public void mark(int readlimit) {
			stream.mark(readlimit);
		}

		public void reset() throws IOException {
			stream.reset();
		}
	}
	
}
