/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts;

import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;

import java.util.List;

public class Domains {

    private final List<Dimension> dimensions;
    private final ReferencedEnvelope boundingBox;
    private final Filter filter;

    private String histogram;
    private String resolution;

    public Domains(List<Dimension> dimensions, ReferencedEnvelope boundingBox, Filter filter) {
        this.dimensions = dimensions;
        this.boundingBox = boundingBox;
        this.filter = filter;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public ReferencedEnvelope getBoundingBox() {
        return boundingBox;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setHistogram(String histogram) {
        this.histogram = histogram;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getHistogramName() {
        return histogram;
    }

    public Tuple<List<String>, List<String>> getHistogramValues() {
        for (Dimension dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase(histogram)) {
                return dimension.getHistogram(filter, resolution);
            }
        }
        throw new RuntimeException(String.format("Dimension '%s' could not be found.", histogram));
    }
}
