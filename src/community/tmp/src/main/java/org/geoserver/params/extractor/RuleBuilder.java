/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import java.util.Optional;
import java.util.regex.Pattern;

final class RuleBuilder {

    private final int id;

    private Pattern match;
    private String parameter;
    private Integer remove;
    private String transform;
    private String combine;

    public RuleBuilder(int id) {
        this.id = id;
    }

    RuleBuilder withPosition(Integer position) {
        this.match = Pattern.compile(String.format("^(?:.*?/){%d}(([^/]+?)/).*$", position));
        return this;
    }

    RuleBuilder withMatch(String match) {
        this.match = Pattern.compile(match);
        return this;
    }

    RuleBuilder withParameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    RuleBuilder withRemove(Integer remove) {
        this.remove = remove;
        return this;
    }

    RuleBuilder withTransform(String transform) {
        this.transform = transform;
        return this;
    }

    RuleBuilder withCombine(String combine) {
        this.combine = combine;
        return this;
    }

    Rule build() {
        Utils.checkCondition(match != null,
                "Match attribute is mandatory it cannot be NULL or EMPTY.");
        Utils.checkCondition(parameter != null && !parameter.isEmpty(),
                "Parameter attribute is mandatory it cannot be NULL or EMPTY.");
        Utils.checkCondition(transform != null && !transform.isEmpty(),
                "Transform attribute is mandatory it cannot be NULL or EMPTY.");
        return new Rule(id, match, parameter, transform,
                Optional.ofNullable(remove).orElse(1), Optional.ofNullable(combine)
        );
    }
}
