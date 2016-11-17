package org.geoserver.test.mongo;

import org.geoserver.test.AbstractAppSchemaTestSupport;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.hamcrest.CoreMatchers.is;

public class AppSchemaMongoTest extends AbstractAppSchemaTestSupport {

    @Before
    public void beforeTest() {
        Assume.assumeThat("Mongo running instance not found, app-schema mongo tests will be ignored.",
                AppSchemaMongoTestUtils.connected(), is(true));
        String stations = AppSchemaMongoTestUtils.readResourceContent("test-data/stations.json");
        AppSchemaMongoTestUtils.dropDatabase();
        AppSchemaMongoTestUtils.insertJson(stations, "geometry", "stations");
    }

    @Override
    protected AppSchemaMongoMockData createTestData() {
        return new AppSchemaMongoMockData();
    }

    @Test
    public void testNonValidNestedGML() throws Exception {
        // get the complex features encoded in GML
        Document result = getAsDOM("wfs?request=GetFeature&version=1.1.0&typename=st:StationFeature");
        /*// checking that we have the correct number of elements
        assertXpathCount(3, "//ex:ParentFeature", result);
        assertXpathCount(5, "//ex:ParentFeature/ex:nestedFeature", result);
        assertXpathCount(5, "//ex:ParentFeature/ex:nestedFeature/ex:nestedValue", result);
        // checking the content of the first feature
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.1']/ex:parentValue[text()='string_one']", result);
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.1']/ex:nestedFeature/ex:nestedValue[text()='1GRAV']", result);
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.1']/ex:nestedFeature/ex:nestedValue[text()='1TILL']", result);
        // checking the content of the second feature
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.2']/ex:parentValue[text()='string_two']", result);
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.2']/ex:nestedFeature/ex:nestedValue[text()='1GRAV']", result);
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.2']/ex:nestedFeature/ex:nestedValue[text()='1TILL']", result);
        // checking the content of the third feature
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.3']/ex:parentValue[text()='string_three']", result);
        assertXpathCount(1, "//ex:ParentFeature[@gml:id='sc.3']/ex:nestedFeature/ex:nestedValue[text()='6ALLU']", result);*/
    }
}
