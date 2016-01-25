/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import org.geotools.util.logging.Logging;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Rule {

    private static final Logger LOGGER = Logging.getLogger(Rule.class);

    private final int id;

    private final Pattern match;
    private final String parameter;
    private final String transform;
    private final int remove;

    private final Optional<String> combine;

    public Rule(int id, Pattern match, String parameter,
                String transform, int remove, Optional<String> combine) {
        this.id = id;
        this.match = match;
        this.parameter = parameter;
        this.transform = transform;
        this.remove = remove;
        this.combine = combine;
    }

    UrlTransform apply(UrlTransform urlTransform) {
        Utils.debug(LOGGER, "Start applying rule %d to URL '%s'.", id, urlTransform);
        Matcher matcher = match.matcher(urlTransform.getRequestUri());
        if (!matcher.matches()) {
            Utils.debug(LOGGER, "Rule %d doesn't match URL '%s'.", id, urlTransform);
            return urlTransform;
        }
        urlTransform.removeMatch(matcher.start(1), matcher.end(1));
        urlTransform.addParameter(parameter, matcher.replaceAll(transform));
        return urlTransform;
    }
}
