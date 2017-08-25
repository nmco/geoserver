/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.nsg.pagination.random;

import java.util.Map;

import org.geoserver.platform.resource.Resource;
import org.geotools.data.DataStore;

/**
 * 
 * Class used to store the index result type configuration managed by {@link IndexInitializer}
 */
public class IndexConfiguration {

    private static DataStore currentDataStore;

    private static Resource storageResource;

    private static Long timeToLive = 600l;

    private static Map<String, Object> currentDataStoreParams;

    /**
     * Store the DB parameters and the relative {@link DataStore}
     * 
     * @param currentDataStoreParams
     * @param currentDataStore
     */
    public static void setCurrentDataStore(Map<String, Object> currentDataStoreParams,
            DataStore currentDataStore) {
        IndexConfiguration.currentDataStoreParams = currentDataStoreParams;
        IndexConfiguration.currentDataStore = currentDataStore;
    }

    /**
     * Store the reference to resource used to archive the serialized GetFeatureRequest
     * 
     * @param currentDataStoreParams
     * @param currentDataStore
     */
    public static void setStorageResource(Resource storageResource) {
        IndexConfiguration.storageResource = storageResource;
    }

    /**
     * Store the value of time to live of stored GetFeatureRequest
     * 
     * @param timeToLive
     */
    public static void setTimeToLive(Long timeToLive) {
        IndexConfiguration.timeToLive = timeToLive;
    }

    public static DataStore getCurrentDataStore() {
        return currentDataStore;
    }

    public static Map<String, Object> getCurrentDataStoreParams() {
        return currentDataStoreParams;
    }

    public static Resource getStorageResource() {
        return storageResource;
    }

    public static Long getTimeToLive() {
        return timeToLive;
    }

}
