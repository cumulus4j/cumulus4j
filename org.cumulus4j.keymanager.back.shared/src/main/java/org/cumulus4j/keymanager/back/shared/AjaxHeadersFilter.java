package org.cumulus4j.keymanager.back.shared;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter adding headers to allow JavaScript clients to avoid
 * the same origin policy.
 * @author Marc Klinger - mklinger[at]nightlabs[dot]de
 */
public class AjaxHeadersFilter implements Filter {

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (response instanceof HttpServletResponse) {
			final HttpServletResponse r = (HttpServletResponse) response;
			r.setHeader("Allow-Control-Allow-Methods", "POST,PUT,GET,OPTIONS");
			r.setHeader("Access-Control-Allow-Credentials", "true");
			r.setHeader("Access-Control-Allow-Origin", "*");
			r.setHeader("Access-Control-Allow-Headers", "Content-Type,Accept,Authorization");
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
