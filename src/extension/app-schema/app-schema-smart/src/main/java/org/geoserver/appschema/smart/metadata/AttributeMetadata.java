package org.geoserver.appschema.smart.metadata;

import com.google.common.collect.ComparisonChain;

/**
 * Class that represents metadata for entities' attributes on the underlying DataStore model.
 *
 * @author Jose Macchi - GeoSolutions
 */
public abstract class AttributeMetadata implements Comparable<AttributeMetadata> {

    protected String name;
    protected EntityMetadata entity;
    protected String type;
    protected boolean externalReference;

    public AttributeMetadata(
            EntityMetadata entity, String name, String type, boolean externalReference) {
        this.name = name;
        this.entity = entity;
        this.setType(type);
        this.externalReference = externalReference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EntityMetadata getEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }

    public boolean isExternalReference() {
        return externalReference;
    }

    @Override
    public int compareTo(AttributeMetadata attributeMetadata) {
        if (attributeMetadata != null) {
            return ComparisonChain.start()
                    .compare(this.getEntity(), attributeMetadata.getEntity())
                    .compare(this.name, attributeMetadata.getName())
                    .result();
        }
        return 1;
    }
}
