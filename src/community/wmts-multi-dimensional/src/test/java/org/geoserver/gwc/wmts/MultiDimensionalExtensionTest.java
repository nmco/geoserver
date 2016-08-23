package org.geoserver.gwc.wmts;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.DimensionDefaultValueSetting;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.impl.DimensionInfoImpl;
import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MultiDimensionalExtensionTest extends TestsSupport {

    private static XpathEngine xpath;

    {
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("xlink", "http://www.w3.org/1999/xlink");
        namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        namespaces.put("ows", "http://www.opengis.net/ows/1.1");
        namespaces.put("wmts", "http://www.opengis.net/wmts/1.0");
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(namespaces));
        xpath = XMLUnit.newXpathEngine();
    }

    private DimensionDefaultValueSetting minimumValue() {
        // create a default value strategy, minimum value in this case
        DimensionDefaultValueSetting defaultValueSetting = new DimensionDefaultValueSetting();
        defaultValueSetting.setStrategyType(DimensionDefaultValueSetting.Strategy.MINIMUM);
        return defaultValueSetting;
    }

    private void registerLayerDimension(ResourceInfo info, String dimensionName,
                                        DimensionPresentation presentation, DimensionDefaultValueSetting defaultValue) {
        DimensionInfo dimension = new DimensionInfoImpl();
        dimension.setEnabled(true);
        dimension.setPresentation(presentation);
        dimension.setDefaultValue(defaultValue);
        info.getMetadata().put(dimensionName, dimension);
        getCatalog().save(info);
    }

    @Test
    public void testGetCapabilitiesResultCoverage() throws Exception {
        // registering elevation and time dimensions for a raster coverage
        CoverageInfo rasterInfo = getCatalog().getCoverageByName(RASTER_ELEVATION.getLocalPart());
        registerLayerDimension(rasterInfo, ResourceInfo.ELEVATION, DimensionPresentation.LIST, minimumValue());
        registerLayerDimension(rasterInfo, ResourceInfo.TIME, DimensionPresentation.CONTINUOUS_INTERVAL, minimumValue());
        // registering elevation and time dimensions for a vector coverage
        FeatureTypeInfo vectorInfo = getCatalog().getFeatureTypeByName(VECTOR_ELEVATION.getLocalPart());
        registerLayerDimension(vectorInfo, ResourceInfo.ELEVATION, DimensionPresentation.CONTINUOUS_INTERVAL, minimumValue());
        registerLayerDimension(vectorInfo, ResourceInfo.TIME, DimensionPresentation.LIST, minimumValue());
        // perform the get capabilities request
        MockHttpServletResponse response = getAsServletResponse("gwc/service/wmts?request=GetCapabilities");
        Document result = getResultAsDocument(response);
        // check raster layer dimensions
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension", "2");
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension[ows:Identifier='elevation']", "1");
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension[ows:Identifier='time']", "1");
        // check raster elevation
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension[wmts:Default='0.0']", "1");
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension[wmts:Value='0.0']", "1");
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension[wmts:Value='100.0']", "1");
        // check raster time
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension[wmts:Default='0.0']", "1");
        checkXpathCount(result, "/wmts:Contents/wmts:Layer/wmts:Dimension[wmts:Value='2008-10-31T00:00:00.000Z--2008-11-01T00:00:00.000Z']", "1");
    }

    private void checkXpathCount(Document result, String path, String count) throws Exception {
        String finalPath = String.format("count(/%s)", path);
        assertThat(xpath.evaluate(finalPath, result), is(count));
    }

    private Document getResultAsDocument(MockHttpServletResponse response) throws Exception {
        String result = response.getContentAsString();
        assertEquals(200, response.getStatus());
        return XMLUnit.buildTestDocument(result);
    }

    @Override
    protected Dimension buildDimension(DimensionInfo dimensionInfo) {
        return null;
    }
}
