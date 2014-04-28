package io.mikael.api.hilma.web;

import javax.servlet.*;
import java.io.IOException;

@FunctionalInterface
public interface LambdaFilter extends Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException;

    @Override
    default public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    default public void destroy() {}
}
