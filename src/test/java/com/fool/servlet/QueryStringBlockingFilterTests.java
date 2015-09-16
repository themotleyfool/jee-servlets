package com.fool.servlet;

import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

public class QueryStringBlockingFilterTests extends TestCase {
	HttpServletRequest request;
	HttpServletResponse response;
	FilterChain chain;
	FilterConfig filterConfig;
	QueryStringBlockingFilter filter;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		chain = Mockito.mock(FilterChain.class);
		filterConfig = Mockito.mock(FilterConfig.class);
		filter = new QueryStringBlockingFilter();
	}
	
	public void testInitParsesAllowedMethods() throws Exception {
		Mockito.when(filterConfig.getInitParameter("pattern")).thenReturn("ord\\(");
		
		filter.init(filterConfig);
		
		assertTrue("isQueryStringAllowed(<no querystring>)", filter.isQueryStringAllowed(null));
		assertTrue("isQueryStringAllowed(\"OPTIONS\")", filter.isQueryStringAllowed("q=*:*"));
	}
	
	public void testInitThrowsOnMissingParameter() throws Exception {
		Mockito.when(filterConfig.getInitParameter("pattern")).thenReturn(null);
		
		try {
			filter.init(filterConfig);
			fail("Expected ServletException");
		} catch (ServletException ex) {
		}
	}

	public void testInvokesChain() throws Exception {
		Mockito.when(request.getQueryString()).thenReturn("q=*:*");
		filter.setPattern(Pattern.compile("ord"));
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(chain).doFilter(request, response);
	}

	public void testQueryStringAllowedDoesNotInvokeChain() throws Exception {
		Mockito.when(request.getQueryString()).thenReturn("bf=recip(rord(date), 100)");
		filter.setPattern(Pattern.compile(".*ord.*"));
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(chain, VerificationModeFactory.times(0)).doFilter(request, response);
	}

	public void testQueryStringNotAllowedSetsResponse() throws Exception {
		Mockito.when(request.getQueryString()).thenReturn("bf=recip(rord(date), 100)");
		filter.setPattern(Pattern.compile(".*ord.*"));
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
	}
}
