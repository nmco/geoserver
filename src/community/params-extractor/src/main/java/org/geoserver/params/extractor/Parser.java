/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import org.geotools.util.logging.Logging;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

final class Parser {

    private static final Logger LOGGER = Logging.getLogger(Parser.class);

    static List<Rule> parse(InputStream inputStream) {
        RuleHandler handler = new RuleHandler();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(inputStream, handler);
        } catch (Exception exception) {
            throw Utils.exception(exception, "Error parsing rules files.");
        }
        return handler.rules;
    }

    private static final class RuleHandler extends DefaultHandler {

        final List<Rule> rules = new ArrayList<>();

        private int parsed = 0;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!qName.equalsIgnoreCase("rule")) {
                return;
            }
            Utils.debug(LOGGER, "Start parsing rule %d.", parsed);
            RuleBuilder ruleBuilder = new RuleBuilder(parsed);
            getAttribute("position", attributes, compose(Integer::valueOf, ruleBuilder::withPosition));
            getAttribute("match", attributes, ruleBuilder::withMatch);
            getAttribute("parameter", attributes, ruleBuilder::withParameter);
            getAttribute("remove", attributes, compose(Integer::valueOf, ruleBuilder::withRemove));
            getAttribute("transform", attributes, ruleBuilder::withTransform);
            getAttribute("combine", attributes, ruleBuilder::withCombine);
            rules.add(ruleBuilder.build());
            Utils.debug(LOGGER, "End parsing rule %d.", parsed);
            parsed++;
        }

        private static <T> Consumer<String> compose(Function<String, T> convert, Consumer<T> setter) {
            return (value) -> setter.accept(convert.apply(value));
        }

        private void getAttribute(String attributeName, Attributes attributes, Consumer<String> setter) {
            String attributeValue = attributes.getValue(attributeName);
            if (attributeValue == null) {
                Utils.debug(LOGGER, "Rule %d attribute %s is NULL.", parsed, attributeName);
                return;
            }
            Utils.debug(LOGGER, "Rule %d attribute %s is %s.", parsed, attributeName, attributeValue);
            try {
                setter.accept(attributeValue);
            } catch (Exception exception) {
                throw Utils.exception(exception,
                        "Error setting attribute '%s' with value '%s' to rule %d.",
                        attributeName, attributeValue, parsed);
            }
        }
    }
}