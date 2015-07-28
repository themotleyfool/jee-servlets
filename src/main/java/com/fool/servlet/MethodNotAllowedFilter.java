package com.fool.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MethodNotAllowedFilter implements Filter {
	private Set<String> allowedMethods = new HashSet<String>();
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		String allowedMethodsParam = config.getInitParameter("allowedMethods");
		
		if (allowedMethodsParam == null) {
			throw new ServletException(getClass().getSimpleName() + " requires init-param 'allowedMethods'.");
		}
		
		String[] allowedMethods = allowedMethodsParam.toLowerCase().split("\\s*,\\s*");
		this.allowedMethods.addAll(Arrays.asList(allowedMethods));
	}
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (isMethodAllowed(httpRequest.getMethod())) {
			filterChain.doFilter(request, response);	
		} else {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	public boolean isMethodAllowed(String method) {
		if (method == null) return false;
		
		return allowedMethods.contains(method.toLowerCase());
	}
	
	public void addAllowedMethod(String method) {
		this.allowedMethods.add(method.toLowerCase());
	}
}
