package com.fool.servlet;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QueryStringBlockingFilter implements Filter {
	private Pattern pattern;
	private boolean caseSensitive;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		String patternParam = config.getInitParameter("pattern");
		
		if (patternParam == null) {
			throw new ServletException(getClass().getSimpleName() + " requires init-param 'pattern'.");
		}

		String caseSensitiveParam = config.getInitParameter("caseSensitive");
		
		caseSensitive = (caseSensitiveParam != null && caseSensitiveParam.toLowerCase() == "true");
		
		pattern = Pattern.compile(patternParam, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
	}
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (isQueryStringAllowed(httpRequest.getQueryString())) {
			filterChain.doFilter(request, response);	
		} else {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}

	public boolean isQueryStringAllowed(String queryString) {
		return queryString == null || !pattern.matcher(queryString).matches();
	}
	
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
}
