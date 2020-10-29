package org.geoserver.appschema.smart.domain.entities;

import org.geoserver.appschema.smart.domain.DomainModelVisitor;

/**
 * Class representing an attribute of an entity on the Smart AppSchema model.
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class DomainEntitySimpleAttribute {

    private String name;
    private DomainAttributeType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DomainAttributeType getType() {
        return type;
    }

    public void setType(DomainAttributeType type) {
        this.type = type;
    }

    public void accept(DomainModelVisitor visitor) {
        visitor.visitDomainEntitySimpleAttribute(this);
    }
}
