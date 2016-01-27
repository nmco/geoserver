/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import java.util.regex.Pattern;

public final class RuleBuilder {

    private String id;
    private Integer position;
    private String match;
    private String parameter;
    private String transform;
    private Integer remove;
    private String combine;

    private Pattern pattern;

    public RuleBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public RuleBuilder withPosition(Integer position) {
        if (position != null) {
            this.position = position;
            this.pattern = Pattern.compile(String.format("^(?:/[^/]*){%d}(/([^/]+)).*$", position));
        }
        return this;
    }

    public RuleBuilder withMatch(String match) {
        if (match != null) {
            this.match = match;
            this.pattern = Pattern.compile(match);
        }
        return this;
    }

    public RuleBuilder withParameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    public RuleBuilder withRemove(Integer remove) {
        this.remove = remove;
        return this;
    }

    public RuleBuilder withTransform(String transform) {
        this.transform = transform;
        return this;
    }

    public RuleBuilder withCombine(String combine) {
        this.combine = combine;
        return this;
    }

    public Rule build() {
        Utils.checkCondition(id != null && !id.isEmpty(),"Rule id cannot be NULL or EMPTY.");
        Utils.checkCondition(pattern != null,"Pattern attribute is mandatory it cannot be NULL.");
        Utils.checkCondition(parameter != null && !parameter.isEmpty(),"Parameter attribute is mandatory it cannot be NULL or EMPTY.");
        Utils.checkCondition(transform != null && !transform.isEmpty(),"Transform attribute is mandatory it cannot be NULL or EMPTY.");
        return new Rule(id, position, match, parameter, transform, Utils.withDefault(remove, 1), combine, pattern);
    }
}
