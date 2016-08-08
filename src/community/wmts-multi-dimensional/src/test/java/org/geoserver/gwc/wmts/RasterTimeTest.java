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

public class RasterTimeTest extends TestsSupport {

    protected static Date[] DATE_VALUES = new Date[]{
            DateUtil.deserializeDateTime("2008-10-31T00:00:00Z"),
            DateUtil.deserializeDateTime("2008-11-01T00:00:00Z"),
            getMinValue(),
            getMiddleValue(),
            getMaxValue()
    };

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

    private static String formatDate(Date date) {
        ISO8601Formatter formatter = new ISO8601Formatter();
        return formatter.format(date);
    }

    private static Date getMinValue() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    private static Date getMiddleValue() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        return calendar.getTime();
    }

    private static Date getMaxValue() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        return calendar.getTime();
    }

    private LayerInfo getLayerInfo() {
        return catalog.getLayerByName(RASTER_TIME.getLocalPart());
    }
}
