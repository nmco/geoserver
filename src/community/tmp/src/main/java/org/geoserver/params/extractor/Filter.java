/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.filters.GeoServerFilter;
import org.geoserver.platform.ExtensionPriority;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.ResourceListener;
import org.geoserver.platform.resource.ResourceNotification;
import org.geoserver.platform.resource.ResourceStore;
import org.geotools.util.logging.Logging;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

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
        chain.doFilter(new Wrapper((HttpServletRequestWrapper) request), response);
    }

    @Override
    public void destroy() {
    }

    private static class Wrapper extends HttpServletRequestWrapper {

        public Wrapper(HttpServletRequest request) {
            super(request);
        }
    }
}
