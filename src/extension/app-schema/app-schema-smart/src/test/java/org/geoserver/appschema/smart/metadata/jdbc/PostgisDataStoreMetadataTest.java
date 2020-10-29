package org.geoserver.appschema.smart.metadata.jdbc;

import java.sql.DatabaseMetaData;
import java.util.Iterator;
import java.util.List;
import org.geoserver.appschema.smart.SmartAppSchemaPostgisTestSupport;
import org.geoserver.appschema.smart.metadata.AttributeMetadata;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geoserver.appschema.smart.metadata.EntityMetadata;
import org.geoserver.appschema.smart.metadata.RelationMetadata;
import org.geoserver.appschema.smart.utils.SmartAppSchemaTestHelper;
import org.junit.Test;

/**
 * Tests in Smart AppSchema related to use of a DataStoreMetadata linked to a JDBC connection.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class PostgisDataStoreMetadataTest extends SmartAppSchemaPostgisTestSupport {

    @Test
    public void testJdbcDataStoreMetadataLoad() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dm = getDataStoreMetadata(metaData);
        List<EntityMetadata> entities = dm.getDataStoreEntities();

        SmartAppSchemaTestHelper.printObjectsFromList(entities);

        Iterator<EntityMetadata> iEntity = entities.iterator();
        while (iEntity.hasNext()) {
            EntityMetadata e = iEntity.next();
            List<AttributeMetadata> attributes = e.getAttributes();
            SmartAppSchemaTestHelper.printObjectsFromList(attributes);
        }

        List<RelationMetadata> relations = dm.getDataStoreRelations();
        SmartAppSchemaTestHelper.printObjectsFromList(relations);

        assertEquals(3, entities.size());
        assertEquals(4, relations.size());

        metaData.getConnection().close();
    }

    @Test
    public void testMeteoObservationsEntityAttributes() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        EntityMetadata entity =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_observations");
        SmartAppSchemaTestHelper.printObjectsFromList(entity.getAttributes());

        assertEquals(5, entity.getAttributes().size());

        metaData.getConnection().close();
    }

    @Test
    public void testMeteoObservationsEntityRelations() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        EntityMetadata entity =
                new JdbcTableMetadata(metaData.getConnection(), null, SCHEMA, "meteo_observations");
        SmartAppSchemaTestHelper.printObjectsFromList(entity.getRelations());

        assertEquals(4, entity.getRelations().size());

        metaData.getConnection().close();
    }
}
