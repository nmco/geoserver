package org.geoserver.appschema.smart.domain;

import org.geoserver.appschema.smart.domain.entities.DomainEntity;
import org.geoserver.appschema.smart.domain.entities.DomainEntitySimpleAttribute;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.domain.entities.DomainRelation;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;

/**
 * DomainModelVisitorImpl
 *
 * @author Jose Macchi - GeoSolutions
 */
public class DomainModelVisitorImpl implements DomainModelVisitor {

    @Override
    public void visitDataStoreMetadata(DataStoreMetadata dataStoreMetadata) {}

    @Override
    public void visitDomainModel(DomainModel model) {}

    @Override
    public void visitDomainRootEntity(DomainEntity entity) {}

    @Override
    public void visitDomainChainedEntity(DomainEntity entity) {}

    @Override
    public void visitDomainEntitySimpleAttribute(DomainEntitySimpleAttribute attribute) {}

    @Override
    public void visitDomainRelation(DomainRelation relation) {}
}
