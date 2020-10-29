package org.geoserver.appschema.smart.domain;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.DatabaseMetaData;
import org.apache.commons.io.IOUtils;
import org.geoserver.appschema.smart.SmartAppSchemaPostgisTestSupport;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geoserver.appschema.smart.utils.LoggerDomainModelVisitor;
import org.geoserver.appschema.smart.utils.SmartAppSchemaTestHelper;
import org.junit.Test;

/**
 * Tests related to a simple DomainModelVisitor (which logs DomainModel visited nodes)
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class DomainModelVisitorTest extends SmartAppSchemaPostgisTestSupport {

    @Test
    public void testDomainModelVisitWithStations() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_stations");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        LoggerDomainModelVisitor dmv = new LoggerDomainModelVisitor();
        dm.accept(dmv);

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream("meteo-stations-logvisitor.txt");
        String expected = IOUtils.toString(is, StandardCharsets.UTF_8);
        assertEquals(expected, dmv.getLog());
    }

    @Test
    public void testDomainModelVisitWithObservations() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_observations");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        LoggerDomainModelVisitor dmv = new LoggerDomainModelVisitor();
        dm.accept(dmv);

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream("meteo-observations-logvisitor.txt");
        String expected = IOUtils.toString(is, StandardCharsets.UTF_8);
        assertEquals(expected, dmv.getLog());
    }
}
