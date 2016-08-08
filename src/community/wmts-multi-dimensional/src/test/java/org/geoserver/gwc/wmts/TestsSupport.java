package org.geoserver.gwc.wmts;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DimensionDefaultValueSetting;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.impl.DimensionInfoImpl;
import org.geoserver.data.test.MockData;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSTestSupport;
import org.geoserver.wms.dimension.RasterTimeDimensionDefaultValueTest;
import org.geoserver.wms.dimension.VectorElevationDimensionDefaultValueTest;
import org.junit.Before;
import org.opengis.filter.Filter;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public abstract class TestsSupport extends WMSTestSupport {

    protected static final QName RASTER_ELEVATION = new QName(MockData.SF_URI, "watertemp", MockData.SF_PREFIX);
    protected static final QName RASTER_TIME = new QName(MockData.SF_URI, "watertemp_future_generated", MockData.SF_PREFIX);
    protected static final QName RASTER_CUSTOM = new QName(MockData.SF_URI, "watertemp_custom", MockData.SF_PREFIX);

    protected static final QName VECTOR_ELEVATION = new QName(MockData.SF_URI, "TimeWithStartEnd", MockData.SF_PREFIX);
    protected static final QName VECTOR_TIME = new QName(MockData.SF_URI, "vector_time", MockData.SF_PREFIX);
    protected static final QName VECTOR_CUSTOM = new QName(MockData.SF_URI, "vector_custom", MockData.SF_PREFIX);


    protected WMS wms;
    protected Catalog catalog;

    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // raster with elevation dimension
        testData.addRasterLayer(RASTER_ELEVATION, "/org/geoserver/wms/dimension/watertemp.zip",
                null, Collections.emptyMap(), getClass(), getCatalog());
        // raster with time dimension
        RasterTimeDimensionDefaultValueTest.prepareFutureCoverageData(RASTER_TIME,
                this.getDataDirectory(), this.getCatalog());
        // raster with custom dimension
        testData.addRasterLayer(RASTER_CUSTOM, "/org/geoserver/wms/dimension/custwatertemp.zip",
                null, Collections.emptyMap(), getClass(), getCatalog());
        // vector with elevation dimension
        testData.addVectorLayer(VECTOR_ELEVATION, Collections.emptyMap(),
                "TimeElevationWithStartEnd.properties", VectorElevationDimensionDefaultValueTest.class, getCatalog());
    }

    @Before
    public void setup() throws Exception {
        wms = getWMS();
        catalog = getCatalog();
    }

    protected abstract Dimension buildDimension(DimensionInfo dimensionInfo);

    protected void testDomainsValuesRepresentation(DimensionPresentation dimensionPresentation, String ... expectedDomainValues) throws IOException {
        DimensionInfo dimensionInfo = createDimension(true, dimensionPresentation, null);
        Dimension dimension = buildDimension(dimensionInfo);
        Tuple<Integer, TreeSet<?>> domainValues = dimension.getDomainValues(Filter.INCLUDE);
        List<String> valuesAsStrings = dimension.getDomainValuesAsStrings(domainValues.second);
        assertThat(valuesAsStrings.size(), is(expectedDomainValues.length));
        assertThat(valuesAsStrings, containsInAnyOrder(expectedDomainValues));
    }

    protected void testDefaultValueStrategy(DimensionDefaultValueSetting.Strategy strategy, String expectedDefaultValue) {
        DimensionDefaultValueSetting defaultValueStrategy = new DimensionDefaultValueSetting();
        defaultValueStrategy.setStrategyType(strategy);
        testDefaultValueStrategy(defaultValueStrategy, expectedDefaultValue);
    }

    protected void testDefaultValueStrategy(DimensionDefaultValueSetting defaultValueStrategy, String expectedDefaultValue) {
        DimensionInfo dimensionInfo = createDimension(true, null, defaultValueStrategy);
        Dimension dimension = buildDimension(dimensionInfo);
        String defaultValue = dimension.getDefaultValueAsString();
        assertThat(defaultValue, is(expectedDefaultValue));
    }

    protected static DimensionInfo createDimension(boolean enable, DimensionPresentation presentation,
                                                DimensionDefaultValueSetting defaultValueStrategy) {
        DimensionInfo dimension = new DimensionInfoImpl();
        dimension.setEnabled(enable);
        dimension.setPresentation(presentation);
        dimension.setDefaultValue(defaultValueStrategy);
        return dimension;
    }
}
