package org.geoserver.gwc.wmts;

import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.DimensionDefaultValueSetting.Strategy;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.testreader.CustomFormat;
import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geoserver.gwc.wmts.dimensions.RasterCustomDimension;
import org.geoserver.gwc.wmts.dimensions.RasterElevationDimension;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class RasterCustomTest extends TestsSupport {

    @Test
    public void testGetDefaultValue() {
        testDefaultValueStrategy(Strategy.MINIMUM, "CustomDimValueA");
        testDefaultValueStrategy(Strategy.MAXIMUM, "CustomDimValueC");
    }

    @Test
    public void testGetDomainsValues() throws Exception {
        testDomainsValuesRepresentation(DimensionPresentation.LIST, "CustomDimValueA", "CustomDimValueB", "CustomDimValueC");
        // testDomainsValuesRepresentation(DimensionPresentation.CONTINUOUS_INTERVAL, "CustomDimValueA--CustomDimValueC");
        // testDomainsValuesRepresentation(DimensionPresentation.DISCRETE_INTERVAL, "CustomDimValueA--CustomDimValueC");
    }

    @Override
    protected Dimension buildDimension(DimensionInfo dimensionInfo) {
        CoverageInfo rasterInfo = getCoverageInfo();
        Dimension dimension = new RasterCustomDimension(wms, getLayerInfo(), CustomFormat.CUSTOM_DIMENSION_NAME, dimensionInfo);
        rasterInfo.getMetadata().put(ResourceInfo.CUSTOM_DIMENSION_PREFIX+CustomFormat.CUSTOM_DIMENSION_NAME, dimensionInfo);
        getCatalog().save(rasterInfo);
        return dimension;
    }

    private LayerInfo getLayerInfo() {
        return catalog.getLayerByName(WATER_TEMPERATURE_CUSTOM.getLocalPart());
    }

    private CoverageInfo getCoverageInfo() {
        LayerInfo layerInfo = getLayerInfo();
        assertThat(layerInfo.getResource(), instanceOf(CoverageInfo.class));
        return (CoverageInfo) layerInfo.getResource();
    }
}
