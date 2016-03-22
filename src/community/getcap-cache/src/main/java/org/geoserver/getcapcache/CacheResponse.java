/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.geoserver.ows.Response;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geotools.util.logging.Logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * <p>
 * This response can only handle {@link CacheResult} results. We used the cached values
 * to produced the response. The mime type is directly obtained from the cache result an
 * we copy the cache output stream to the response output stream.
 * </p>
 */
public final class CacheResponse extends Response {

    private final static Logger LOGGER = Logging.getLogger(CacheResult.class);

    public CacheResponse() {
        super(CacheResult.class);
    }

    @Override
    public String getMimeType(Object value, Operation operation) throws ServiceException {
        // we return the cache mime type for this request
        CacheResult result = (CacheResult) value;
        Utils.info(LOGGER, "Return cached mime type for request: %s", result.getRequestInfo());
        return result.getMimeType();
    }

    @Override
    public void write(Object value, OutputStream output, Operation operation) throws IOException, ServiceException {
        // we copy the cached stream to this response output
        CacheResult result = (CacheResult) value;
        Utils.info(LOGGER, "Return cached output stream for request: %s", result.getRequestInfo());
        result.getOutput().writeTo(output);
    }
}
