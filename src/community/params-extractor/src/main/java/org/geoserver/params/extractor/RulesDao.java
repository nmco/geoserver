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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public final class RulesDao {

    private static final Logger LOGGER = Logging.getLogger(RulesDao.class);
    private static final String NEW_LINE = System.getProperty("line.separator");

    public static List<Rule> getRules(InputStream inputStream) {
        try {
            if(inputStream.available() == 0) {
                Utils.debug(LOGGER, "Rules files seems to be empty.");
                return new ArrayList<>();
            }
            RuleHandler handler = new RuleHandler();
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(inputStream, handler);
            return handler.rules;
        } catch (Exception exception) {
            throw Utils.exception(exception, "Error parsing rules files.");
        } finally {
            Utils.closeQuietly(inputStream);
        }
    }

    public static void saveOrUpdateRule(Rule rule, InputStream inputStream, OutputStream outputStream) {
        try {
            List<Rule> rules = getRules(inputStream);
            boolean exists = false;
            for (int i = 0; i < rules.size() && !exists; i++) {
                if (rules.get(i).getId().equals(rule.getId())) {
                    rules.set(i, rule);
                    exists = true;
                }
            }
            if (!exists) {
                rules.add(rule);
            }
            writeRules(rules, outputStream);
        } finally {
            Utils.closeQuietly(inputStream);
            Utils.closeQuietly(outputStream);
        }
    }

    private static void writeRules(List<Rule> rules, OutputStream outputStream) {
        try {
            XMLStreamWriter output = XMLOutputFactory.newInstance().
                    createXMLStreamWriter(new OutputStreamWriter(outputStream, "utf-8"));
            output.writeStartDocument();
            output.writeCharacters(NEW_LINE);
            output.writeStartElement("Rules");
            output.writeCharacters(NEW_LINE);
            rules.forEach(rule -> writeRule(rule, output));
            output.writeEndElement();
            output.writeCharacters(NEW_LINE);
            output.writeEndDocument();
            output.close();
        } catch (Exception exception) {
            throw Utils.exception(exception, "Something bad happen when writing rules.");
        }
    }

    private static void writeRule(Rule rule, XMLStreamWriter output) {
        try {
            output.writeCharacters("  ");
            output.writeStartElement("Rule");
            writeAttribute("id", rule.getId(), output);
            writeAttribute("position", rule.getPosition(), output);
            writeAttribute("match", rule.getMatch(), output);
            writeAttribute("parameter", rule.getParameter(), output);
            writeAttribute("transform", rule.getTransform(), output);
            writeAttribute("remove", rule.getRemove(), output);
            writeAttribute("combine", rule.getCombine(), output);
            output.writeEndElement();
            output.writeCharacters(NEW_LINE);
        } catch (Exception exception) {
            throw Utils.exception(exception, "Error writing rule %s.", rule.getId());
        }
    }

    private static <T> void writeAttribute(String name, T value, XMLStreamWriter output) throws Exception {
        if (value != null) {
            output.writeAttribute(name, value.toString());
        }
    }

    private static final class RuleHandler extends DefaultHandler {

        final List<Rule> rules = new ArrayList<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!qName.equalsIgnoreCase("rule")) {
                return;
            }
            Utils.debug(LOGGER, "Start parsing rule.");
            RuleBuilder ruleBuilder = new RuleBuilder();
            getAttribute("id", attributes, ruleBuilder::withId);
            getAttribute("position", attributes, compose(Integer::valueOf, ruleBuilder::withPosition));
            getAttribute("match", attributes, ruleBuilder::withMatch);
            getAttribute("parameter", attributes, ruleBuilder::withParameter);
            getAttribute("remove", attributes, compose(Integer::valueOf, ruleBuilder::withRemove));
            getAttribute("transform", attributes, ruleBuilder::withTransform);
            getAttribute("combine", attributes, ruleBuilder::withCombine);
            rules.add(ruleBuilder.build());
            Utils.debug(LOGGER, "End parsing rule.");
        }

        private static <T> Consumer<String> compose(Function<String, T> convert, Consumer<T> setter) {
            return (value) -> setter.accept(convert.apply(value));
        }

        private void getAttribute(String attributeName, Attributes attributes, Consumer<String> setter) {
            String attributeValue = attributes.getValue(attributeName);
            if (attributeValue == null) {
                Utils.debug(LOGGER, "Rule %s attribute %s is NULL.", attributeName);
                return;
            }
            Utils.debug(LOGGER, "Rule %s attribute %s is %s.", attributeName, attributeValue);
            try {
                setter.accept(attributeValue);
            } catch (Exception exception) {
                throw Utils.exception(exception,
                        "Error setting attribute '%s' with value '%s'.", attributeName, attributeValue);
            }
        }
    }
}