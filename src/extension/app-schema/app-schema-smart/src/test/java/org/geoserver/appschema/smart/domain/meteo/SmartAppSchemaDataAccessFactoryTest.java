package org.geoserver.appschema.smart.domain.meteo;

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
 * Tests related to SmartAppSchemaDataAccessFactory in the context of Meteo domain model.
 *
 * @author Jose Macchi - GeoSolutions
 */
public class SmartAppSchemaDataAccessFactoryTest extends SmartAppSchemaGeoServerTestSupport {

    public SmartAppSchemaDataAccessFactoryTest() {
        SCHEMA = "smartappschematest";
        NAMESPACE_PREFIX = "mt";
        TARGET_NAMESPACE = "http://www.geo-solutions.it/smartappschema/1.0";
        MOCK_SQL_SCRIPT = "meteo_db.sql";
    }

    @Ignore
    @Test
    public void testMeteoStationsSchema() throws Exception {
        Map<String, Serializable> params = getDataStoreParameters();
        // set root entity of domainmodel
        params.put(ROOT_ENTITY.key, "meteo_stations");
        // create dataaccessfactory and assert resulting features
        DataAccessFactory factory = new PostgisSmartAppSchemaDataAccessFactory();
        assertTrue(factory.canProcess(params));
        DataAccess store = factory.createDataStore(params);
        ArrayList<String> expectedFeatures = new ArrayList<String>();
        expectedFeatures.add(TARGET_NAMESPACE + ":meteo_observations_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":meteo_parameters_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":meteo_stations_t");
        List<Name> list = store.getNames();
        for (Name name : list) {
            FeatureSource fs = store.getFeatureSource(name);
            assertTrue(expectedFeatures.contains(fs.getName().getURI()));
        }
        store.dispose();
    }

    @Ignore
    @Test
    public void testMeteoObservationsSchema() throws Exception {
        Map<String, Serializable> params = getDataStoreParameters();
        // set root entity of domainmodel
        params.put(ROOT_ENTITY.key, "meteo_observations");
        // create dataaccessfactory and assert resulting features
        DataAccessFactory factory = new PostgisSmartAppSchemaDataAccessFactory();
        assertTrue(factory.canProcess(params));
        DataAccess store = factory.createDataStore(params);
        ArrayList<String> expectedFeatures = new ArrayList<String>();
        expectedFeatures.add(TARGET_NAMESPACE + ":meteo_observations_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":meteo_parameters_t");
        expectedFeatures.add(TARGET_NAMESPACE + ":meteo_stations_t");
        List<Name> list = store.getNames();
        for (Name name : list) {
            FeatureSource fs = store.getFeatureSource(name);
            assertTrue(expectedFeatures.contains(fs.getName().getURI()));
        }
        store.dispose();
    }
}
