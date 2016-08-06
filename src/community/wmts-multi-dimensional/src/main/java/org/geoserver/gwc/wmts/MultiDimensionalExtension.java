/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.gwc.layer.CatalogConfiguration;
import org.geoserver.gwc.layer.GeoServerTileLayer;
import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geoserver.gwc.wmts.dimensions.DimensionsUtils;
import org.geoserver.ows.LocalWorkspace;
import org.geoserver.ows.util.KvpMap;
import org.geoserver.ows.util.KvpUtils;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.WMS;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.Range;
import org.geotools.util.logging.Logging;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.conveyor.Conveyor;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.io.XMLBuilder;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.service.OWSException;
import org.geowebcache.service.wmts.WMTSExtensionImpl;
import org.geowebcache.storage.StorageBroker;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WMTS extension that provides the necessary metadata and operations
 * for handling multidimensional requests.
 */
public final class MultiDimensionalExtension extends WMTSExtensionImpl {

    private final static Logger LOGGER = Logging.getLogger(MultiDimensionalExtension.class);

    private final FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

    private TileLayerDispatcher tileLayerDispatcher;

    private final WMS wms;
    private final Catalog catalog;

    public MultiDimensionalExtension(WMS wms, Catalog catalog) {
        this.wms = wms;
        this.catalog = catalog;
        tileLayerDispatcher = GeoServerExtensions.bean(TileLayerDispatcher.class);
    }

    private final List<OperationMetadata> extraOperations = new ArrayList<>();

    {
        extraOperations.add(new OperationMetadata("DescribeDomains"));
        extraOperations.add(new OperationMetadata("GetFeature"));
        extraOperations.add(new OperationMetadata("GetHistogram"));
    }

    @Override
    public List<OperationMetadata> getExtraOperationsMetadata() throws IOException {
        return extraOperations;
    }

    @Override
    public Conveyor getConveyor(HttpServletRequest request, HttpServletResponse response,
                                StorageBroker storageBroker) throws GeoWebCacheException, OWSException {
        // parse the request parameters converting string raw values to java objects
        KvpMap parameters = KvpUtils.normalize(request.getParameterMap());
        KvpUtils.parse(parameters);
        // let's see if we can handle this request
        String operationName = (String) parameters.get("request");
        return Operation.match(operationName, request, response, storageBroker, parameters);
    }

    @Override
    public boolean handleRequest(Conveyor candidateConveyor) throws OWSException {
        if (!(candidateConveyor instanceof SimpleConveyor)) {
            return false;
        }
        SimpleConveyor conveyor = (SimpleConveyor) candidateConveyor;
        switch (conveyor.getOperation()) {
            case DESCRIBE_DOMAINS:
                try {
                    executeDescribeDomainsOperation(conveyor);
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, "Error executing get histogram operation.", exception);
                    throw new OWSException(500, "NoApplicableCode", "",
                            "Error executing histogram operation:" + exception.getMessage());
                }
                break;
            case GET_HISTOGRAM:
                try {
                    executeGetHistogramOperation(conveyor);
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, "Error executing describe domains operation.", exception);
                    throw new OWSException(500, "NoApplicableCode", "",
                            "Error executing describe domains operation:" + exception.getMessage());
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void encodeLayer(XMLBuilder xmlBuilder, TileLayer tileLayer) throws IOException {
        LayerInfo layerInfo = getLayerInfo(tileLayer, tileLayer.getName());
        List<Dimension> dimensions = DimensionsUtils.extractDimensions(wms, layerInfo);
        encodeLayerDimensions(xmlBuilder, dimensions);
    }

    private Domains getDomains(SimpleConveyor conveyor) {
        // getting and parsing the mandatory parameters
        String layerName = (String) conveyor.getParameter("layer", true);
        TileLayer tileLayer = tileLayerDispatcher.getTileLayer(layerName);
        LayerInfo layerInfo = getLayerInfo(tileLayer, layerName);
        String tileMatrixSet = (String) conveyor.getParameter("tileMatrixSet", true);
        GridSubset gridSubset = getGridSubset(tileLayer, tileMatrixSet);
        // getting this layer dimensions along with its values
        List<Dimension> dimensions = DimensionsUtils.extractDimensions(wms, layerInfo);
        // let's see if we have a spatial limitation
        ReferencedEnvelope boundingBox = (ReferencedEnvelope) conveyor.getParameter("bbox", false);
        // add any domain provided restriction and set the bounding box
        Filter filter = Filter.INCLUDE;
        for (Dimension dimension : dimensions) {
            Object restriction = conveyor.getParameter(dimension.getName(), false);
            dimension.setBoundingBox(boundingBox);
            dimension.addDomainRestriction(restriction);
            filter = filterFactory.and(filter, dimension.getFilter());
        }
        // encode the domains
        return new Domains(dimensions, boundingBox, SimplifyingFilterVisitor.simplify(filter));
    }

    private void executeDescribeDomainsOperation(SimpleConveyor conveyor) throws Exception {
        Domains domains = getDomains(conveyor);
        DescribeDomainsTransformer transformer = new DescribeDomainsTransformer(wms);
        transformer.transform(domains, conveyor.getResponse().getOutputStream());
    }

    private void executeGetHistogramOperation(SimpleConveyor conveyor) throws Exception {
        Domains domains = getDomains(conveyor);
        domains.setHistogram((String) conveyor.getParameter("histogram", true));
        domains.setResolution((String) conveyor.getParameter("resolution", true));
        Range
        DescribeDomainsTransformer transformer = new DescribeDomainsTransformer(wms);
        transformer.transform(domains, conveyor.getResponse().getOutputStream());
    }

    private GridSubset getGridSubset(TileLayer tileLayer, String tileMatrixSet) {
        GridSubset gridSubset = tileLayer.getGridSubset(tileMatrixSet);
        if (gridSubset == null) {
            throw new ServiceException(String.format("Unknown tile matrix set '%s'.", tileMatrixSet));
        }
        return gridSubset;
    }

    private LayerInfo getLayerInfo(TileLayer tileLayer, String layerName) {
        // let's see if we can get the layer info from the tile layer
        if (tileLayer != null && tileLayer instanceof GeoServerTileLayer) {
            PublishedInfo publishedInfo = ((GeoServerTileLayer) tileLayer).getPublishedInfo();
            if (!(publishedInfo instanceof LayerInfo)) {
                // dimensions are not supported for layers groups
                throw new ServiceException(String.format("Layer '%s' is a layer group.", layerName));
            }
            return (LayerInfo) publishedInfo;
        }
        // let's see if we are in the context of a virtual service
        WorkspaceInfo localWorkspace = LocalWorkspace.get();
        if (localWorkspace != null) {
            // we need to make sure that the layer name is prefixed with the local workspace
            layerName = CatalogConfiguration.removeWorkspacePrefix(layerName, catalog);
            layerName = localWorkspace.getName() + ":" + layerName;
        }
        LayerInfo layerInfo = catalog.getLayerByName(layerName);
        if (layerInfo == null) {
            // the catalog is not aware of this layer, there is nothing we can do
            throw new ServiceException(String.format("Unknown layer '%s'.", layerName));
        }
        return layerInfo;
    }

    /**
     * Helper method that will encode a layer dimensions, if the layer dimension are NULL or empty nothing will be done.
     */
    private void encodeLayerDimensions(XMLBuilder xml, List<Dimension> dimensions) throws IOException {
        for (Dimension dimension : dimensions) {
            // encode each dimension as top element
            encodeLayerDimension(xml, dimension);
        }
    }

    /**
     * Helper method that will encode a dimension, if the dimension is NULL nothing will be done. All optional attributes
     * that are NULL will be ignored.
     */
    private void encodeLayerDimension(XMLBuilder xml, Dimension dimension) throws IOException {
        xml.indentElement("Dimension");
        // identifier is mandatory
        xml.simpleElement("ows:Identifier", dimension.getName(), true);
        // default value is mandatory
        xml.simpleElement("Default", dimension.getDefaultValueAsString(), true);
        // at least one value is required
        Tuple<Integer, TreeSet<?>> domainsValues = dimension.getDomainValues(Filter.INCLUDE);
        for (String value : dimension.getDomainValuesAsStrings(domainsValues.second)) {
            xml.simpleElement("Value", value, true);
        }
        xml.endElement("Dimension");
    }
}
