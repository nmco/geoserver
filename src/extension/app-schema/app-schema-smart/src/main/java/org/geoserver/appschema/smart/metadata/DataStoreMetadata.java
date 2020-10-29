package org.geoserver.appschema.smart.metadata;

import java.util.List;

/**
 * Interface that provides access to metadata from a particular DataStore. It identifies different
 * objects that defines the abstract model. Those are Entities, Relations and Attributes.
 *
 * @author Jose Macchi - GeoSolutions
 */
public interface DataStoreMetadata {

    public String getName();

    public DataStoreMetadataConfig getDataStoreMetadataConfig();

    public void setDataStoreMetadataConfig(DataStoreMetadataConfig modelMetadataConfig);

    public List<EntityMetadata> getDataStoreEntities();

    public List<RelationMetadata> getDataStoreRelations();

    public List<RelationMetadata> getEntityMetadataRelations(EntityMetadata entity);

    public EntityMetadata getEntityMetadata(String name);

    public void load() throws Exception;
}
