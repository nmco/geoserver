package org.geoserver.appschema.smart.domain.meteo;

import java.io.InputStream;
import java.sql.DatabaseMetaData;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.geoserver.appschema.smart.SmartAppSchemaPostgisTestSupport;
import org.geoserver.appschema.smart.domain.DomainModelBuilder;
import org.geoserver.appschema.smart.domain.DomainModelConfig;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geoserver.appschema.smart.utils.SmartAppSchemaTestHelper;
import org.geoserver.appschema.smart.visitors.appschema.AppSchemaVisitor;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Tests related to SmartAppSchemaDomainModelVisitor
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class SmartAppSchemaDomainModelVisitorTest extends SmartAppSchemaPostgisTestSupport {

    public SmartAppSchemaDomainModelVisitorTest() {
        SCHEMA = "smartappschematest";
        NAMESPACE_PREFIX = "mt";
        TARGET_NAMESPACE = "http://www.geo-solutions.it/smartappschema/1.0";
        MOCK_SQL_SCRIPT = "meteo_db.sql";
    }

    @Test
    public void testObservationsRootEntity() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_observations");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        AppSchemaVisitor dmv =
                new AppSchemaVisitor(
                        NAMESPACE_PREFIX, TARGET_NAMESPACE, "./meteo-observations-gml.xsd");
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/meteo-observations-appschema.xml");*/

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream("meteo-observations-appschema.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document control = dBuilder.parse(is);

        // clean sourceDataStores nodes from control and dmv doc to allow assertion based on xml
        // comparision
        removeSourceDataStoresNode(control);
        removeSourceDataStoresNode(dmv.getDocument());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Diff d = XMLUnit.compareXML(control, dmv.getDocument());

        assertEquals(true, d.similar());
    }

    @Test
    public void testStationsRootEntity() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_stations");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        AppSchemaVisitor dmv =
                new AppSchemaVisitor(
                        NAMESPACE_PREFIX, TARGET_NAMESPACE, "./meteo-stations-gml.xsd");
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/meteo-stations-appschema.xml");*/

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream("meteo-stations-appschema.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document control = dBuilder.parse(is);

        // clean sourceDataStores nodes from control and dmv doc to allow assertion based on xml
        // comparision
        removeSourceDataStoresNode(control);
        removeSourceDataStoresNode(dmv.getDocument());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Diff d = XMLUnit.compareXML(control, dmv.getDocument());

        assertEquals(true, d.similar());
    }

    @Test
    public void testParametersRootEntity() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("meteo_parameters");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        AppSchemaVisitor dmv =
                new AppSchemaVisitor(
                        NAMESPACE_PREFIX, TARGET_NAMESPACE, "./meteo-parameters-gml.xsd");
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/meteo-parameters-appschema.xml");*/

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream("meteo-parameters-appschema.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document control = dBuilder.parse(is);

        // clean sourceDataStores nodes from control and dmv doc to allow assertion based on xml
        // comparision
        removeSourceDataStoresNode(control);
        removeSourceDataStoresNode(dmv.getDocument());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Diff d = XMLUnit.compareXML(control, dmv.getDocument());

        assertEquals(true, d.similar());
    }
}
