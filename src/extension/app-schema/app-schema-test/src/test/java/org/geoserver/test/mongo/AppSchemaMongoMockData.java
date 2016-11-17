package org.geoserver.test.mongo;

import org.geoserver.test.AbstractAppSchemaMockData;

public class AppSchemaMongoMockData extends AbstractAppSchemaMockData {

    protected static final String STATIONS_PREFIX = "st";

    protected static final String STATIONS_URI = "http://www.stations.org/1.0";

    @Override
    public void addContent() {
        putNamespace(STATIONS_PREFIX, STATIONS_URI);
        addFeatureType(STATIONS_PREFIX, "StationFeature", "stations.xml", "stations.xsd");
    }
}
