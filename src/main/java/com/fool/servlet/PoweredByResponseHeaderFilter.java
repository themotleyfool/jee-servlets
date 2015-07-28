package com.fool.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
	private String host = "localhost";
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.addHeader(headerName, host + ":" + request.getLocalPort());
		
		filterChain.doFilter(request, response);
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		String headerName = filterConfig.getInitParameter("header");
		if (headerName != null && !"".equals(headerName)) {
			this.headerName = headerName;
		}
		
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// ignore and use default "localhost"
		}
				
	}
	
	public void destroy() {
	}
	
	public String getHeaderName() {
		return headerName;
	}
	
	public String getHost() {
		return host;
	}
}
