package com.fool.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class PoweredByResponseHeaderFilter implements Filter
{
	private String headerName = "X-Powered-By";
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.addHeader(headerName, request.getServerName() + ":" + request.getServerPort());
		
		filterChain.doFilter(request, response);
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		String headerName = filterConfig.getInitParameter("header");
		if (headerName != null && !"".equals(headerName)) {
			this.headerName = headerName;
		}
	}
	
	public void destroy() {
	}
	
	public String getHeaderName() {
		return headerName;
	}
}
