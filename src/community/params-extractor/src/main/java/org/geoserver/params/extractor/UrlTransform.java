/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import java.util.*;

class UrlTransform {

    private final Map<String, String[]> parameters = new HashMap<>();
    private final String requestUri;
    private final String queryString;

    private final List<String> replacements = new ArrayList<>();

    public UrlTransform(String requestUri) {
        this(requestUri, Optional.empty());
    }

    public UrlTransform(String requestUri, Optional<String> queryString) {
        this.requestUri = requestUri;
        this.queryString = queryString.orElse("");
    }

    public String getOriginalRequestUri() {
        return requestUri;
    }

    public String getOriginalQueryString() {
        return queryString;
    }

    public String getRequestUri() {
        String updatedRequestUri = requestUri;
        for (String replacement : replacements) {
            updatedRequestUri = updatedRequestUri.replace(replacement, "");
        }
        return updatedRequestUri;
    }

    public String getQueryString() {
        if(parameters.isEmpty()) {
            return queryString;
        }
        StringBuilder queryStringBuilder = new StringBuilder();
        if (queryString.length() == 0) {
            queryStringBuilder.append("?");
        } else {
            queryStringBuilder.append("?").append(queryString).append("&");
        }
        for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
            queryStringBuilder.append(parameter.getKey()).append("=").append(parameter.getValue()[0]).append("&");
        }
        queryStringBuilder.deleteCharAt(queryStringBuilder.length() - 1);
        return queryStringBuilder.toString();
    }

    void addParameter(String name, String value, Optional<String> combine) {
        if (parameters.get(name) != null && combine.isPresent()) {
            String combinedValue = combine.get().replace("$1", parameters.get(name)[0]);
            combinedValue = combinedValue.replace("$2", value);
            parameters.put(name, new String[]{combinedValue});
        } else {
            parameters.put(name, new String[]{value});
        }
    }

    void removeMatch(String matchedText) {
        replacements.add(matchedText);
    }

    Map<String, String[]> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return getRequestUri() + getQueryString();
    }
}
