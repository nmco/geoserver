package org.geoserver.appschema.smart.domain.inspire;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.geoserver.appschema.smart.SmartAppSchemaPostgisTestSupport;
import org.geoserver.appschema.smart.data.store.ExclusionsDomainModelVisitor;
import org.geoserver.appschema.smart.domain.DomainModelBuilder;
import org.geoserver.appschema.smart.domain.DomainModelConfig;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.junit.Test;

/**
 * Tests related to ExclusionsDomainModelVisitor.
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class ExclusionsDomainModelVisitorTest extends SmartAppSchemaPostgisTestSupport {

    public ExclusionsDomainModelVisitorTest() {
        SCHEMA = "smartappschematest";
        NAMESPACE_PREFIX = "inspire";
        TARGET_NAMESPACE = "http://www.api4inspire.it/smartappschema/1.0";
        MOCK_SQL_SCRIPT = "inspire_db.sql";
    }

    private DomainModel getDomainModelForExclusionsInitiativeTests()
            throws SQLException, IOException, Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("initiative");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        return dm;
    }

    @Test
    public void testDomainModelVisitWithExclusions_Initiative_01() throws Exception {
        DomainModel dm = getDomainModelForExclusionsInitiativeTests();
        List<String> exclusions = new ArrayList<>();
        // keep only root and attributes
        exclusions.add("initiative.endpoint");
        DomainModel newDomainModel = ExclusionsDomainModelVisitor.buildDomainModel(dm, exclusions);
        assertEquals(16, newDomainModel.getRootEntity().getAttributes().size());
        assertEquals(0, newDomainModel.getRootEntity().getRelations().size()); // no relations
    }

    @Test
    public void testDomainModelVisitWithExclusions_Initiative_02() throws Exception {
        DomainModel dm = getDomainModelForExclusionsInitiativeTests();
        List<String> exclusions = new ArrayList<>();
        // remove some attributes, keep only root, no relations
        exclusions.add("initiative.endpoint");
        exclusions.add("initiative.name");
        exclusions.add("initiative.website");
        exclusions.add("initiative.startdate");
        DomainModel newDomainModel = ExclusionsDomainModelVisitor.buildDomainModel(dm, exclusions);
        assertEquals(13, newDomainModel.getRootEntity().getAttributes().size());
        assertEquals(0, newDomainModel.getRootEntity().getRelations().size()); // no relations
    }

    @Test
    public void testDomainModelVisitWithExclusions_Initiative_03() throws Exception {
        DomainModel dm = getDomainModelForExclusionsInitiativeTests();
        List<String> exclusions = new ArrayList<>();
        // remove some attributes on root entity, keep relations 1st level
        exclusions.add("initiative.name");
        exclusions.add("initiative.website");
        exclusions.add("initiative.startdate");
        DomainModel newDomainModel = ExclusionsDomainModelVisitor.buildDomainModel(dm, exclusions);
        assertEquals(13, newDomainModel.getRootEntity().getAttributes().size());
        assertEquals(1, newDomainModel.getRootEntity().getRelations().size());
        assertEquals(
                "endpoint",
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getName());
        assertEquals(
                4,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .size());
        assertEquals(
                5,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getAttributes()
                        .size());
    }

    @Test
    public void testDomainModelVisitWithExclusions_Initiative_04() throws Exception {
        DomainModel dm = getDomainModelForExclusionsInitiativeTests();
        List<String> exclusions = new ArrayList<>();
        // remove some attributes on root entity, keep relations 1st level
        exclusions.add("initiative.name");
        exclusions.add("initiative.website");
        exclusions.add("initiative.startdate");
        // remove some attributes on 1st level related entity
        exclusions.add("endpoint.url");
        exclusions.add("endpoint.type");
        DomainModel newDomainModel = ExclusionsDomainModelVisitor.buildDomainModel(dm, exclusions);
        assertEquals(13, newDomainModel.getRootEntity().getAttributes().size());
        assertEquals(1, newDomainModel.getRootEntity().getRelations().size());
        assertEquals(
                "endpoint",
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getName());
        assertEquals(
                4,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .size());
        assertEquals(
                3,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getAttributes()
                        .size());
    }

    @Test
    public void testDomainModelVisitWithExclusions_Initiative_05() throws Exception {
        DomainModel dm = getDomainModelForExclusionsInitiativeTests();
        List<String> exclusions = new ArrayList<>();
        // remove some attributes on root entity, keep relations 1st level
        // remove some attributes on 1st level related entity
        exclusions.add("initiative.name");
        exclusions.add("initiative.website");
        exclusions.add("initiative.startdate");
        // remove some attributes on 1st level related entity
        exclusions.add("endpoint.url");
        exclusions.add("endpoint.type");
        // remove some relations
        exclusions.add("endpoint.access_mode");
        exclusions.add("endpoint.data_type");
        DomainModel newDomainModel = ExclusionsDomainModelVisitor.buildDomainModel(dm, exclusions);
        assertEquals(13, newDomainModel.getRootEntity().getAttributes().size());
        assertEquals(1, newDomainModel.getRootEntity().getRelations().size());
        assertEquals(
                "endpoint",
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getName());
        assertEquals(
                3,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getAttributes()
                        .size());
        assertEquals(
                2,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .size());
    }

    @Test
    public void testDomainModelVisitWithExclusions_Initiative_06() throws Exception {
        DomainModel dm = getDomainModelForExclusionsInitiativeTests();
        List<String> exclusions = new ArrayList<>();
        // remove some attributes on root entity, keep relations 1st level
        // remove some attributes on 1st level related entity
        exclusions.add("initiative.name");
        exclusions.add("initiative.website");
        exclusions.add("initiative.startdate");
        // remove some attributes on 1st level related entity
        exclusions.add("endpoint.url");
        exclusions.add("endpoint.type");
        // remove some relations at 1st level
        exclusions.add("endpoint.access_mode");
        exclusions.add("endpoint.data_type");
        exclusions.add("endpoint.output_format");
        DomainModel newDomainModel = ExclusionsDomainModelVisitor.buildDomainModel(dm, exclusions);
        assertEquals(13, newDomainModel.getRootEntity().getAttributes().size());
        assertEquals(1, newDomainModel.getRootEntity().getRelations().size());
        assertEquals(
                "endpoint",
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getName());
        assertEquals(
                3,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getAttributes()
                        .size());
        assertEquals(
                1,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .size());
        assertEquals(
                "indicator_initiative_ass",
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getName());
        assertEquals(
                0,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getAttributes()
                        .size());
        assertEquals(
                1,
                newDomainModel
                        .getRootEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .get(0)
                        .getDestinationEntity()
                        .getRelations()
                        .size());
    }
}
