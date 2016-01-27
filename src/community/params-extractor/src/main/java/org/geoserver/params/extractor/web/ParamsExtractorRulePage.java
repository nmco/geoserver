/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor.web;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.params.extractor.Rule;
import org.geoserver.params.extractor.RulesDao;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geoserver.web.GeoServerSecuredPage;

import java.util.Optional;
import java.util.UUID;

public class ParamsExtractorRulePage extends GeoServerSecuredPage {

    public ParamsExtractorRulePage(Optional<Rule> rule) {
        RuleModel ruleModel = rule.isPresent() ? new RuleModel(rule.get()) : new RuleModel();
        Form<RuleModel> form = new Form<>("form", new CompoundPropertyModel<>(ruleModel));
        add(form);
        form.add(new NumberTextField<Integer>("position").setMinimum(1));
        form.add(new TextField<String>("match"));
        form.add(new TextField<String>("parameter"));
        form.add(new TextField<String>("transform"));
        form.add(new NumberTextField<Integer>("remove").setMinimum(1));
        form.add(new TextField<String>("combine"));
        form.add(new SubmitLink("save") {
            @Override
            public void onSubmit() {
                try {
                    GeoServerDataDirectory dataDir = (GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory");
                    RuleModel formData = (RuleModel) getForm().getModelObject();
                    Resource tmpRules = dataDir.get(UUID.randomUUID() + "-rules.xml");
                    Resource rules = dataDir.get("rules.xml");
                    RulesDao.saveOrUpdateRule(formData.toRule(), rules.in(), tmpRules.out());
                    rules.delete();
                    tmpRules.renameTo(rules);
                    doReturn(ParamsExtractorConfigPage.class);
                } catch (Exception exception) {
                    error(exception);
                }
            }
        });
        form.add(new BookmarkablePageLink("cancel", ParamsExtractorConfigPage.class));
    }
}
