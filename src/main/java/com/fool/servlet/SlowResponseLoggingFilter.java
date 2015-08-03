package com.fool.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SlowResponseLoggingFilter implements Filter {
	public static long NANOS_PER_MILLI = 1000000L;
	private static Logger LOG = LogManager.getLogger(SlowResponseLoggingFilter.class);
	
	private long thresholdMillis;

	@Override
	public void init(FilterConfig config) throws ServletException {
		thresholdMillis = Long.parseLong(config.getInitParameter("thresholdMillis"));
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		long mark = System.nanoTime();
		
		try {
			filterChain.doFilter(request, response);
		} finally {
			long elapsedMillis = (System.nanoTime() - mark) / NANOS_PER_MILLI;
			
			if (elapsedMillis < thresholdMillis) {
				return;
			}
			
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			StringBuilder message = new StringBuilder("Request took ");
			message.append(elapsedMillis);
			message.append("ms to complete: ");
			message.append(httpRequest.getRequestURI());
			if (httpRequest.getQueryString() != null) {
				message.append("?");
				message.append(httpRequest.getQueryString());
			}

			LOG.warn(message);
		}
	}

}
