package org.geoserver.params.extractor.web;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.params.extractor.RulesDao;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geoserver.web.wicket.GeoServerDataProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RulesModel extends GeoServerDataProvider<RuleModel> {

    private final static List<Property<RuleModel>> PROPERTIES = Arrays.asList(
            new BeanProperty<>("position", "position"),
            new BeanProperty<>("match", "match"),
            new BeanProperty<>("parameter", "parameter"),
            new BeanProperty<>("transform", "transform"),
            new BeanProperty<>("remove", "remove"),
            new BeanProperty<>("combine", "combine"));

    @Override
    protected List<Property<RuleModel>> getProperties() {
        return PROPERTIES;
    }

    @Override
    protected List<RuleModel> getItems() {
        GeoServerDataDirectory dataDirectory = (GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory");
        Resource rules = dataDirectory.get("rules.xml");
        return RulesDao.getRules(rules.in()).stream()
                .map(RuleModel::new)
                .collect(Collectors.toList());
    }
}
