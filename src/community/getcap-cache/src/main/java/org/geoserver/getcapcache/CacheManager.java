/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.geotools.util.logging.Logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * <p>
 * The cache manager should be instantiated only once (spring singleton bean). Its thread safe,
 * all the operations can be safely invoked in concurrent environment.
 * </p>
 * <p>
 * Three operations are supported: put, get and truncate. The cache manager use instances of
 * {@link RequestInfo} for keys and instances of {@link CacheResult} for values.
 * </p>
 */
final class CacheManager {

    private final static Logger LOGGER = Logging.getLogger(CacheManager.class);

    protected final ConcurrentHashMap<RequestInfo, CacheResult> cache = new ConcurrentHashMap<RequestInfo, CacheResult>();

    /**
     * Put an element in the cache, if the element already exits it will be overridden.
     */
    void put(RequestInfo requestInfo, CacheResult result) {
        cache.put(requestInfo, result);
        Utils.debug(LOGGER, "Put cache entry for request: %s", requestInfo);
    }

    /**
     * Return an element from the cache, if the lement is not present NULL is returned.
     */
    CacheResult get(RequestInfo requestInfo) {
        return cache.get(requestInfo);
    }

    /**
     * Truncate the cache, all cached elements are removed.
     */
    void truncate() {
        cache.clear();
        Utils.debug(LOGGER, "Cache truncated.");
    }
}
