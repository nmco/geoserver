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
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ParserTest {

    @Test
    public void testParsingEmptyRules() throws Exception {
        doWork("rules1.xml", (InputStream inputStream) -> {
            List<Rule> rules = Parser.parse(inputStream);
            assertThat(rules.size(), is(0));
        });
    }

    @Test
    public void testParsingOneRules() throws Exception {
        doWork("rules2.xml", (InputStream inputStream) -> {
            List<Rule> rules = Parser.parse(inputStream);
            assertThat(rules.size(), is(1));
        });
    }

    @Test
    public void testParsingMultipleRules() throws Exception {
        doWork("rules3.xml", (InputStream inputStream) -> {
            List<Rule> rules = Parser.parse(inputStream);
            assertThat(rules.size(), is(1));
        });
    }

    private static void doWork(String resourcePath, Consumer<InputStream> consumer) throws Exception {
        URL resource = ParserTest.class.getClassLoader().getResource(resourcePath);
        assertThat(resource, notNullValue());
        File file = new File(resource.getFile());
        assertThat(file.exists(), is(true));
        try (InputStream inputStream = new FileInputStream(file)) {
            consumer.accept(inputStream);
        }
    }
}
