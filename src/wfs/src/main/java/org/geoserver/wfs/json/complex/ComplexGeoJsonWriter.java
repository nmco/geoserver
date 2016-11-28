package org.geoserver.wfs.json.complex;

import org.geotools.feature.FeatureCollection;

import java.io.Writer;
import java.util.List;

public class ComplexGeoJsonWriter {

    private final List<FeatureCollection> collections;
    private final Writer writer;

    public ComplexGeoJsonWriter(List<FeatureCollection> collections, Writer writer) {
        this.collections = collections;
        this.writer = writer;
    }


}