/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class UrlTransform {

    private final Map<String, String[]> parameters = new HashMap<>();

    private String requestUri;

    private final StringBuilder queryString;

    public UrlTransform(String requestUri) {
        this(requestUri, Optional.empty());
    }

    public UrlTransform(String requestUri, Optional<String> queryString) {
        this.requestUri = requestUri;
        this.queryString = new StringBuilder(queryString.orElse(""));
    }

    public String getRequestUri() {
        return requestUri;
    }

    public StringBuilder getQueryString() {
        return queryString;
    }

    void addParameter(String name, String value) {
        parameters.put(name, new String[]{value});
        if (queryString.length() != 0) {
            queryString.append("&");
        }
        queryString.append(name).append("=").append(value);
    }

    void removeMatch(int start, int end) {
        this.requestUri = new StringBuilder(requestUri).delete(start, end).toString();
    }

    Map<String, String[]> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        if (queryString.length() == 0) {
            return requestUri;
        }
        return requestUri + "?" + queryString;
    }
}
