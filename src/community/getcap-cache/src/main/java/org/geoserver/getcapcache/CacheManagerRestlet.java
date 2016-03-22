/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.restlet.Finder;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

/**
 * <p>
 * Restlet that handle requests that will be delegated in the {@link CacheManager}.
 * The paths will be under: /rest/getcapcache/
 * </p>
 * <p>
 * Currently {@link CacheManager#truncate()} is the only support operation.
 * It can be invoked using GET using the path: /rest/getcapcache/truncate
 * </p>
 */
public class CacheManagerRestlet extends Finder {

    private final CacheManager cacheManager;

    public CacheManagerRestlet(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Resource findTarget(Request request, Response response) {
        // only the GET method is supported
        if (request.getMethod() != Method.GET) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return null;
        }
        return new Resource() {
            @Override
            public void handleGet() {
                // simply remove all the elements from the cache
                cacheManager.truncate();
            }
        };
    }
}