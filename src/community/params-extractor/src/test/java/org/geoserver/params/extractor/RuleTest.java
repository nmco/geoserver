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
    public void testPositionRule() throws Exception {
        Rule ruleA = new RuleBuilder().withId("0")
                .withPosition(3)
                .withParameter("CQL_FILTER")
                .withTransform("CFCC='$2'")
                .build();
        Rule ruleB = new RuleBuilder().withId("1")
                .withPosition(4)
                .withParameter("CQL_FILTER")
                .withTransform("CFCC='$2'")
                .withCombine("$1 AND $2")
                .build();
        UrlTransform urlTransform = new UrlTransform("/geoserver/tiger/wms/H11/D68", Optional.of("REQUEST=GetMap"));
        ruleA.apply(urlTransform);
        assertThat(urlTransform.toString(),
                is("/geoserver/tiger/wms/D68?REQUEST=GetMap&CQL_FILTER=CFCC%3D%27H11%27"));
        ruleB.apply(urlTransform);
        assertThat(urlTransform.toString(),
                is("/geoserver/tiger/wms?REQUEST=GetMap&CQL_FILTER=CFCC%3D%27H11%27+AND+CFCC%3D%27D68%27"));
    }

    @Test
    public void testMatchRule() throws Exception {
        Rule rule = new RuleBuilder().withId("0")
                .withMatch("^.*?(/([^/]+)/([^/]+))$")
                .withParameter("CQL_FILTER")
                .withTransform("CFCC='$2' AND CFCC='$3'")
                .build();
        UrlTransform urlTransform = new UrlTransform("/geoserver/tiger/wms/H11/D68", Optional.of("REQUEST=GetMap"));
        rule.apply(urlTransform);
        assertThat(urlTransform.toString(),
                is("/geoserver/tiger/wms?REQUEST=GetMap&CQL_FILTER=CFCC%3D%27H11%27+AND+CFCC%3D%27D68%27"));
    }
}
