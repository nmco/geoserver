/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.jsonld.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.data.test.MockData;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class JSONLDGetFeatureSimpleFeatureTest extends GeoServerSystemTestSupport {
    Catalog catalog;
    FeatureTypeInfo typeInfo;
    GeoServerDataDirectory dd;

    @Before
    public void before() throws IOException {
        catalog = getCatalog();

        typeInfo =
                catalog.getFeatureTypeByName(MockData.CDF_PREFIX, MockData.FIFTEEN.getLocalPart());
        dd = (GeoServerDataDirectory) applicationContext.getBean("dataDirectory");
        File file =
                dd.getResourceLoader()
                        .createFile(
                                "workspaces/cdf/"
                                        + typeInfo.getStore().getName()
                                        + "/"
                                        + typeInfo.getName(),
                                typeInfo.getName() + ".json");
        dd.getResourceLoader().copyFromClassPath("Fifteen.json", file, getClass());
    }

    @Test
    public void testJsonLdResponse() throws Exception {
        StringBuffer sb = new StringBuffer("wfs?request=GetFeature&version=2.0");
        sb.append("&TYPENAME=cdf:Fifteen&outputFormat=");
        sb.append("application%2Fld%2Bjson");
        JSONObject result = (JSONObject) getJsonLd(sb.toString());
        JSONObject context = (JSONObject) result.get("@context");
        assertNotNull(context);
        JSONArray features = (JSONArray) result.get("features");
        assertEquals(features.size(), 15);
        for (int i = 0; i < features.size(); i++) {
            JSONObject feature = (JSONObject) features.get(i);
            checkFeature(feature);
        }
    }

    @Test
    public void testJsonLdResponseOGCAPI() throws Exception {
        String path =
                "ogc/features/collections/" + "cdf:Fifteen" + "/items?f=application%2Fld%2Bjson";
        JSONObject result = (JSONObject) getJsonLd(path);
        JSONObject context = (JSONObject) result.get("@context");
        assertNotNull(context);
        JSONArray features = (JSONArray) result.get("features");
        assertEquals(features.size(), 15);
        for (int i = 0; i < features.size(); i++) {
            JSONObject feature = (JSONObject) features.get(i);
            checkFeature(feature);
        }
    }

    protected JSON getJsonLd(String path) throws Exception {
        MockHttpServletResponse response = getAsServletResponse(path);
        assertEquals(response.getContentType(), "application/ld+json");
        return json(response);
    }

    @After
    public void cleanup() {
        dd.getResourceLoader()
                .get(
                        "workspaces/cdf/"
                                + typeInfo.getStore().getName()
                                + "/"
                                + typeInfo.getName()
                                + "/"
                                + typeInfo.getName()
                                + ".json")
                .delete();
    }

    private void checkFeature(JSONObject feature) {
        assertNotNull(feature.getString("id"));
        assertNotNull(feature.getString("boundedBy"));
        JSONObject geometry = (JSONObject) feature.get("geometry");
        assertEquals(geometry.getString("@type"), "Polygon");
        assertNotNull(geometry.getString("wkt"));
    }
}
