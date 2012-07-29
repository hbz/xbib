package org.xbib.servlet.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Request character encoding filter.
 *
 * <p>This class is used for setting character encoding for ServletRequest object
 * and content-type for ServletResponse object for each request coming from the
 * client. The encoding to be set for the request is by default set to UTF-8.</p>
 *
 *  @author <a href="mailto:prante@hbz-nrw.de">J&ouml;rg Prante</a>
 */
public class RequestCharacterEncodingFilter implements Filter {
    
    private String encoding;

    public RequestCharacterEncodingFilter() {
        this.encoding = null;
    }

    @Override
    public void init(FilterConfig config) {
        this.encoding = config.getInitParameter("encoding");
    }

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        request.setCharacterEncoding(encoding);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {        
    }
}
