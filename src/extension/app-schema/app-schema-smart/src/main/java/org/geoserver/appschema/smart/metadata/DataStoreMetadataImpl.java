package org.geoserver.appschema.smart.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class that allows to get metadata information from a particular DataStore. Implements
 * some of the generic methods in the interface DataStoreMetadata, delegating some specific methods
 * to the concrete DataStores classes implementations (ie. JdbcDataStoreMetadata)
 *
 * @author Jose Macchi - GeoSolutions
 */
public abstract class DataStoreMetadataImpl implements DataStoreMetadata {

    protected DataStoreMetadataConfig config;

    protected List<EntityMetadata> entities;
    protected List<RelationMetadata> relations;

    public DataStoreMetadataImpl(DataStoreMetadataConfig config) {
        this.entities = new ArrayList<EntityMetadata>();
        this.relations = new ArrayList<RelationMetadata>();
        this.config = config;
    }

    @Override
    public List<EntityMetadata> getDataStoreEntities() {
        return entities;
    }

    @Override
    public List<RelationMetadata> getEntityMetadataRelations(EntityMetadata entity) {
        List<RelationMetadata> output = new ArrayList<RelationMetadata>();
        Iterator<RelationMetadata> ir = this.relations.iterator();
        while (ir.hasNext()) {
            RelationMetadata relation = ir.next();
            if (relation.participatesInRelation(entity.getName())) {
                output.add(relation);
            }
        }
        return output;
    }

    @Override
    public List<RelationMetadata> getDataStoreRelations() {
        return this.relations;
    }

    @Override
    public DataStoreMetadataConfig getDataStoreMetadataConfig() {
        return this.config;
    }

    @Override
    public void setDataStoreMetadataConfig(DataStoreMetadataConfig modelMetadataConfig) {
        this.config = modelMetadataConfig;
    }
}
