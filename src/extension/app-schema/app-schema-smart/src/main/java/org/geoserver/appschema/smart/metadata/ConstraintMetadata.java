package org.geoserver.appschema.smart.metadata;

/**
 * Class that represents metadata for constraints definitions on the underlying DataStore model.
 *
 * @author Jose Macchi - Geosolutions
 */
public abstract class ConstraintMetadata implements Comparable<ConstraintMetadata> {

    protected String name;

    public ConstraintMetadata(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
