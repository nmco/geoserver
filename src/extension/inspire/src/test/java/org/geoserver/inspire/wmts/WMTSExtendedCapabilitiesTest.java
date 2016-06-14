/* (c) 2014 - 2015 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.inspire.wmts;

import org.geoserver.inspire.ViewServicesTestSupport;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class WMTSExtendedCapabilitiesTest extends ViewServicesTestSupport {

    private static final String WMTS_1_0_0_GETCAPREQUEST = "gwc/service/wmts?REQUEST=GetCapabilities";

    @Override
    protected String getGetCapabilitiesRequestPath() {
        return WMTS_1_0_0_GETCAPREQUEST;
    }

    @Override
    protected String getMetadataUrl() {
        return "http://foo.com?bar=baz";
    }

    @Override
    protected String getMetadataType() {
        return "application/vnd.iso.19139+xml";
    }

    @Override
    protected String getLanguage() {
        return "fre";
    }

    @Override
    protected String getAlternateMetadataType() {
        return "application/vnd.ogc.csw.GetRecordByIdResponse_xml";
    }

    @Test
    public void test() throws Exception {
        MockHttpServletResponse sr = getAsServletResponse("gwc/service/wmts?REQUEST=GetCapabilities");
        System.out.println("qaa");
        final Document dom = getAsDOM(getGetCapabilitiesRequestPath());
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(dom), new StreamResult(writer));
        System.out.println(writer.getBuffer().toString());
    }
}
