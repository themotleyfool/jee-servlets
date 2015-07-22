package com.fool.servlet;

import java.net.InetAddress;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mockito.Mockito;

import com.fool.servlet.PoweredByResponseHeaderFilter;

public class PoweredByResponseHeaderFilterTests extends TestCase {
	HttpServletRequest request;
	HttpServletResponse response;
	FilterChain chain;
	FilterConfig filterConfig;
	PoweredByResponseHeaderFilter filter;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		chain = Mockito.mock(FilterChain.class);
		filterConfig = Mockito.mock(FilterConfig.class);
		filter = new PoweredByResponseHeaderFilter();
	}
	
	public void testSetsHeader() throws Exception {
		Mockito.when(request.getServerName()).thenReturn("snorkle01.example.com");
		Mockito.when(request.getServerPort()).thenReturn(8087);
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(response).addHeader("X-Powered-By", filter.getHost() + ":8087");
	}
	
	public void testInitOverridesDefaultHeaderName() throws Exception {
		Mockito.when(filterConfig.getInitParameter("header")).thenReturn("X-Served-By");
		
		filter.init(filterConfig);
		
		assertEquals("X-Served-By", filter.getHeaderName());
	}
	
	public void testInitSetsHost() throws Exception {
		filter.init(filterConfig);
		
		assertEquals(InetAddress.getLocalHost().getHostName(), filter.getHost());
	}
}
