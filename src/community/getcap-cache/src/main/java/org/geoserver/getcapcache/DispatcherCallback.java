/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.ows.LocalLayer;
import org.geoserver.ows.LocalWorkspace;
import org.geoserver.ows.Request;
import org.geoserver.ows.Response;
import org.geoserver.platform.Operation;
import org.geoserver.platform.Service;
import org.geoserver.platform.ServiceException;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * <p>
 * We intercept cacheable requests in two executions: when the handling service is found
 * and when the response is dispatched. Currently we only consider cacheable non
 * authenticated WMS GetCapabilities requests without local workspace or local layer.
 * If caching is disabled or the client IP address is not allowed to use the cache the
 * request will not be considered cacheable.
 * </p>
 * <p>
 * In the first execution point we check if there is already a cached value for the
 * request. If that is the case we return an instance of a {@link CacheService} (properly
 * initiated with the cached value) instead of the original service. Otherwise we return
 * the original service.
 * </p>
 * <p>
 * In the second point if the result is not a {@link CacheResult} and the request is cacheable
 * we cached the request result for future use.
 * </p>
 * <p>
 * If the client provided an update sequence we ignore the first point. If the sequence is equal
 * no need for the cache. If the sequence is higher we let the normal service throw the appropriate
 * exception. If the sequence is lower we return the cached result.
 * </p>
 */
public final class DispatcherCallback implements org.geoserver.ows.DispatcherCallback {

    private final static Logger LOGGER = Logging.getLogger(DispatcherCallback.class);

    // the singleton cache manager used by this callback
    private final CacheManager cacheManager;

    // geoserver data directory, used to get configurations
    private final GeoServerDataDirectory dataDirectory;

    // the configuration used by this callback, the configuration ca be changed at runtime
    private volatile Configuration configuration;

    public DispatcherCallback(CacheManager cacheManager, GeoServerDataDirectory dataDirectory) {
        this.cacheManager = cacheManager;
        this.dataDirectory = dataDirectory;
        initConfiguration();
    }

    private void initConfiguration() {
        updateConfiguration();
        if (this.configuration == null) {
            throw Utils.exception("The provided configuration is not valid.");
        }
        setupConfigurationListener();
    }

    /**
     * Updates the current configuration by parsing the configuration file.
     */
    private void updateConfiguration() {
        try {
            // we get the configuration file, if he doesn't exists it will be created
            File configurationDirectory = dataDirectory.findOrCreateDir(ConfigurationParser.CONFIGURATION_DIRECTORY);
            File configurationFile = Utils.getOrCreate(configurationDirectory,
                    ConfigurationParser.CONFIGURATION_FILE_NAME, ConfigurationParser.CONFIGURATION_DEFAULT_CONTENT);
            new Utils.DoWorkWithFile(configurationFile) {
                @Override
                public void doWork(InputStream inputStream) {
                    // let's parse the configuration
                    Configuration newConfiguration = ConfigurationParser.parse(inputStream);
                    if (newConfiguration != null) {
                        // the new configuration seems to be ok
                        configuration = newConfiguration;
                        Utils.info(LOGGER, "Configuration updated.");
                    } else {
                        // the new configuration is not parsable
                        Utils.info(LOGGER, "The new configuration was no valid.");
                    }
                }
            };
        } catch (Exception exception) {
            throw Utils.exception(exception, "Error reading configuration: %s", exception.getMessage());
        }
    }

    /**
     * Helper method that setups a listener on the configuration file. If the file
     * is created or changed the configuration will be updated.
     */
    private void setupConfigurationListener() {
        try {
            FileAlterationMonitor monitor = new FileAlterationMonitor(1000);
            FileAlterationObserver observer = new FileAlterationObserver(
                    dataDirectory.findOrCreateDir(ConfigurationParser.CONFIGURATION_DIRECTORY));
            observer.addListener(new FileAlterationListenerAdaptor() {
                @Override
                public void onFileCreate(File file) {
                    if (file.getPath().contains(ConfigurationParser.CONFIGURATION_PATH)) {
                        Utils.info(LOGGER, "Configuration file created.");
                        updateConfiguration();
                    }
                }

                @Override
                public void onFileChange(File file) {
                    if (file.getPath().contains(ConfigurationParser.CONFIGURATION_PATH)) {
                        Utils.info(LOGGER, "Configuration file changed.");
                        updateConfiguration();
                    }
                }
            });
            monitor.addObserver(observer);
            monitor.start();
        } catch (Exception exception) {
            throw Utils.exception(exception, "Error setting up configuration listener: %s", exception.getMessage());
        }
    }

    Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Request init(Request request) {
        return request;
    }

    @Override
    public Service serviceDispatched(Request request, Service service) throws ServiceException {
        // is this a cacheable request ?
        if (isCacheableRequest(request)) {
            // we ask the cache manager for a cached result corresponding to this request
            RequestInfo requestInfo = new RequestInfoBuilder().withRequest(request).build();
            CacheResult result = cacheManager.get(requestInfo);
            // if there is no catchable result we proceed with a normal execution
            if (result != null && checkUpdateSequence(request, result)) {
                // we return an instance of the cache service initiated with the found cached value
                Utils.info(LOGGER, "Cached result exists for request: %s", requestInfo);
                return new Service(service.getId(), service.getNamespace(),
                        new CacheService(result), service.getVersion(), service.getOperations());
            }
            Utils.info(LOGGER, "No cached result for request: %s", requestInfo);
        }
        return service;
    }

    @Override
    public Operation operationDispatched(Request request, Operation operation) {
        return operation;
    }

    @Override
    public Object operationExecuted(Request request, Operation operation, Object result) {
        return result;
    }

    @Override
    public Response responseDispatched(Request request, Operation operation, Object result, Response response) {
        // is this a cacheable request with not cached result ?
        if (isCacheableRequest(request) && !(result instanceof CacheResult)) {
            // this is the first execution of a cacheable requests or the first execution after a cache truncate
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                // we obtain the result from the original response
                response.write(result, output, operation);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            // we cache this response result
            RequestInfo requestInfo = new RequestInfoBuilder().withRequest(request).build();
            CacheResult cacheResult = new CacheResult(requestInfo, response.getMimeType(result, operation), output);
            cacheManager.put(requestInfo, cacheResult);
            Utils.info(LOGGER, "Cached result for request: %s", requestInfo);
        }
        return response;
    }

    @Override
    public void finished(Request request) {
    }

    /**
     * Helper method to test if we are in the presence of anonymous (non authenticated) request.
     */
    private boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // this request is non authenticated if authentication is NULL and the user is anonymous
        return !(authentication != null && !authentication.getName().equals("anonymous"));
    }

    /**
     * Helper method that checks if the current request is cacheabe.
     */
    private boolean isCacheableRequest(Request request) {
        // only non authenticated WMS GetCapabilities requests without local workspace or local layer are cacheable
        // we also check the caching is enable and that the IP address of the client is allowed to use caching
        return configuration.isCachingEnable()
                && configuration.isIpAddressCacheable(getIpAddress(request))
                && isAnonymous() && request.getService().equalsIgnoreCase("wms")
                && request.getRequest().equalsIgnoreCase("getcapabilities")
                && LocalWorkspace.get() == null && LocalLayer.get() == null;
    }

    /**
     * Helper method that extract client IP address from request.
     */
    private String getIpAddress(Request request) {
        // let's see if the client is behind a proxy
        String ipAddress = request.getHttpRequest().getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            // no proxy so let's simply return the remote address
            ipAddress = request.getHttpRequest().getRemoteAddr();
        }
        return ipAddress;
    }

    /**
     * Helper method to check the update sequence value. If no update sequence is present we return TRUE.
     * If the update sequence is present and lower than the cached one we return TRUE. Otherwise we return FALSE.
     */
    private boolean checkUpdateSequence(Request request, CacheResult result) {
        String rawUpdateSequence = (String) request.getKvp().get("UPDATESEQUENCE");
        return rawUpdateSequence == null || Integer.parseInt(rawUpdateSequence) < result.getUpdateSequence();
    }
}
