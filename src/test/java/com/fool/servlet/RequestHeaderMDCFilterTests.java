package com.fool.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.log4j.MDC;
import org.mockito.Mockito;

import com.fool.servlet.RequestHeaderMDCFilter;

public class RequestHeaderMDCFilterTests extends TestCase {
	static final String EXAMPLE_HEADER_NAME = "X-Thingy";
	
	HttpServletRequest request;
	HttpServletResponse response;
	MDCCheckingFilterChain chain;
	FilterConfig filterConfig;
	RequestHeaderMDCFilter filter;
	
	static Object observedMDCValue;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		chain = Mockito.mock(MDCCheckingFilterChain.class);
		filterConfig = Mockito.mock(FilterConfig.class);
		filter = new RequestHeaderMDCFilter();
	}
	
	public void testInitParsesHeaderNames() throws Exception {
		Mockito.when(filterConfig.getInitParameter("headers")).thenReturn("accept, content-type");
		
		filter.init(filterConfig);

		assertEquals(new HashSet<String>(Arrays.asList(new String[] {"accept", "content-type"})), filter.getHeaders());
	}

	public void testClearsBindingAfter() throws Exception {
		filter.addHeader(EXAMPLE_HEADER_NAME);
		Mockito.when(request.getHeader(EXAMPLE_HEADER_NAME)).thenReturn("thing value");
		RuntimeException exception = new RuntimeException("chain failure");
		Mockito.doThrow(exception).when(chain).doFilter(request, response);
		
		try {
			filter.doFilter(request, response, chain);
			fail("Expected exception");
		} catch (RuntimeException ex) {
			assertSame(exception, ex);
		}
		
		assertEquals(null, MDC.get(EXAMPLE_HEADER_NAME));
	}
	
	public void testBindsHeaderDuringChain() throws Exception {
		filter.addHeader(EXAMPLE_HEADER_NAME);
		Mockito.when(request.getHeader(EXAMPLE_HEADER_NAME)).thenReturn("thing value");
		Mockito.doCallRealMethod().when(chain).doFilter(request, response);
		
		filter.doFilter(request, response, chain);
		
		assertEquals("thing value", observedMDCValue);
	}
	
	public void testSkipsNullHeaderValue() throws Exception {
		filter.addHeader(EXAMPLE_HEADER_NAME);
		Mockito.when(request.getHeader(EXAMPLE_HEADER_NAME)).thenReturn(null);
		
		filter.doFilter(request, response, chain);
	}
	
	abstract class MDCCheckingFilterChain implements FilterChain {
		public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
			observedMDCValue = MDC.get(EXAMPLE_HEADER_NAME);
		}
	}
}
