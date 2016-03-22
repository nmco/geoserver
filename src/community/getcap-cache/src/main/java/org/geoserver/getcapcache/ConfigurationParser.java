/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.net.util.SubnetUtils;
import org.geotools.util.logging.Logging;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * <p>
 * Parser for GetCapabilities caching configurations.
 * The default configuration enables the caching and allow all IPs.
 * </p>
 */
final class ConfigurationParser {

    private final static Logger LOGGER = Logging.getLogger(ConfigurationParser.class);

    final static String CONFIGURATION_DIRECTORY = "getcapcahe";
    final static String CONFIGURATION_FILE_NAME = "configuration.xml";
    final static String CONFIGURATION_PATH = "getcapcahe/configuration.xml";

    final static String CONFIGURATION_DEFAULT_CONTENT = "<Caching enable='true'></Caching>";

    final static Configuration DEFAULT_CONFIGURATION = new ConfigurationBuilder().build();

    private ConfigurationParser() {
    }

    /**
     * If there is no configuration available (the input stream is empty) the default one is returned.
     * If an error happens when parsing the configuration NULL will be returned.
     * If everything went fine the parsed configuration is returned.
     */
    static Configuration parse(InputStream inputStream) {
        try {
            if (inputStream.available() == 0) {
                // no configuration available we return the default one
                Utils.info(LOGGER, "Configuration is EMPTY returning default configuration.");
                return DEFAULT_CONFIGURATION;
            }
            ConfigurationHandler handler = new ConfigurationHandler();
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(inputStream, handler);
            return handler.getConfiguration();
        } catch (Exception exception) {
            Utils.error(LOGGER, exception, "Error parsing configuration: %s", exception.getMessage());
        }
        return null;
    }

    /**
     * SAX handler for parsing the configurations.
     */
    private static final class ConfigurationHandler extends DefaultHandler {

        private ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        private final StringBuilder currentText = new StringBuilder();
        private boolean parsingText = false;

        private boolean parsingBlackList = false;
        private boolean parsingWhiteList = false;

        @Override
        public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
            if (qualifiedName.equalsIgnoreCase("SubNetMask")) {
                startParsingText();
            } else if (qualifiedName.equalsIgnoreCase("Blacklist")) {
                parsingBlackList = true;
            } else if (qualifiedName.equalsIgnoreCase("Whitelist")) {
                parsingWhiteList = true;
            } else if (qualifiedName.equalsIgnoreCase("Caching")) {
                configurationBuilder.withCaching(getBooleanAttribute("enable", attributes));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
            if (qualifiedName.equalsIgnoreCase("SubNetMask")) {
                handleSubNetMask(endParsingText());
            } else if (qualifiedName.equalsIgnoreCase("Blacklist")) {
                parsingBlackList = false;
            } else if (qualifiedName.equalsIgnoreCase("Whitelist")) {
                parsingWhiteList = false;
            }
        }

        @Override
        public void characters(char chars[], int start, int length) throws SAXException {
            if (parsingText) {
                currentText.append(new String(chars, start, length));
            }
        }

        public Configuration getConfiguration() {
            return configurationBuilder.build();
        }

        private void startParsingText() {
            currentText.setLength(0);
            parsingText = true;
        }

        private String endParsingText() {
            parsingText = false;
            return currentText.toString();
        }

        private void handleSubNetMask(String subNetMask) {
            try {
                SubnetUtils subnet = new SubnetUtils(subNetMask);
                // we want to include the host (192.168.33.0 - 192.168.33.255)
                subnet.setInclusiveHostCount(true);
                if (parsingBlackList) {
                    configurationBuilder.withBlackListedIps(subnet.getInfo());
                } else if (parsingWhiteList) {
                    configurationBuilder.withWhitedListedIps(subnet.getInfo());
                }
            } catch (Exception exception) {
                throw Utils.exception(exception, "Error parsing sub net mask '%s': %s", subNetMask, exception.getMessage());
            }
        }

        private Boolean getBooleanAttribute(String attributeName, Attributes attributes) {
            String stringValue = getStringAttribute(attributeName, attributes);
            if (stringValue == null) {
                return null;
            }
            return Boolean.valueOf(stringValue);
        }

        private String getStringAttribute(String attributeName, Attributes attributes) {
            String attributeValue = attributes.getValue(attributeName);
            if (attributeValue == null) {
                return null;
            }
            return attributeValue;
        }
    }
}
