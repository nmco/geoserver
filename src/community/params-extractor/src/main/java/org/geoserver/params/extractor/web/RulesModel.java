package org.geoserver.params.extractor.web;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.params.extractor.Rule;
import org.geoserver.params.extractor.RulesDao;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geoserver.web.wicket.GeoServerDataProvider;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RulesModel extends GeoServerDataProvider<RuleModel> {

    public static final Property<RuleModel> BUTTONS = new PropertyPlaceholder<>("Edit");

    private final static List<Property<RuleModel>> PROPERTIES = Arrays.asList(
            new BeanProperty<>("Position", "position"),
            new BeanProperty<>("Match", "match"),
            new BeanProperty<>("Activation", "activation"),
            new BeanProperty<>("Parameter", "parameter"),
            new BeanProperty<>("Transform", "transform"),
            new BeanProperty<>("Remove", "remove"),
            new BeanProperty<>("Combine", "combine"),
            BUTTONS);

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

    static void saveOrUpdate(RuleModel ruleModel) {
        GeoServerDataDirectory dataDir = (GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory");
        Rule rule = ruleModel.toRule();
        Resource rules = dataDir.get("rules.xml");
        Resource tmpRules = dataDir.get(UUID.randomUUID() + "-rules.xml");
        RulesDao.saveOrUpdateRule(rule, rules.in(), tmpRules.out());
        rules.delete();
        tmpRules.renameTo(rules);
    }

    static void deleteRules(String... rulesIds) {
        GeoServerDataDirectory dataDir = (GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory");
        Resource rules = dataDir.get("rules.xml");
        Resource tmpRules = dataDir.get(UUID.randomUUID() + "-rules.xml");
        RulesDao.deleteRules(rules.in(), tmpRules.out(), rulesIds);
        rules.delete();
        tmpRules.renameTo(rules);
    }
}
