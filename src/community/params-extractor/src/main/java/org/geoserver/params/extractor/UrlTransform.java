/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import java.net.URLEncoder;
import java.util.*;

public class UrlTransform {

    private final Map<String, String[]> parameters = new HashMap<>();
    private final String requestUri;
    private final String queryString;

    private final List<String> replacements = new ArrayList<>();

    public UrlTransform(String requestUri, Optional<String> queryString) {
        this.requestUri = requestUri;
        this.queryString = queryString.orElse("");
    }

    public String getOriginalRequestUri() {
        return requestUri;
    }

    public String getRequestUri() {
        String updatedRequestUri = requestUri;
        for (String replacement : replacements) {
            updatedRequestUri = updatedRequestUri.replace(replacement, "");
        }
        return updatedRequestUri;
    }

    public String getQueryString() {
        if (parameters.isEmpty()) {
            return queryString;
        }
        StringBuilder queryStringBuilder = new StringBuilder();
        if (queryString.length() != 0) {
            queryStringBuilder.append(queryString).append("&");
        }
        for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
            queryStringBuilder.append(parameter.getKey())
                    .append("=").append(URLEncoder.encode(parameter.getValue()[0])).append("&");
        }
        queryStringBuilder.deleteCharAt(queryStringBuilder.length() - 1);
        return queryStringBuilder.toString();
    }

    public void addParameter(String name, String value, Optional<String> combine) {
        if (parameters.get(name) != null && combine.isPresent()) {
            String combinedValue = combine.get().replace("$1", parameters.get(name)[0]);
            combinedValue = combinedValue.replace("$2", value);
            parameters.put(name, new String[]{combinedValue});
        } else {
            parameters.put(name, new String[]{value});
        }
    }

    public void removeMatch(String matchedText) {
        replacements.add(matchedText);
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public boolean haveChanged() {
        return !(parameters.isEmpty() && replacements.isEmpty());
    }

    @Override
    public String toString() {
        String updatedQueryString = getQueryString();
        if (updatedQueryString.isEmpty()) {
            return getRequestUri();
        }
        return getRequestUri() + "?" + getQueryString();
    }
}
