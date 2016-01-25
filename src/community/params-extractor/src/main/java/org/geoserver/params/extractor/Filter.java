/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import org.geoserver.filters.GeoServerFilter;
import org.geoserver.platform.ExtensionPriority;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.ResourceStore;
import org.geotools.util.logging.Logging;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Filter implements GeoServerFilter, ExtensionPriority {

    private static final Logger LOGGER = Logging.getLogger(Filter.class);

    private static List<Rule> rules;

    public Filter(ResourceStore dataDirectory) {
        Resource resource = dataDirectory.get("rules.xml");
        rules = Parser.parse(resource.in());
        resource.addListener(notify -> rules = Parser.parse(resource.in()));
    }

    @Override
    public int getPriority() {
        return ExtensionPriority.HIGHEST;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Utils.info(LOGGER, "Start processing request path !");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        UrlTransform urlTransform = new UrlTransform(httpServletRequest.getRequestURI(),
                Optional.ofNullable(httpServletRequest.getQueryString()));
        rules.forEach(rule -> rule.apply(urlTransform));
        chain.doFilter(new Wrapper(urlTransform, httpServletRequest), response);
    }

    @Override
    public void destroy() {
    }

    private static class Wrapper extends HttpServletRequestWrapper {

        private static final Pattern pathInfoPattern = Pattern.compile("^/geoserver/([^/]+?).*$");
        private static final Pattern servletPathPattern = Pattern.compile("^/geoserver/[^/]+?/([^/]+?).*$");

        private final UrlTransform urlTransform;

        private final String pathInfo;
        private final String servletPath;

        private final Map<String, String[]> parameters;

        public Wrapper(UrlTransform urlTransform, HttpServletRequest request) {
            super(request);
            this.urlTransform = urlTransform;
            pathInfo = extractPathInfo(urlTransform.getRequestUri());
            servletPath = extractServletPath(urlTransform.getRequestUri());
            parameters = new HashMap<String, String[]>(super.getParameterMap());
            parameters.putAll(urlTransform.getParameters());
        }

        @Override
        public String getPathInfo() {
            return pathInfo;
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }

        @Override
        public String getParameter(String name) {
            String[] value = parameters.get(name);
            if (value != null) {
                return value[0];
            }
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameters;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return super.getParameterNames();
        }

        @Override
        public String[] getParameterValues(final String name) {
            return getParameterMap().get(name);
        }

        private String extractPathInfo(String requestUri) {
            Matcher matcher = pathInfoPattern.matcher(requestUri);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return "";
        }

        private String extractServletPath(String requestUri) {
            Matcher matcher = servletPathPattern.matcher(requestUri);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return "";
        }
    }
}
