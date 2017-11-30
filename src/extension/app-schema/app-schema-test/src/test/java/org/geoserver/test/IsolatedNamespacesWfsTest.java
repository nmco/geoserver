
/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.test;

import org.custommonkey.xmlunit.XpathEngine;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that isolated workspaces \ namespaces allow the publishing of the same
 * complex feature type multiple times.
 */
public final class IsolatedNamespacesWfsTest extends AbstractAppSchemaTestSupport {

    // workspaces isolation first use case GML 3.1 namespaces
    protected static final String STATIONS_1_PREFIX_GML31 = "st_1_gml31";
    protected static final String MEASUREMENTS_1_PREFIX_GML31 = "ms_1_gml31";

    // workspaces isolation first use case GML 3.2 namespaces
    protected static final String STATIONS_1_PREFIX_GML32 = "st_1_gml32";
    protected static final String MEASUREMENTS_1_PREFIX_GML32 = "ms_1_gml32";

    // workspaces isolation second use case GML 3.1 namespaces
    protected static final String STATIONS_2_PREFIX_GML31 = "st_2_gml31";
    protected static final String MEASUREMENTS_2_PREFIX_GML31 = "ms_2_gml31";

    // workspaces isolation second use case GML 3.2 namespaces
    protected static final String STATIONS_2_PREFIX_GML32 = "st_2_gml32";
    protected static final String MEASUREMENTS_2_PREFIX_GML32 = "ms_2_gml32";

    // xpath engines used to check WFS responses
    private XpathEngine WFS11_XPATH_ENGINE;
    private XpathEngine WFS20_XPATH_ENGINE;

    @Before
    public void beforeTest() {
        // instantiate WFS 1.1 xpath engine
        WFS11_XPATH_ENGINE = StationsMockData.buildXpathEngine(
                getTestData().getNamespaces(),
                "wfs", "http://www.opengis.net/wfs",
                "gml", "http://www.opengis.net/gml");
        // instantiate WFS 2.0 xpath engine
        WFS20_XPATH_ENGINE = StationsMockData.buildXpathEngine(
                getTestData().getNamespaces(),
                "wfs", "http://www.opengis.net/wfs/2.0",
                "gml", "http://www.opengis.net/gml/3.2");
    }

    @Override
    protected StationsMockData createTestData() {
        // instantiate our custom complex types
        return new MockData();
    }

    /**
     * Helper class that will setup custom complex feature types using the stations data set.
     */
    private static final class MockData extends StationsMockData {

        @Override
        public void addContent() {
            super.addContent();

            // GML 3.1 parameters for files parameterization
            Map<String, String> gml31Parameters = new HashMap<>();
            gml31Parameters.put("GML_PREFIX", "gml31");
            gml31Parameters.put("GML_NAMESPACE", "http://www.opengis.net/gml");
            gml31Parameters.put("GML_LOCATION", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
            // GML 3.2 parameters for files parameterization
            Map<String, String> gml32Parameters = new HashMap<>();
            gml32Parameters.put("GML_PREFIX", "gml32");
            gml32Parameters.put("GML_NAMESPACE", "http://www.opengis.net/gml/3.2");
            gml32Parameters.put("GML_LOCATION", "http://schemas.opengis.net/gml/3.2.1/gml.xsd");

            // add first use case namespaces
            putIsolatedNamespace(STATIONS_1_PREFIX_GML31, STATIONS_URI_GML31);
            putIsolatedNamespace(MEASUREMENTS_1_PREFIX_GML31, MEASUREMENTS_URI_GML31);
            putIsolatedNamespace(STATIONS_1_PREFIX_GML32, STATIONS_URI_GML32);
            putIsolatedNamespace(MEASUREMENTS_1_PREFIX_GML32, MEASUREMENTS_URI_GML32);
            // add first use case features types
            gml31Parameters.put("USE_CASE", "1");
            gml32Parameters.put("USE_CASE", "1");
            addMeasurementFeatureType(MEASUREMENTS_1_PREFIX_GML31, "gml31", "measurements1", "isolatedMappings/measurements1.xml", gml31Parameters);
            addStationFeatureType(STATIONS_1_PREFIX_GML31, "gml31", "stations1", "isolatedMappings/stations1.xml", gml31Parameters);
            addMeasurementFeatureType(MEASUREMENTS_1_PREFIX_GML32, "gml32", "measurements1", "isolatedMappings/measurements1.xml", gml32Parameters);
            addStationFeatureType(STATIONS_1_PREFIX_GML32, "gml32", "stations1", "isolatedMappings/stations1.xml", gml32Parameters);

            // add second use case namespaces
            putIsolatedNamespace(STATIONS_2_PREFIX_GML31, STATIONS_URI_GML31);
            putIsolatedNamespace(MEASUREMENTS_2_PREFIX_GML31, MEASUREMENTS_URI_GML31);
            putIsolatedNamespace(STATIONS_2_PREFIX_GML32, STATIONS_URI_GML32);
            putIsolatedNamespace(MEASUREMENTS_2_PREFIX_GML32, MEASUREMENTS_URI_GML32);
            // add second use case features types
            gml31Parameters.put("USE_CASE", "2");
            gml32Parameters.put("USE_CASE", "2");
            addStationFeatureType(STATIONS_2_PREFIX_GML31, "gml31", "stations2", "isolatedMappings/stations2.xml",
                    "measurements2", "isolatedMappings/measurements2.xml", gml31Parameters);
            addStationFeatureType(STATIONS_2_PREFIX_GML32, "gml32", "stations2", "isolatedMappings/stations2.xml",
                    "measurements2", "isolatedMappings/measurements2.xml", gml32Parameters);
        }
    }

    @Test
    public void testIsolatedWorkspacesWithFirstUseCaseWfs11() {
        Document document = getAsDOM("wfs?request=GetFeature&version=1.1.0&typename=st_1_gml31:Station_gml31");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_1_gml31:Station_gml31[@gml:id='st.1'][st_1_gml31:name='isolated_1_station1']");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_1_gml31:Station_gml31[@gml:id='st.1']/st_1_gml31:measurements/ms_1_gml31:Measurement_gml31[ms_1_gml31:name='isolated_1_temperature']");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_1_gml31:Station_gml31[@gml:id='st.1']/st_1_gml31:measurements/ms_1_gml31:Measurement_gml31[ms_1_gml31:name='isolated_1_wind']");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_1_gml31:Station_gml31[@gml:id='st.1']/st_1_gml31:location/gml:Point[gml:pos='1.0 -1.0']");
    }

    @Test
    public void testIsolatedWorkspacesWithFirstUseCaseWfs20() {
        Document document = getAsDOM("wfs?request=GetFeature&version=2.0&typename=st_1_gml32:Station_gml32");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1'][st_1_gml32:name='isolated_1_station1']");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1']/st_1_gml32:measurements/ms_1_gml32:Measurement_gml32[ms_1_gml32:name='isolated_1_temperature']");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1']/st_1_gml32:measurements/ms_1_gml32:Measurement_gml32[ms_1_gml32:name='isolated_1_wind']");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1']/st_1_gml32:location/gml:Point[gml:pos='1.0 -1.0']");
    }

    @Test
    public void testIsolatedWorkspacesWithSecondUseCaseWfs11() {
        Document document = getAsDOM("wfs?request=GetFeature&version=1.1.0&typename=st_2_gml31:Station_gml31");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_2_gml31:Station_gml31[@gml:id='st.1'][st_2_gml31:name='isolated_2_station1']");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_2_gml31:Station_gml31[@gml:id='st.1']/st_2_gml31:measurements/ms_2_gml31:Measurement_gml31[ms_2_gml31:name='isolated_2_temperature']");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_2_gml31:Station_gml31[@gml:id='st.1']/st_2_gml31:measurements/ms_2_gml31:Measurement_gml31[ms_2_gml31:name='isolated_2_wind']");
        checkCount(WFS11_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/gml:featureMember/" +
                "st_2_gml31:Station_gml31[@gml:id='st.1']/st_2_gml31:location/gml:Point[gml:pos='1.0 -1.0']");
    }

    @Test
    public void testIsolatedWorkspacesWithSecondUseCaseWfs20() {
        Document document = getAsDOM("wfs?request=GetFeature&version=2.0&typename=st_1_gml32:Station_gml32");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1'][st_1_gml32:name='isolated_1_station1']");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1']/st_1_gml32:measurements/ms_1_gml32:Measurement_gml32[ms_1_gml32:name='isolated_1_temperature']");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1']/st_1_gml32:measurements/ms_1_gml32:Measurement_gml32[ms_1_gml32:name='isolated_1_wind']");
        checkCount(WFS20_XPATH_ENGINE, document, 1, "/wfs:FeatureCollection/wfs:member/" +
                "st_1_gml32:Station_gml32[@gml:id='st.1']/st_1_gml32:location/gml:Point[gml:pos='1.0 -1.0']");
    }

    /**
     * Helper method that evaluates a xpath and checks if the number of nodes found
     * correspond to the expected number,
     */
    private void checkCount(XpathEngine xpathEngine, Document document, int expectedCount, String xpath) {
        try {
            // evaluate the xpath and compare the number of nodes found
            assertThat(xpathEngine.getMatchingNodes(xpath, document).getLength(), is(expectedCount));
        } catch (Exception exception) {
            throw new RuntimeException("Error evaluating xpath.", exception);
        }
    }
}
