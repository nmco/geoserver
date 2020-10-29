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
import org.geoserver.appschema.smart.visitors.gml.GmlSchemaVisitor;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Tests related to GMLDomainModelVisitor
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class GmlDomainModelVisitorTest extends SmartAppSchemaPostgisTestSupport {

    public GmlDomainModelVisitorTest() {
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
        GmlSchemaVisitor dmv = new GmlSchemaVisitor(NAMESPACE_PREFIX, TARGET_NAMESPACE);
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/meteo-observations-gml.xsd");*/

        InputStream is = SmartAppSchemaTestHelper.getResourceAsStream("meteo-observations-gml.xsd");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document control = dBuilder.parse(is);
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
        GmlSchemaVisitor dmv = new GmlSchemaVisitor(NAMESPACE_PREFIX, TARGET_NAMESPACE);
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/meteo-stations-gml.xsd");*/

        InputStream is = SmartAppSchemaTestHelper.getResourceAsStream("meteo-stations-gml.xsd");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document control = dBuilder.parse(is);
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
        GmlSchemaVisitor dmv = new GmlSchemaVisitor(NAMESPACE_PREFIX, TARGET_NAMESPACE);
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/meteo-parameters-gml.xsd");*/

        InputStream is = SmartAppSchemaTestHelper.getResourceAsStream("meteo-parameters-gml.xsd");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document control = dBuilder.parse(is);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Diff d = XMLUnit.compareXML(control, dmv.getDocument());

        assertEquals(true, d.similar());
    }
}
