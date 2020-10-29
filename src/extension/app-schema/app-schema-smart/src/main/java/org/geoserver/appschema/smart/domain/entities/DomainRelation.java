package org.geoserver.appschema.smart.domain.entities;

import org.geoserver.appschema.smart.domain.DomainModelVisitor;

/**
 * Class representing a relation between two entities on the Smart AppSchema model.
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class DomainRelation {

    private DomainEntity containingEntity;
    private DomainEntity destinationEntity;
    private DomainEntitySimpleAttribute containingKeyAttribute;
    private DomainEntitySimpleAttribute destinationKeyAttribute;

    public DomainEntity getContainingEntity() {
        return containingEntity;
    }

    public DomainEntity getDestinationEntity() {
        return destinationEntity;
    }

    public void setContainingEntity(DomainEntity containingEntity) {
        this.containingEntity = containingEntity;
    }

    public void setDestinationEntity(DomainEntity destinationEntity) {
        this.destinationEntity = destinationEntity;
    }

    public DomainEntitySimpleAttribute getContainingKeyAttribute() {
        return containingKeyAttribute;
    }

    public void setContainingKeyAttribute(DomainEntitySimpleAttribute containingKeyAttribute) {
        this.containingKeyAttribute = containingKeyAttribute;
    }

    public DomainEntitySimpleAttribute getDestinationKeyAttribute() {
        return destinationKeyAttribute;
    }

    public void setDestinationKeyAttribute(DomainEntitySimpleAttribute destinationKeyAttribute) {
        this.destinationKeyAttribute = destinationKeyAttribute;
    }

    public void accept(DomainModelVisitor visitor) {
        visitor.visitDomainRelation(this);
        destinationEntity.accept(visitor, false);
    }
}
