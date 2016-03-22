/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

/**
 * <p>
 * Helper container the contains the info for identifying a request in the cache manager.
 * Currently requests are identified by the service and the version.
 * </p>
 */
final class RequestInfo {

    private final String service;
    private final String version;

    public RequestInfo(String service, String version) {
        this.service = service;
        this.version = version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        RequestInfo that = (RequestInfo) other;
        return service != null ? service.equals(that.service) : that.service == null
                && (version != null ? version.equals(that.version) : that.version == null);
    }

    @Override
    public int hashCode() {
        int result = service != null ? service.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("[service=%s, version=%s]", service, version);
    }
}
