package org.geoserver.appschema.smart.metadata;

import org.geoserver.appschema.smart.metadata.jdbc.JdbcDataStoreMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcDataStoreMetadataConfig;

/**
 * Factory class that builds a DataStoreMetadata based on the DataStoreMetadataConfig passed as
 * argument.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class DataStoreMetadataFactory {

    public DataStoreMetadata getDataStoreMetadata(DataStoreMetadataConfig config) throws Exception {
        if (config.getType().equals(JdbcDataStoreMetadataConfig.TYPE)) {
            JdbcDataStoreMetadataConfig jdmp = (JdbcDataStoreMetadataConfig) config;
            DataStoreMetadata store = new JdbcDataStoreMetadata(jdmp);
            store.load();
            return store;
        }
        return null;
    }
}
