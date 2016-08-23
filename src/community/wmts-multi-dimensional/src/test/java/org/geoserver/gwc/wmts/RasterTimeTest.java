package org.geoserver.gwc.wmts;

import org.geoserver.catalog.DimensionDefaultValueSetting.Strategy;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geoserver.gwc.wmts.dimensions.RasterTimeDimension;
import org.geoserver.util.ISO8601Formatter;
import org.geotools.feature.type.DateUtil;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>
 * This class contains tests that check that time dimension values are correctly extracted from rasters. Some domain
 * values are dynamically created based on the current time, this matches what is done in WMS dimensions tests.
 * </p>
 * <p>
 * Note, there is some inconsistency between ISO formatter and GeoTools data serializer string output, this
 * is something that needs to be fixed (or maybe not) at GeoServer and GeoTools level. In the mean time
 * this tests will use the two formatter were needed.
 * </p>
 */
public class RasterTimeTest extends TestsSupport {

    // sorted time domain values as date objects
    protected static Date[] DATE_VALUES = new Date[]{
            DateUtil.deserializeDateTime("2008-10-31T00:00:00Z"),
            DateUtil.deserializeDateTime("2008-11-01T00:00:00Z"),
            getGeneratedMinValue(),
            getGeneratedMiddleValue(),
            getGeneratedMaxValue()
    };

    // sorted time domain values as strings formatted with ISO8601 formatter
    protected static String[] STRING_VALUES = new String[]{
            formatDate(DATE_VALUES[0]),
            formatDate(DATE_VALUES[1]),
            formatDate(DATE_VALUES[2]),
            formatDate(DATE_VALUES[3]),
            formatDate(DATE_VALUES[4])
    };

    @Test
    public void testGetDefaultValue() {
        testDefaultValueStrategy(Strategy.MINIMUM, DateUtil.serializeDateTime(DATE_VALUES[0].getTime(), true));
        testDefaultValueStrategy(Strategy.MAXIMUM, DateUtil.serializeDateTime(DATE_VALUES[4].getTime(), true));
    }

    @Test
    public void testGetDomainsValues() throws Exception {
        testDomainsValuesRepresentation(DimensionPresentation.LIST, STRING_VALUES);
        testDomainsValuesRepresentation(DimensionPresentation.CONTINUOUS_INTERVAL, STRING_VALUES[0] + "--" + STRING_VALUES[4]);
        testDomainsValuesRepresentation(DimensionPresentation.DISCRETE_INTERVAL, STRING_VALUES[0] + "--" + STRING_VALUES[4]);
    }

    @Override
    protected Dimension buildDimension(DimensionInfo dimensionInfo) {
        return new RasterTimeDimension(wms, getLayerInfo(), dimensionInfo);
    }

    /**
     * Helper method that simply formats a date using the ISO8601 formatter.
     */
    private static String formatDate(Date date) {
        ISO8601Formatter formatter = new ISO8601Formatter();
        return formatter.format(date);
    }

    /**
     * Generates the current minimum date.
     */
    private static Date getGeneratedMinValue() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    /**
     * Generates the current middle date, this date is one month later than the current minimum date.
     */
    private static Date getGeneratedMiddleValue() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        return calendar.getTime();
    }

    /**
     * Generates the current maximum date, this date is one year later than the current minimum date.
     */
    private static Date getGeneratedMaxValue() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        return calendar.getTime();
    }

    /**
     * Helper method that just returns the current layer info.
     */
    private LayerInfo getLayerInfo() {
        return catalog.getLayerByName(RASTER_TIME.getLocalPart());
    }
}
