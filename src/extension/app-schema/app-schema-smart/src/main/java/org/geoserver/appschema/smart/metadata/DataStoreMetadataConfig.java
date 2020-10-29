package org.geoserver.appschema.smart.metadata;

import java.util.Map;

/**
 * Configuration class that determines the type of DataStoreMetadata that the
 * DataStoreMetadataFactory will build.
 *
 * @author Jose Macchi - Geosolutions
 */
public abstract class DataStoreMetadataConfig {

    public abstract String getName();

    public abstract String getType();

    public abstract Map<String, String> getParameters();
}
