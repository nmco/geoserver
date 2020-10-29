package org.geoserver.appschema.smart;

import static org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory.DBTYPE;
import static org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory.NAMESPACE;
import static org.geotools.data.postgis.PostgisNGDataStoreFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataConfig;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataFactory;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcDataStoreMetadataConfig;
import org.geoserver.appschema.smart.metadata.jdbc.SmartAppSchemaPostgisTestSetup;
import org.geoserver.appschema.smart.metadata.jdbc.utils.JdbcUrlSplitter;
import org.geoserver.appschema.smart.utils.SmartAppSchemaTestHelper;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.impl.NamespaceInfoImpl;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.AdminRequest;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.geoserver.web.data.store.DataAccessNewPage;
import org.geoserver.web.data.store.panel.WorkspacePanel;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;
import org.geotools.test.FixtureUtilities;
import org.geotools.util.logging.Logging;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.jdbc.SslMode;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Implementation of GeoServerWicketTestSupport for SmartAppSchema Postgis tests, including
 * Geoserver and Wicket support.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class SmartAppSchemaGeoServerTestSupport extends GeoServerWicketTestSupport {

    static final Logger LOGGER = Logging.getLogger(SmartAppSchemaGeoServerTestSupport.class);

    public static final String ONLINE_TEST_PROFILE = "onlineTestProfile";
    protected static Map<String, Boolean> online = new HashMap<String, Boolean>();
    protected static Map<String, Boolean> found = new HashMap<String, Boolean>();
    protected static Param PASSWORD = new Param("password", String.class, "Password", true);

    public String SCHEMA = SmartAppSchemaPostgisTestSetup.ONLINE_DB_SCHEMA;
    public String NAMESPACE_PREFIX = "mt";
    public String TARGET_NAMESPACE = "http://www.geo-solutions.it/smartappschema/1.0";

    private final DataAccessFactory dataStoreFactory = new PostgisSmartAppSchemaDataAccessFactory();

    protected String MOCK_SQL_SCRIPT = "meteo_db.sql";
    protected SmartAppSchemaPostgisTestSetup setup;
    protected JDBCDataStore dataStore;
    protected SQLDialect dialect;
    protected Properties fixture;

    protected DataAccessNewPage startPage() {
        AdminRequest.start(new Object());
        login();
        final DataAccessNewPage page = new DataAccessNewPage(dataStoreFactory.getDisplayName());
        tester.startPage(page);
        return page;
    }

    protected DataStoreMetadata getDataStoreMetadata(DatabaseMetaData metaData) throws Exception {
        DataStoreMetadataConfig config =
                new JdbcDataStoreMetadataConfig(SCHEMA, metaData.getConnection(), null, SCHEMA);
        DataStoreMetadata dsm = (new DataStoreMetadataFactory()).getDataStoreMetadata(config);
        return dsm;
    }

    /**
     * Helper method that allows to remove sourceDataStore node from AppSchema xml doc. Useful to
     * clean xml docs when required to compare assertXML (since those sections of documents contains
     * specific information from dataStores based on JDBC Connection, it's required to avoid the
     * comparision.
     *
     * @param appSchemaDoc
     */
    protected void removeSourceDataStoresNode(Document appSchemaDoc) {
        NodeList sds = appSchemaDoc.getElementsByTagName("sourceDataStores");
        if (sds != null && sds.getLength() > 0) {
            sds.item(0).getParentNode().removeChild(sds.item(0));
        }
    }

    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        configureFixture();
        connect();
        // setup geoserver required namespaces and data for SmartAppSchema tests
        setupGeoserverTestData();
    }

    @Override
    protected void onTearDown(SystemTestData testData) throws Exception {
        disconnect();
    }

    protected SmartAppSchemaPostgisTestSetup createTestSetup() {
        String sql;
        try {
            sql =
                    IOUtils.toString(
                            SmartAppSchemaTestHelper.getResourceAsStream(
                                    "mockdata/" + MOCK_SQL_SCRIPT),
                            Charset.defaultCharset());
            return SmartAppSchemaPostgisTestSetup.getInstance(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void connect() throws Exception {
        // create the test harness
        if (setup == null) {
            setup = createTestSetup();
        }

        setup.setFixture(fixture);
        setup.setUp();

        // initialize the database
        setup.initializeDatabase();

        // initialize the data
        setup.setUpData();

        // create the dataStore
        HashMap params = createDataStoreFactoryParams();
        try {
            HashMap temp = (HashMap) params.clone();
            temp.putAll(fixture);
            dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(temp);
        } catch (Exception e) {
            // ignore
        }
        if (dataStore == null) {
            JDBCDataStoreFactory factory = setup.createDataStoreFactory();
            dataStore = factory.createDataStore(params);
        }
        setup.setUpDataStore(dataStore);
        dialect = dataStore.getSQLDialect();
    }

    protected HashMap createDataStoreFactoryParams() throws Exception {
        HashMap params = new HashMap();
        params.put(JDBCDataStoreFactory.NAMESPACE.key, "http://www.geotools.org/test");
        params.put(JDBCDataStoreFactory.SCHEMA.key, "geotools");
        params.put(JDBCDataStoreFactory.DATASOURCE.key, setup.getDataSource());
        params.put(JDBCDataStoreFactory.BATCH_INSERT_SIZE.key, 100);
        return params;
    }

    protected void disconnect() throws Exception {
        setup.tearDown();
        dataStore.dispose();
    }

    protected String getFixtureId() {
        return createTestSetup().getDatabaseID();
    }

    protected Properties createOfflineFixture() {
        return createTestSetup().createOfflineFixture();
    }

    protected Properties createExampleFixture() {
        return createTestSetup().createExampleFixture();
    }

    /** Load fixture configuration. Create example if absent. */
    protected void configureFixture() {
        if (fixture == null) {
            String fixtureId = getFixtureId();
            if (fixtureId == null) {
                return; // not available (turn test off)
            }
            try {
                // load the fixture
                File base = FixtureUtilities.getFixtureDirectory();
                // look for a "profile", these can be used to group related fixtures
                String profile = System.getProperty(ONLINE_TEST_PROFILE);
                if (profile != null && !"".equals(profile)) {
                    base = new File(base, profile);
                }
                File fixtureFile = FixtureUtilities.getFixtureFile(base, fixtureId);
                Boolean exists = found.get(fixtureFile.getCanonicalPath());
                if (exists == null || exists.booleanValue()) {
                    if (fixtureFile.exists()) {
                        fixture = FixtureUtilities.loadProperties(fixtureFile);
                        found.put(fixtureFile.getCanonicalPath(), true);
                    } else {
                        // no fixture file, if no profile was specified write out a template
                        // fixture using the offline fixture properties
                        if (profile == null) {
                            Properties exampleFixture = createExampleFixture();
                            if (exampleFixture != null) {
                                File exFixtureFile =
                                        new File(fixtureFile.getAbsolutePath() + ".example");
                                if (!exFixtureFile.exists()) {
                                    createExampleFixture(exFixtureFile, exampleFixture);
                                }
                            }
                        }
                        found.put(fixtureFile.getCanonicalPath(), false);
                    }
                }
                if (fixture == null) {
                    fixture = createOfflineFixture();
                }
                if (fixture == null && exists == null) {
                    // only report if exists == null since it means that this is
                    // the first time trying to load the fixture
                    FixtureUtilities.printSkipNotice(fixtureId, fixtureFile);
                }
            } catch (Exception e) {
                java.util.logging.Logger.getGlobal().log(java.util.logging.Level.INFO, "", e);
            }
        }
    }

    protected Map<String, Serializable> getDataStoreParameters() {
        Properties db = getFixture();
        if (db == null) {
            configureFixture();
            db = getFixture();
        }
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        JdbcUrlSplitter jdbcUrl = new JdbcUrlSplitter(db.getProperty("url"));
        params.put(HOST.key, jdbcUrl.host);
        params.put(DATABASE.key, jdbcUrl.database);
        params.put(PORT.key, jdbcUrl.port);
        params.put(USER.key, db.getProperty(USER.key));
        params.put(PASSWD.key, db.getProperty(PASSWORD.key));
        params.put(DBTYPE.key, PostgisSmartAppSchemaDataAccessFactory.DBTYPE_STRING);
        params.put(NAMESPACE.key, TARGET_NAMESPACE);
        params.put(PostgisNGDataStoreFactory.SCHEMA.key, SCHEMA);
        params.put(SSL_MODE.key, SslMode.DISABLE);
        return params;
    }

    protected void setFormValues(
            FormTester ft,
            String datastoreName,
            Map<String, Serializable> params,
            String rootentity) {
        String host = (String) params.get(HOST.key);
        String port = (String) params.get(PORT.key);
        String database = (String) params.get(DATABASE.key);
        String schema = (String) params.get(PostgisNGDataStoreFactory.SCHEMA.key);
        String username = (String) params.get(USER.key);
        String password = (String) params.get(PASSWD.key);

        ft.setValue(
                "parametersPanel:parameters:2:parameterPanel:border:border_body:paramValue", host);
        ft.setValue(
                "parametersPanel:parameters:3:parameterPanel:border:border_body:paramValue", port);
        ft.setValue(
                "parametersPanel:parameters:4:parameterPanel:border:border_body:paramValue",
                database);
        ft.setValue(
                "parametersPanel:parameters:5:parameterPanel:border:border_body:paramValue",
                schema);
        ft.setValue(
                "parametersPanel:parameters:6:parameterPanel:border:border_body:paramValue",
                username);
        ft.setValue(
                "parametersPanel:parameters:7:parameterPanel:border:border_body:paramValue",
                password);
        ft.setValue(
                "parametersPanel:parameters:9:parameterPanel:border:border_body:paramValue",
                rootentity);
        ft.setValue("dataStoreNamePanel:border:border_body:paramValue", datastoreName);
    }

    private void createExampleFixture(File exFixtureFile, Properties exampleFixture) {
        try {
            exFixtureFile.getParentFile().mkdirs();
            exFixtureFile.createNewFile();

            try (FileOutputStream fout = new FileOutputStream(exFixtureFile)) {

                exampleFixture.store(
                        fout,
                        "This is an example fixture. Update the "
                                + "values and remove the .example suffix to enable the test");
                fout.flush();
            }
        } catch (IOException ioe) {
            java.util.logging.Logger.getGlobal().log(java.util.logging.Level.INFO, "", ioe);
        }
    }

    private void setupGeoserverTestData() {
        // insert workspace with defined prefix into geoserver
        Catalog catalog = ((GeoServer) GeoServerExtensions.bean("geoServer")).getCatalog();
        // create the namespace
        NamespaceInfoImpl namespace = new NamespaceInfoImpl();
        namespace.setPrefix(NAMESPACE_PREFIX);
        namespace.setURI(TARGET_NAMESPACE);
        namespace.setIsolated(false);
        catalog.add(namespace);
        // create the workspace
        WorkspaceInfoImpl workspace = new WorkspaceInfoImpl();
        workspace.setName(NAMESPACE_PREFIX);
        workspace.setIsolated(false);
        catalog.add(workspace);
    }

    public Properties getFixture() {
        return fixture;
    }

    @After
    public void clearAdminRequest() {
        AdminRequest.finish();
    }

    @Ignore
    @Test
    public void testPageRendersOnLoad() {
        startPage();
        tester.assertLabel("dataStoreForm:storeType", dataStoreFactory.getDisplayName());
        tester.assertLabel("dataStoreForm:storeTypeDescription", dataStoreFactory.getDescription());
        tester.assertComponent("dataStoreForm:workspacePanel", WorkspacePanel.class);
    }

    @Ignore
    @Test
    public void testDbtypeParameterHidden() {
        startPage();
        // check the dbtype field is not visible
        MarkupContainer container =
                (MarkupContainer)
                        tester.getComponentFromLastRenderedPage(
                                "dataStoreForm:parametersPanel:parameters:0");
        assertEquals("dbtype", container.getDefaultModelObject());
        assertFalse(container.get("parameterPanel").isVisible());
    }

    @Ignore
    @Test
    public void testParametersListed() {
        startPage();
        MarkupContainer container =
                (MarkupContainer)
                        tester.getComponentFromLastRenderedPage(
                                "dataStoreForm:parametersPanel:parameters:0");
        assertEquals("dbtype", container.getDefaultModelObject());
        assertFalse(container.get("parameterPanel").isVisible());
    }

    @Ignore
    @Test
    public void testDataStoreParametersAreCreated() {
        startPage();
        List parametersListViewValues =
                Arrays.asList(
                        new Object[] {
                            "dbtype",
                            "namespace",
                            "host",
                            "port",
                            "database",
                            "schema",
                            "user",
                            "passwd",
                            "SSL mode",
                            "root entity"
                        });
        tester.assertListView("dataStoreForm:parametersPanel:parameters", parametersListViewValues);
    }
}
