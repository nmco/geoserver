package org.geoserver.appschema.smart.domain;

import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.appschema.smart.SmartAppSchemaPostgisTestSupport;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.domain.entities.DomainRelation;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geotools.util.logging.Logging;
import org.junit.Test;

/**
 * Tests for DomainModelBuilder class.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class DomainModelBuilderTest extends SmartAppSchemaPostgisTestSupport {

    private static final Logger LOGGER = Logging.getLogger(DomainModelBuilderTest.class);

    @Test
    public void testDomainModelBuilderWithRootEntityFailure() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_failure");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);

        DomainModel dm = null;
        try {
            dm = dmb.buildDomainModel();
            LOGGER.log(Level.INFO, dm.toString());
        } catch (RuntimeException e) {
            assertEquals(dm, null);
        }
    }

    @Test
    public void testDomainModelBuilderWithStationsAsRoot() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_stations");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);

        DomainModel dm = null;
        dm = dmb.buildDomainModel();
        LOGGER.log(Level.INFO, dm.toString());

        // check build domainmodel
        int rootMeteoStationsRelationsSize = dm.getRootEntity().getRelations().size();
        DomainRelation meteoStationRelation = dm.getRootEntity().getRelations().get(0);
        String containingEntityNameMeteoStationsRelation =
                meteoStationRelation.getContainingEntity().getName();
        String destinationEntityNameMeteoStationsRelation =
                meteoStationRelation.getDestinationEntity().getName();
        int meteoObservationsRelationsSize =
                meteoStationRelation.getDestinationEntity().getRelations().size();
        DomainRelation meteoObservationRelation =
                meteoStationRelation.getDestinationEntity().getRelations().get(0);
        String destinationEntityNameMeteoObservationsRelation =
                meteoObservationRelation.getDestinationEntity().getName();
        int meteoParamatersRelationsSize =
                meteoObservationRelation.getDestinationEntity().getRelations().size();

        assertEquals(4, dm.getRootEntity().getAttributes().size());
        assertEquals(1, rootMeteoStationsRelationsSize);
        assertEquals("meteo_stations", containingEntityNameMeteoStationsRelation);
        assertEquals("meteo_observations", destinationEntityNameMeteoStationsRelation);
        assertEquals(1, meteoObservationsRelationsSize);
        assertEquals("meteo_parameters", destinationEntityNameMeteoObservationsRelation);
        assertEquals(0, meteoParamatersRelationsSize);
    }

    @Test
    public void testDomainModelBuilderWithParametersAsRoot() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_parameters");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);

        DomainModel dm = null;
        dm = dmb.buildDomainModel();
        LOGGER.log(Level.INFO, dm.toString());

        // check build domainmodel
        int rootMeteoParametersRelationsSize = dm.getRootEntity().getRelations().size();
        DomainRelation meteoParameterRelation = dm.getRootEntity().getRelations().get(0);
        String containingEntityNameMeteoParametersRelation =
                meteoParameterRelation.getContainingEntity().getName();
        String destinationEntityNameMeteoParametersRelation =
                meteoParameterRelation.getDestinationEntity().getName();
        int meteoObservationsRelationsSize =
                meteoParameterRelation.getDestinationEntity().getRelations().size();
        DomainRelation meteoObservationRelation =
                meteoParameterRelation.getDestinationEntity().getRelations().get(0);
        String destinationEntityNameMeteoObservationsRelation =
                meteoObservationRelation.getDestinationEntity().getName();
        int meteoStationsRelationsSize =
                meteoObservationRelation.getDestinationEntity().getRelations().size();

        assertEquals(3, dm.getRootEntity().getAttributes().size());
        assertEquals(1, rootMeteoParametersRelationsSize);
        assertEquals("meteo_parameters", containingEntityNameMeteoParametersRelation);
        assertEquals("meteo_observations", destinationEntityNameMeteoParametersRelation);
        assertEquals(1, meteoObservationsRelationsSize);
        assertEquals("meteo_stations", destinationEntityNameMeteoObservationsRelation);
        assertEquals(0, meteoStationsRelationsSize);
    }

    @Test
    public void testDomainModelBuilderWithObservationsAsRoot() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_observations");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);

        DomainModel dm = null;
        dm = dmb.buildDomainModel();
        LOGGER.log(Level.INFO, dm.toString());

        // check build domainmodel
        int rootMeteoObservationsRelationsSize = dm.getRootEntity().getRelations().size();
        // check we have 2 relations
        assertEquals(2, rootMeteoObservationsRelationsSize);
        // check relations containing and destination entity, as much as relations from that
        // entities
        List<DomainRelation> meteoObservationsRelations = dm.getRootEntity().getRelations();
        Map<String, DomainRelation> map = new HashMap<>();
        // put every relation in a map, with key based on destination entity
        for (DomainRelation dr : meteoObservationsRelations) {
            map.put(dr.getDestinationEntity().getName(), dr);
        }
        map.forEach(
                (destinationKey, relation) -> {
                    int relationsSize = relation.getDestinationEntity().getRelations().size();
                    assertEquals(0, relationsSize);
                    assertEquals("meteo_observations", relation.getContainingEntity().getName());
                    assertEquals(destinationKey, relation.getDestinationEntity().getName());
                });
        assertEquals(3, dm.getRootEntity().getAttributes().size());
    }
}
