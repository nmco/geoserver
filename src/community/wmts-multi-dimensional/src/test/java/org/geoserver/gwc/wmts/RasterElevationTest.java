package org.geoserver.gwc.wmts;

import org.geoserver.catalog.DimensionDefaultValueSetting.Strategy;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geoserver.gwc.wmts.dimensions.RasterElevationDimension;
import org.junit.Test;

public class RasterElevationTest extends TestsSupport {

    @Test
    public void testGetDefaultValue() {
        testDefaultValueStrategy(Strategy.MINIMUM, "0.0");
        testDefaultValueStrategy(Strategy.MAXIMUM, "100.0");
    }

    @Test
    public void testGetDomainsValues() throws Exception {
        testDomainsValuesRepresentation(DimensionPresentation.LIST, "0.0", "100.0");
        testDomainsValuesRepresentation(DimensionPresentation.CONTINUOUS_INTERVAL, "0.0--100.0");
        testDomainsValuesRepresentation(DimensionPresentation.DISCRETE_INTERVAL, "0.0--100.0");
    }

    @Override
    protected Dimension buildDimension(DimensionInfo dimensionInfo) {
        return new RasterElevationDimension(wms, getLayerInfo(), dimensionInfo);
    }

    private LayerInfo getLayerInfo() {
        return catalog.getLayerByName(RASTER_ELEVATION.getLocalPart());
    }
}
