package com.fool.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.MDC;

public class RequestHeaderMDCFilter implements Filter {
	private final Map<String, String> headers = new HashMap<String, String>();
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		
		try {
			for (String header : headers.keySet()) {
				String value = httpRequest.getHeader(header);
				if (value != null) {
					MDC.put(headers.get(header), value);
				}
			}
			
			filterChain.doFilter(request, response);
		} finally {
			for (String header : headers.values()) {
				MDC.remove(header);
			}
		}
	}

	public Set<String> getHeaders() {
		return Collections.unmodifiableSet(headers.keySet());
	}
	
	public void init(FilterConfig config) throws ServletException {
		String headerParam = config.getInitParameter("headers");
		for (String s : headerParam.split("[,;]")) {
			addHeader(s.trim());
		}
	}

	public void destroy() {
	}

	void addHeader(String header) {
		String[] kv = header.split(":");
		String key = kv[0];
		if (kv.length == 2) {
			key = kv[1];
		}
		headers.put(kv[0], key);
	}
}
