package org.geoserver.appschema.smart.domain.inspire;

import static org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory.ROOT_ENTITY;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.geoserver.appschema.smart.SmartAppSchemaGeoServerTestSupport;
import org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.FeatureSource;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.type.Name;

/**
 * Tests related to SmartAppSchemaDataAccessFactory in the context of Inspire domain model.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class SmartAppSchemaDataAccessFactoryTest extends SmartAppSchemaGeoServerTestSupport {

    public SmartAppSchemaDataAccessFactoryTest() {
        SCHEMA = "smartappschematest";
        NAMESPACE_PREFIX = "inspire";
        TARGET_NAMESPACE = "http://www.api4inspire.it/smartappschema/1.0";
        MOCK_SQL_SCRIPT = "inspire_db.sql";
    }

    @Ignore
    @Test
    public void testInspireInitiativeSchema() throws Exception {
        Map<String, Serializable> params = getDataStoreParameters();
        params.put(ROOT_ENTITY.key, "initiative");
        // create dataaccessfactory and assert resulting features
        DataAccessFactory factory = new PostgisSmartAppSchemaDataAccessFactory();
        assertTrue(factory.canProcess(params));
        DataAccess store = factory.createDataStore(params);
        ArrayList<String> expectedFeatures = new ArrayList<String>();
        expectedFeatures.add(TARGET_NAMESPACE + ":initiative_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":access_mode_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":data_type_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":output_format_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":vocabularies_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":endpoint_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":indicators_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":indicator_initiative_ass_t");
        List<Name> list = store.getNames();
        for (Name name : list) {
            FeatureSource fs = store.getFeatureSource(name);
            assertTrue(expectedFeatures.contains(fs.getName().getURI()));
        }
        store.dispose();
    }
}
