package org.geoserver.appschema.smart.domain;

/**
 * DomainModel configuration for Smart AppSchema. It keeps root entity and detailed information
 * about selected user attributes and relations that will be used for mappings to output formats.
 *
 * @author Jose Macchi - Geosolutions
 */
public final class DomainModelConfig {

    private String rootEntityName;

    public String getRootEntityName() {
        return rootEntityName;
    }

    public void setRootEntityName(String rootEntityName) {
        this.rootEntityName = rootEntityName;
    }
}
