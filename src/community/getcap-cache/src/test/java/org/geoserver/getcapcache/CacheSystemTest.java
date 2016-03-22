/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.catalog.Catalog;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.data.test.CiteTestData;
import org.geoserver.wms.WMSTestSupport;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public final class CacheSystemTest extends WMSTestSupport {

    @Override
    protected void registerNamespaces(Map<String, String> namespaces) {
        namespaces.put("wms", "http://www.opengis.net/wms");
        namespaces.put("ows", "http://www.opengis.net/ows");
    }

    /**
     * For now we test all the behavior in a single test to avoid having to deal with configurations changes
     * and asynchronous behaviors when running multiples tests in a unpredictable order.
     */
    @Test
    public void testGetCapabilitiesCacheWithDefaultConfiguration() throws Exception {

        // we get the cache manager form the spring context
        CacheManager cacheManager = (CacheManager) applicationContext.getBean("getCapCacheManager");
        assertThat(cacheManager, notNullValue());
        // we init the XML path engine
        XpathEngine xpath = XMLUnit.newXpathEngine();

        // first request test, the value will be cached
        RequestInfo requestA = new RequestInfoBuilder().withRequest("WMS").withVersion("1.1.1").build();
        Document domA = dom(get("wms?service=wms&request=getCapabilities&version=1.1.1"), true);
        assertThat(domA, notNullValue());
        int layersA = countLayers(xpath, domA, "WMT_MS_Capabilities", "");
        assertThat(layersA, greaterThan(0));
        assertThat(cacheManager.cache.size(), is(1));
        CacheResult cacheResultA = cacheManager.get(requestA);
        assertThat(cacheResultA, notNullValue());
        assertThat(countLayers(xpath, cacheResultA, "WMT_MS_Capabilities", ""), is(layersA));
        assertThat(cacheResultA.getUpdateSequence(), is(getUpdateSequence(xpath, domA, "WMT_MS_Capabilities", "")));
        assertThat(getVersion(xpath, cacheResultA, "WMT_MS_Capabilities", ""), is("1.1.1"));

        // second request test with different version, the value will be cached
        RequestInfo requestB = new RequestInfoBuilder().withRequest("WMS").withVersion("1.3.0").build();
        Document domB = dom(get("wms?service=wms&request=getCapabilities&version=1.3.0"), true);
        assertThat(domB, notNullValue());
        int layersB = countLayers(xpath, domB, "WMS_Capabilities", "wms:");
        assertThat(layersB, greaterThan(0));
        assertThat(cacheManager.cache.size(), is(2));
        CacheResult cacheResultB = cacheManager.get(requestB);
        assertThat(cacheResultB, notNullValue());
        assertThat(countLayers(xpath, cacheResultB, "WMS_Capabilities", "wms:"), is(layersB));
        assertThat(cacheResultB.getUpdateSequence(), is(getUpdateSequence(xpath, domB, "WMS_Capabilities", "wms:")));
        assertThat(getVersion(xpath, cacheResultB, "WMS_Capabilities", "wms:"), is("1.3.0"));

        // third request test with wrong version, the value will be cached
        RequestInfo requestC = new RequestInfoBuilder().withRequest("WMS").withVersion("5.0.0").build();
        Document domC = dom(get("wms?service=wms&request=getCapabilities&version=5.0.0"), true);
        assertThat(domC, notNullValue());
        int layersC = countLayers(xpath, domC, "WMS_Capabilities", "wms:");
        assertThat(layersC, greaterThan(0));
        assertThat(cacheManager.cache.size(), is(3));
        CacheResult cacheResultC = cacheManager.get(requestC);
        assertThat(cacheResultC, notNullValue());
        assertThat(countLayers(xpath, cacheResultC, "WMS_Capabilities", "wms:"), is(layersC));
        assertThat(cacheResultC.getUpdateSequence(), is(getUpdateSequence(xpath, domC, "WMS_Capabilities", "wms:")));
        assertThat(getVersion(xpath, cacheResultC, "WMS_Capabilities", "wms:"), is("1.3.0"));
        // check that the cached result is returned
        CacheResult changedCacheResult = new CacheResult(requestB, "text", TestUtils.stringToByteArray("CACHE VALUE updateSequence=\"2\""));
        assertThat(changedCacheResult.getUpdateSequence(), is(2));
        cacheManager.put(requestB, changedCacheResult);
        InputStream inputStreamA = get("wms?service=wms&request=getCapabilities&version=1.3.0");
        assertThat(TestUtils.inputStreamToString(inputStreamA), is("CACHE VALUE updateSequence=\"2\""));

        // check request with lower update sequence using request B who's update sequence value is 2
        int updateSequence = 2;
        InputStream inputStreamB = get(
                "wms?service=wms&request=getCapabilities&version=1.3.0&updateSequence=" + (updateSequence - 1));
        assertThat(TestUtils.inputStreamToString(inputStreamB), is("CACHE VALUE updateSequence=\"2\""));

        // check request with same update sequence using request B
        InputStream inputStreamC = get(
                "wms?service=wms&request=getCapabilities&version=1.3.0&updateSequence=" + updateSequence);
        assertThat(TestUtils.inputStreamToString(inputStreamC).contains("WMS capabilities document is current"), is(true));

        // check request with higher update sequence using request B
        InputStream inputStreamD = get(
                "wms?service=wms&request=getCapabilities&version=1.3.0&updateSequence=" + updateSequence + 1);
        assertThat(TestUtils.inputStreamToString(inputStreamD).contains("updateSequence that is greater than"), is(true));

        // check cache truncate on catalog events
        Catalog catalog = (Catalog) applicationContext.getBean("catalog");
        catalog.remove(catalog.getLayerByName(CiteTestData.BRIDGES.getLocalPart()));
        // let's wait to see if the cached is truncated until a max of 10 seconds
        for (int i = 0; i < 20; i++) {
            if (cacheManager.cache.size() == 0) {
                // the cache was truncated
                break;
            }
            // not ready yet let's wait 500 milliseconds
            Thread.sleep(500);
        }
        assertThat(cacheManager.cache.size(), is(0));

        // test configuration changes, we are disabling the cache
        DispatcherCallback dispatcherCallback = (DispatcherCallback) applicationContext.getBean("getCapCacheDispatcherCallback");
        GeoServerDataDirectory dataDirectory = (GeoServerDataDirectory) applicationContext.getBean("dataDirectory");
        File configurationFile = Utils.getOrCreate(dataDirectory.findOrCreateDir(ConfigurationParser.CONFIGURATION_DIRECTORY),
                ConfigurationParser.CONFIGURATION_FILE_NAME, null);
        FileUtils.deleteQuietly(configurationFile);
        FileUtils.copyFile(TestUtils.findResource("configuration2.xml"), configurationFile);
        for (int i = 0; i < 20; i++) {
            if (!dispatcherCallback.getConfiguration().isCachingEnable()) {
                break;
            }
            Thread.sleep(500);
        }
        assertThat(dispatcherCallback.getConfiguration().isCachingEnable(), is(false));
        get("wms?service=wms&request=getCapabilities&version=1.1.1");
        assertThat(cacheManager.cache.size(), is(0));

        // enabling the cache again with white listed and black listed IPs
        FileUtils.deleteQuietly(configurationFile);
        FileUtils.copyFile(TestUtils.findResource("configuration5.xml"), configurationFile);
        for (int i = 0; i < 20; i++) {
            if (dispatcherCallback.getConfiguration().isCachingEnable()) {
                break;
            }
            Thread.sleep(500);
        }
        assertThat(dispatcherCallback.getConfiguration().isCachingEnable(), is(true));
        assertThat(dispatcherCallback.getConfiguration().getBlackListedIps().size(), is(2));
        assertThat(dispatcherCallback.getConfiguration().getWhitedListedIps().size(), is(3));
        get("wms?service=wms&request=getCapabilities&version=1.1.1");
        assertThat(cacheManager.cache.size(), is(1));
        cacheManager.truncate();

        // test black listed and white listed IPs invocation, not allowed IP
        Document domE = dom(executeRequest("wms?service=wms&request=getCapabilities&version=1.1.1", "192.180.10.20"), true);
        assertThat(domE, notNullValue());
        int layersE = countLayers(xpath, domE, "WMT_MS_Capabilities", "");
        assertThat(layersE, greaterThan(0));
        assertThat(cacheManager.cache.size(), is(0));

        // test black listed and white listed IPs invocation, not allowed IP
        Document domF = dom(executeRequest("wms?service=wms&request=getCapabilities&version=1.1.1", "192.160.15.10"), true);
        assertThat(domF, notNullValue());
        int layersF = countLayers(xpath, domF, "WMT_MS_Capabilities", "");
        assertThat(layersF, greaterThan(0));
        assertThat(cacheManager.cache.size(), is(1));
    }

    private int countLayers(XpathEngine xpath, CacheResult result, String root, String prefix) throws Exception {
        return countLayers(xpath, dom(new ByteArrayInputStream(result.getOutput().toByteArray()), true), root, prefix);
    }

    private int countLayers(XpathEngine xpath, Document document, String root, String prefix) throws Exception {
        return xpath.getMatchingNodes("/" + prefix + root + "/" + prefix +
                "Capability/" + prefix + "Layer/" + prefix + "Layer", document).getLength();
    }

    private int getUpdateSequence(XpathEngine xpath, Document document, String root, String prefix) throws Exception {
        return Integer.parseInt(xpath.getMatchingNodes("/" + prefix + root, document).item(0).getAttributes()
                .getNamedItem("updateSequence").getNodeValue());
    }

    private String getVersion(XpathEngine xpath, CacheResult result, String root, String prefix) throws Exception {
        return xpath.getMatchingNodes("/" + prefix + root,
                dom(new ByteArrayInputStream(result.getOutput().toByteArray()), true))
                .item(0).getAttributes().getNamedItem("version").getNodeValue();
    }

    private InputStream executeRequest(String path, String remoteAddress) throws Exception {
        MockHttpServletRequest request = createRequest(path);
        request.setRemoteAddr(remoteAddress);
        request.setMethod("GET");
        request.setContent(new byte[]{});
        MockHttpServletResponse response = dispatch(request, null);
        return new ByteArrayInputStream(response.getContentAsByteArray());
    }
}
