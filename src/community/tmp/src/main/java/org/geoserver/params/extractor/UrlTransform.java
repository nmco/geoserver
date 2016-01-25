/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class UrlTransform {

    private final Map<String, String> parameters = new HashMap<>();

    private String requestUrl;

    private final StringBuilder queryString;

    public UrlTransform(String requestUrl) {
        this(requestUrl, Optional.empty());
    }

    public UrlTransform(String requestUrl, Optional<String> queryString) {
        this.requestUrl = requestUrl;
        this.queryString = new StringBuilder(queryString.orElse(""));
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public StringBuilder getQueryString() {
        return queryString;
    }

    void addParameter(String name, String value) {
        parameters.put(name, value);
        if (queryString.length() != 0) {
            queryString.append("&");
        }
        queryString.append(name).append("=").append(value);
    }

    void removeMatch(int start, int end) {
        this.requestUrl = new StringBuilder(requestUrl).delete(start, end).toString();
    }

    @Override
    public String toString() {
        if (queryString.length() == 0) {
            return requestUrl;
        }
        return requestUrl + "?" + queryString;
    }
}
