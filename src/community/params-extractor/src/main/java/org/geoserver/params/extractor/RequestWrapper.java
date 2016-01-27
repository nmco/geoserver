package org.geoserver.params.extractor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RequestWrapper extends HttpServletRequestWrapper {

    private static final Pattern pathInfoPattern = Pattern.compile("^/geoserver/([^/]+?).*$");
    private static final Pattern servletPathPattern = Pattern.compile("^/geoserver/[^/]+?/([^/]+?).*$");

    private final UrlTransform urlTransform;

    private final String pathInfo;
    private final String servletPath;

    private final Map<String, String[]> parameters;

    public RequestWrapper(UrlTransform urlTransform, HttpServletRequest request) {
        super(request);
        this.urlTransform = urlTransform;
        pathInfo = extractPathInfo(urlTransform.getOriginalRequestUri());
        servletPath = extractServletPath(urlTransform.getOriginalRequestUri());
        parameters = new HashMap<>(super.getParameterMap());
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