package com.fool.servlet;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

public class MethodNotAllowedFilterTests extends TestCase {
	HttpServletRequest request;
	HttpServletResponse response;
	FilterChain chain;
	FilterConfig filterConfig;
	MethodNotAllowedFilter filter;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		chain = Mockito.mock(FilterChain.class);
		filterConfig = Mockito.mock(FilterConfig.class);
		filter = new MethodNotAllowedFilter();
	}
	
	public void testInitParsesAllowedMethods() throws Exception {
		Mockito.when(filterConfig.getInitParameter("allowedMethods")).thenReturn("POST, options");
		
		filter.init(filterConfig);
		
		assertTrue("isMethodAllowed(\"post\")", filter.isMethodAllowed("post"));
		assertTrue("isMethodAllowed(\"OPTIONS\")", filter.isMethodAllowed("OPTIONS"));
	}
	
	public void testInitThrowsOnMissingParameter() throws Exception {
		Mockito.when(filterConfig.getInitParameter("allowedMethods")).thenReturn(null);
		
		try {
			filter.init(filterConfig);
			fail("Expected ServletException");
		} catch (ServletException ex) {
		}
	}

	public void testInvokesChain() throws Exception {
		Mockito.when(request.getMethod()).thenReturn("get");
		filter.addAllowedMethod("get");
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(chain).doFilter(request, response);
	}

	public void testMethodNotAllowedDoesNotInvokeChain() throws Exception {
		Mockito.when(request.getMethod()).thenReturn("get");
		filter.addAllowedMethod("post");
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(chain, VerificationModeFactory.times(0)).doFilter(request, response);
	}

	public void testMethodNotAllowedSetsResponse() throws Exception {
		Mockito.when(request.getMethod()).thenReturn("get");
		filter.addAllowedMethod("post");
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
}
