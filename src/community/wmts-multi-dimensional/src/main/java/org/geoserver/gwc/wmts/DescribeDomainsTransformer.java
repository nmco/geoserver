/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts;

import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geoserver.wms.WMS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.opengis.filter.Filter;
import org.xml.sax.ContentHandler;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

class DescribeDomainsTransformer extends TransformerBase {

    public DescribeDomainsTransformer(WMS wms) {
        setIndentation(2);
        setEncoding(wms.getCharSet());
    }

    @Override
    public Translator createTranslator(ContentHandler handler) {
        return new TranslatorSupport(handler);
    }

    class TranslatorSupport extends TransformerBase.TranslatorSupport {

        public TranslatorSupport(ContentHandler handler) {
            super(handler, null, null);
        }

        @Override
        public void encode(Object object) throws IllegalArgumentException {
            if (!(object instanceof Domains)) {
                throw new IllegalArgumentException("Expected domains info but instead got: " + object.getClass().getCanonicalName());
            }
            Domains domains = (Domains) object;
            start("Domains");
            handleBoundingBox(domains.getBoundingBox());
            domains.getDimensions().forEach(dimension -> handleDimension(dimension, domains.getFilter()));
            end("Domains");
        }

        private void handleBoundingBox(ReferencedEnvelope boundingBox) {
        }

        private void handleDimension(Dimension dimension, Filter filter) {
            Tuple<Integer, TreeSet<?>> domainsValues = dimension.getDomainValues(filter);
            List<String> domainsValuesAsStrings = dimension.getDomainValuesAsStrings(domainsValues.second);
            start("DimensionDomain");
            element("ows:Identifier", dimension.getName());
            element("Domain", domainsValuesAsStrings.stream().collect(Collectors.joining(",")));
            element("Size", String.valueOf(domainsValues.second.size()));
            end("DimensionDomain");
        }
    }
}
