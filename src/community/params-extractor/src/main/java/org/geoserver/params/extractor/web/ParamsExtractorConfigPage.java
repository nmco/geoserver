/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor.web;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerTablePanel;

import java.util.Optional;

public class ParamsExtractorConfigPage extends GeoServerSecuredPage {

    private GeoServerTablePanel<RuleModel> rulesPanel;
    private RulesModel rulesModel;

    public ParamsExtractorConfigPage() {
        setHeaderPanel(headerPanel());
        add(rulesPanel = new GeoServerTablePanel<RuleModel>("rulesPanel", rulesModel = new RulesModel(), true) {

            @Override
            protected Component getComponentForProperty(String id, IModel<RuleModel> itemModel,
                                                        GeoServerDataProvider.Property<RuleModel> property) {
                return null;
            }
        });
    }

    private Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);
        header.add(new AjaxLink<Object>("addNew") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(new ParamsExtractorRulePage(Optional.empty()));
            }
        });
        return header;
    }
}
