package org.geoserver.gwc.wmts;

import org.geoserver.config.GeoServer;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamServiceLoader;
import org.geoserver.platform.GeoServerResourceLoader;

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
        service.setEnabled(true);
        service.setMaintainer("http://geoserver.org/comm");
        service.setOnlineResource("http://geoserver.org");
        service.setTitle("GeoServer Web Map Tile Service");
        service.setAbstract("A compliant implementation of WMTS service.");
        service.setFees("NONE");
        service.setAccessConstraints("NONE");
        return service;
    }
}
