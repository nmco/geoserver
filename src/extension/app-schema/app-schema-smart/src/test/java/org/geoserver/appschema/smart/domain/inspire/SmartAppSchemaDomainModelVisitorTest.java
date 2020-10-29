package org.geoserver.appschema.smart.domain.inspire;

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
 * Tests for inspire use case
 *
 * @author Jose Macchi - GeoSolutions
 */
public final class SmartAppSchemaDomainModelVisitorTest extends SmartAppSchemaPostgisTestSupport {

    public SmartAppSchemaDomainModelVisitorTest() {
        SCHEMA = "smartappschematest";
        NAMESPACE_PREFIX = "inspire";
        TARGET_NAMESPACE = "http://www.api4inspire.it/smartappschema/1.0";
        MOCK_SQL_SCRIPT = "inspire_db.sql";
    }

    @Test
    public void testIndicatorInitiativeAssRootEntity() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("indicator_initiative_ass");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        AppSchemaVisitor dmv =
                new AppSchemaVisitor(
                        NAMESPACE_PREFIX,
                        TARGET_NAMESPACE,
                        "./inspire-indicator_initiative_ass-gml.xsd");
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/inspire-indicator_initiative_ass-appschema.xml");*/

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream(
                        "inspire-indicator_initiative_ass-appschema.xml");
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
    public void testIndicatorsRootEntity() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("indicators");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        AppSchemaVisitor dmv =
                new AppSchemaVisitor(
                        NAMESPACE_PREFIX, TARGET_NAMESPACE, "./inspire-indicators-gml.xsd");
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/inspire-indicators-appschema.xml");*/

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream("inspire-indicators-appschema.xml");
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
    public void testInitiativeRootEntity() throws Exception {
        DatabaseMetaData metaData = this.setup.getDataSource().getConnection().getMetaData();
        DataStoreMetadata dsm = this.getDataStoreMetadata(metaData);
        DomainModelConfig dmc = new DomainModelConfig();
        dmc.setRootEntityName("initiative");
        DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
        DomainModel dm = dmb.buildDomainModel();
        AppSchemaVisitor dmv =
                new AppSchemaVisitor(
                        NAMESPACE_PREFIX, TARGET_NAMESPACE, "./inspire-initiative-gml.xsd");
        dm.accept(dmv);

        /*SmartAppSchemaTestHelper.saveDocumentToFile(
        dmv.getDocument(), "/inspire-initiative-appschema.xml");*/

        InputStream is =
                SmartAppSchemaTestHelper.getResourceAsStream("inspire-initiative-appschema.xml");
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
