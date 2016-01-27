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
import org.geoserver.params.extractor.RulesDao;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geoserver.web.GeoServerSecuredPage;

import java.util.Optional;
import java.util.UUID;

public class ParamsExtractorRulePage extends GeoServerSecuredPage {

    public ParamsExtractorRulePage(Optional<RuleModel> optionalRuleModel) {
        RuleModel ruleModel = optionalRuleModel.orElse(new RuleModel());
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
                    RulesModel.saveOrUpdate((RuleModel) getForm().getModelObject());
                    doReturn(ParamsExtractorConfigPage.class);
                } catch (Exception exception) {
                    error(exception);
                }
            }
        });
        form.add(new BookmarkablePageLink("cancel", ParamsExtractorConfigPage.class));
    }
}
