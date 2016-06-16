package org.geoserver.inspire.wmts;

import org.geoserver.config.GeoServer;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamServiceLoader;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geotools.util.Version;

import java.util.ArrayList;

public class WMTSXStreamLoader extends XStreamServiceLoader<WMTSInfo> {

    public WMTSXStreamLoader(GeoServerResourceLoader resourceLoader) {
        super(resourceLoader, "wmts");

    }

    public Class<WMTSInfo> getServiceClass() {
        return WMTSInfo.class;
    }

    protected WMTSInfo createServiceFromScratch(GeoServer gs) {

        WMTSInfoImpl wmts = new WMTSInfoImpl();
        wmts.setName("WMTS");

        return wmts;
    }

    @Override
    public void initXStreamPersister(XStreamPersister xp, GeoServer gs) {
        super.initXStreamPersister(xp, gs);
        xp.getXStream().alias("wmts", WMTSInfo.class, WMTSInfoImpl.class);
    }

    @Override
    protected WMTSInfo initialize(WMTSInfo service) {
		super.initialize(service);
        return service;
    }
}
