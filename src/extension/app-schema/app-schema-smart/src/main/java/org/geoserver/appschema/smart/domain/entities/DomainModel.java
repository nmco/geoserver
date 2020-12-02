package org.geoserver.appschema.smart.domain.entities;

import org.geoserver.appschema.smart.domain.DomainModelVisitor;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;

/**
 * Contains the definition of a domain model, the navigation of a domain model should be done
 * thought the visitor API.
 */
public class DomainModel {

    private final DataStoreMetadata dataStoreMetadata;
    private final DomainEntity rootEntity;

    public DomainModel(DataStoreMetadata dataStoreMetadata, DomainEntity rootEntity) {
        this.dataStoreMetadata = dataStoreMetadata;
        this.rootEntity = rootEntity;
    }

    /** Will visitor the domain model definition with the provided visitor. */
    public void accept(DomainModelVisitor visitor) {
        visitor.visitDomainModel(this);
        visitor.visitDataStoreMetadata(dataStoreMetadata);
        rootEntity.accept(visitor, true);
    }

    /** Return the root entity of the domain model. */
    public DomainEntity getRootEntity() {
        return rootEntity;
    }

    /** Returns the raw metadata obtained from the data store to build the domain model. */
    public DataStoreMetadata getDataStoreMetadata() {
        return dataStoreMetadata;
    }
}
