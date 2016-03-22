/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.geoserver.ows.Request;

/**
 * <p>
 * Helper class to build a correct request info.
 * </p>
 */
final class RequestInfoBuilder {

    private String service;
    private String version;

    RequestInfoBuilder withRequest(String service) {
        this.service = service;
        return this;
    }

    RequestInfoBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    RequestInfoBuilder withRequest(Request request) {
        // we use the normalized KVP values so we can avoid any kind of internal negotiations
        this.service = (String) request.getKvp().get("SERVICE");
        this.version = (String) request.getKvp().get("VERSION");
        return this;
    }

    RequestInfo build() {
        service = Utils.withDefault(service, "NULL");
        version = Utils.withDefault(version, "NULL");
        return new RequestInfo(service.toUpperCase(), version.toUpperCase());
    }
}