/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

/**
 * <p>
 * Helper class used to fool the dispatcher so we can use a cached result instead of computing it.
 * For the dispatcher this class will represent a service capable of handling the get capabilities
 * operation. Instead of executing the real get capabilities operation we return a cached result.
 * This helper is instantiated by this module dispatcher callback.
 * </p>
 */
public final class CacheService {

    // get capabilities operation cached result
    private final CacheResult result;

    CacheService(CacheResult result) {
        this.result = result;
    }

    public Object getCapabilities() {
        // we simply return the already cached result that will be handled by cache response
        return result;
    }
}
