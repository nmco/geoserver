package org.geoserver.appschema.smart.domain.entities;

/**
 * Cardinality of a relation of the domain model, the direction goes from the cotnaining entity to
 * the destination entity.
 */
public enum DomainRelationType {
    ONEONE,
    ONEMANY,
    MANYONE,
    MANYMANY
}
