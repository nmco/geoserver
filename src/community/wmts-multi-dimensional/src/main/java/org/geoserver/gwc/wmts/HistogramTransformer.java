/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts;

import org.geoserver.wms.WMS;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.xml.sax.ContentHandler;

import java.util.List;
import java.util.stream.Collectors;

class HistogramTransformer extends TransformerBase {

    public HistogramTransformer(WMS wms) {
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
            Tuple<List<String>, List<String>> histogram = domains.getHistogramValues();
            start("Histogram");
            element("ows:Identifier", domains.getHistogramName());
            element("Domain", histogram.first.stream().collect(Collectors.joining(",")));
            element("Values", histogram.second.stream().collect(Collectors.joining(",")));
            end("Histogram");
        }
    }
}
