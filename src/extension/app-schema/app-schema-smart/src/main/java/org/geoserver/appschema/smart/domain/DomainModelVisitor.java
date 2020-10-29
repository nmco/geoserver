package org.geoserver.appschema.smart.domain;

import org.geoserver.appschema.smart.domain.entities.DomainEntity;
import org.geoserver.appschema.smart.domain.entities.DomainEntitySimpleAttribute;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.domain.entities.DomainRelation;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;

/**
 * Smart AppSchema model objects visitor interface. Defined with the purpose of accessing elements
 * on model and visiting them in order to build output structure data.
 *
 * @author Jose Macchi - GeoSolutions
 */
public interface DomainModelVisitor {

    void visitDataStoreMetadata(DataStoreMetadata dataStoreMetadata);

    void visitDomainModel(DomainModel model);

    void visitDomainRootEntity(DomainEntity entity);

    void visitDomainChainedEntity(DomainEntity entity);

    void visitDomainEntitySimpleAttribute(DomainEntitySimpleAttribute attribute);

    void visitDomainRelation(DomainRelation relation);
}
