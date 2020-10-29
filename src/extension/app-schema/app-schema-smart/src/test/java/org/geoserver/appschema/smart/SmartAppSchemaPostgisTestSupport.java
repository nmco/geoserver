package org.geoserver.appschema.smart;

import java.nio.charset.Charset;
import java.sql.DatabaseMetaData;
import org.apache.commons.io.IOUtils;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataConfig;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataFactory;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcDataStoreMetadataConfig;
import org.geoserver.appschema.smart.metadata.jdbc.SmartAppSchemaPostgisTestSetup;
import org.geoserver.appschema.smart.utils.SmartAppSchemaTestHelper;
import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.JDBCTestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public abstract class SmartAppSchemaPostgisTestSupport extends JDBCTestSupport {

    public String SCHEMA = SmartAppSchemaPostgisTestSetup.ONLINE_DB_SCHEMA;
    public String NAMESPACE_PREFIX = "mt";
    public String TARGET_NAMESPACE = "http://www.geo-solutions.it/smartappschema/1.0";
    protected String MOCK_SQL_SCRIPT = "meteo_db.sql";

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
    protected JDBCTestSetup createTestSetup() {
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
}
