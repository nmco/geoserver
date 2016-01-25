/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class RuleTest {

    @Test
    public void testParsingEmptyRules() throws Exception {
        Rule rule = new RuleBuilder(0).withMatch("^.*?(/([^/]+?))/[^/]+$")
                .withParameter("cql_filter")
                .withTransform("seq='$2'")
                .build();
        UrlTransform urlTransform = new UrlTransform("geoserver/workspace/layer/K_140M/ows");
        rule.apply(urlTransform);
        System.out.println(urlTransform.toString());
    }

    @Test
    public void testParsingEmptyRules2() throws Exception {
        Rule rule = new RuleBuilder(0).withPosition(2)
                .withParameter("cql_filter")
                .withTransform("seq='$2'")
                .build();
        UrlTransform urlTransform = new UrlTransform("geoserver/workspace/layer/K_140M/ows");
        rule.apply(urlTransform);
        System.out.println(urlTransform.toString());
    }
}
